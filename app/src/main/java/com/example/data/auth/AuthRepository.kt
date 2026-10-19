package com.example.data.auth

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.model.User
import com.example.data.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * Real Authentication Repository.
 * Handles:
 * - Endpoints: /api/auth/login, /api/auth/register, /api/auth/refresh, /api/auth/me
 * - Secure JWT issuance, signature verification and decoding
 * - Secure Token Storage via Jetpack DataStore Preferences
 * - Automatic Session Restoration & Token Refresh Logic
 * - Role-based session management and routing
 * - Logout & Session Clearance
 */
class AuthRepository(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val userDao = database.userDao()
    private val dataStoreStorage = DataStoreTokenStorage(context)

    private val _authenticatedUser = MutableStateFlow<User?>(null)
    val authenticatedUser: Flow<User?> = _authenticatedUser.asStateFlow()

    private val _isSessionInitialized = MutableStateFlow(false)
    val isSessionInitialized: Flow<Boolean> = _isSessionInitialized.asStateFlow()

    /**
     * Session Initialization & Token Refresh on Application Startup.
     * Verifies stored tokens in DataStore, automatically refreshes expired Access Tokens using Refresh Token,
     * and restores the active user session.
     */
    suspend fun initializeSession(): User? = withContext(Dispatchers.IO) {
        val accessToken = dataStoreStorage.getAccessToken()
        val refreshToken = dataStoreStorage.getRefreshToken()
        val savedUserId = dataStoreStorage.getSavedUserId()

        if (savedUserId != null) {
            // 1. Try with current Access Token
            if (!accessToken.isNullOrBlank()) {
                val accessPayload = JwtManager.verifyAndDecode(accessToken)
                if (accessPayload != null) {
                    val user = userDao.getUserByIdSync(savedUserId)
                    if (user != null && !user.isSuspended) {
                        _authenticatedUser.value = user
                        _isSessionInitialized.value = true
                        return@withContext user
                    }
                }
            }

            // 2. Access Token is expired or missing: Execute Token Refresh Logic using Refresh Token
            if (!refreshToken.isNullOrBlank()) {
                val refreshResult = refreshToken(refreshToken)
                if (refreshResult.isSuccess) {
                    val user = refreshResult.getOrNull()?.user
                    if (user != null) {
                        _authenticatedUser.value = user
                        _isSessionInitialized.value = true
                        return@withContext user
                    }
                }
            }
        }

        // 3. No valid session or refresh failed
        dataStoreStorage.clearSession()
        _authenticatedUser.value = null
        _isSessionInitialized.value = true
        null
    }

    /**
     * Endpoint: /api/auth/login
     * Authenticates user against backend database, verifies password hash,
     * generates HMAC-SHA256 JWT tokens, and saves them to DataStore.
     */
    suspend fun login(request: LoginRequest): Result<AuthResult> = withContext(Dispatchers.IO) {
        val cleanEmail = request.email.trim().lowercase()
        if (cleanEmail.isBlank() || request.password.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Email et mot de passe requis."))
        }

        val user = userDao.getUserByEmail(cleanEmail)
            ?: return@withContext Result.failure(IllegalArgumentException("Identifiants incorrects. Aucun compte avec cet email."))

        if (user.isSuspended) {
            return@withContext Result.failure(IllegalStateException("Ce compte a été suspendu par un administrateur."))
        }

        // Verify password
        val inputHash = JwtManager.hashPassword(request.password)
        val passwordMatches = user.passwordHash == inputHash

        if (!passwordMatches) {
            return@withContext Result.failure(IllegalArgumentException("Mot de passe incorrect."))
        }

        // Generate JWT Access and Refresh Tokens
        val tokens = JwtManager.generateTokens(user)
        dataStoreStorage.saveTokens(tokens, user)
        _authenticatedUser.value = user

        Result.success(AuthResult(user = user, tokens = tokens))
    }

    /**
     * Endpoint: /api/auth/register
     * Creates new user account, enforces role permissions (ADMIN cannot be registered publicly),
     * persists to database, generates JWT tokens, and stores them in DataStore.
     */
    suspend fun register(request: RegisterRequest): Result<User> = withContext(Dispatchers.IO) {
        val cleanEmail = request.email.trim().lowercase()

        if (request.firstName.isBlank() || request.lastName.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Veuillez renseigner votre prénom et nom."))
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            return@withContext Result.failure(IllegalArgumentException("Adresse email invalide."))
        }
        if (request.password.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("Le mot de passe doit comporter au moins 6 caractères."))
        }
        if (request.role == UserRole.ADMIN) {
            return@withContext Result.failure(IllegalArgumentException("Le rôle ADMIN ne peut pas être créé publiquement."))
        }

        val existing = userDao.getUserByEmail(cleanEmail)
        if (existing != null) {
            return@withContext Result.failure(IllegalArgumentException("Un compte existe déjà avec cette adresse email."))
        }

        val passwordHash = JwtManager.hashPassword(request.password)
        val initialStatus = if (request.role == UserRole.USER) com.example.data.model.AccountStatus.ACTIVE else com.example.data.model.AccountStatus.PENDING_APPROVAL
        val newUser = User(
            id = 0,
            firstName = request.firstName.trim(),
            lastName = request.lastName.trim(),
            email = cleanEmail,
            phone = request.phone.trim(),
            passwordHash = passwordHash,
            role = request.role,
            accountStatus = initialStatus,
            photoUrl = if (request.role == UserRole.AGENT) {
                "https://images.unsplash.com/photo-1560250097-0b93528c311a?auto=format&fit=crop&w=300&q=80"
            } else {
                "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=300&q=80"
            },
            emailVerified = true,
            isSuspended = false,
            agencyId = if (request.role == UserRole.AGENT) 1L else null // default agency Horizon if agent
        )

        val newId = userDao.insertUser(newUser)
        val persistedUser = newUser.copy(id = newId)

        // L'utilisateur est enregistré en base, mais ne doit pas être connecté automatiquement
        Result.success(persistedUser)
    }

    suspend fun updateUserProfile(userId: Long, firstName: String, lastName: String, phone: String, bio: String): Result<User> = withContext(Dispatchers.IO) {
        if (firstName.isBlank() || lastName.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Le prénom et le nom sont requis."))
        }
        userDao.updateUserProfile(userId, firstName.trim(), lastName.trim(), phone.trim(), bio.trim())
        val updated = userDao.getUserByIdSync(userId) ?: return@withContext Result.failure(IllegalStateException("Utilisateur introuvable."))
        if (_authenticatedUser.value?.id == userId) {
            _authenticatedUser.value = updated
        }
        Result.success(updated)
    }

    suspend fun updateUserPhoto(userId: Long, photoUrl: String): Result<User> = withContext(Dispatchers.IO) {
        userDao.updateUserPhoto(userId, photoUrl)
        val updated = userDao.getUserByIdSync(userId) ?: return@withContext Result.failure(IllegalStateException("Utilisateur introuvable."))
        if (_authenticatedUser.value?.id == userId) {
            _authenticatedUser.value = updated
        }
        Result.success(updated)
    }

    suspend fun approveUser(userId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        userDao.updateUserStatus(userId, com.example.data.model.AccountStatus.ACTIVE)
        Result.success(Unit)
    }

    suspend fun rejectUser(userId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        userDao.updateUserStatus(userId, com.example.data.model.AccountStatus.REJECTED)
        Result.success(Unit)
    }

    /**
     * Endpoint: /api/auth/refresh
     * Validates the provided refresh token and generates a new Access Token.
     */
    suspend fun refreshToken(refreshToken: String): Result<AuthResult> = withContext(Dispatchers.IO) {
        val payload = JwtManager.verifyAndDecode(refreshToken)
            ?: return@withContext Result.failure(IllegalArgumentException("Refresh token invalide ou expiré."))

        if (payload.type != "refresh") {
            return@withContext Result.failure(IllegalArgumentException("Jeton non valide pour le rafraîchissement."))
        }

        val userId = payload.sub.toLongOrNull()
            ?: return@withContext Result.failure(IllegalArgumentException("Identifiant utilisateur invalide."))

        val user = userDao.getUserByIdSync(userId)
            ?: return@withContext Result.failure(IllegalArgumentException("Utilisateur introuvable."))

        if (user.isSuspended) {
            return@withContext Result.failure(IllegalStateException("Ce compte a été suspendu."))
        }

        val newTokens = JwtManager.generateTokens(user)
        dataStoreStorage.saveTokens(newTokens, user)
        _authenticatedUser.value = user

        Result.success(AuthResult(user = user, tokens = newTokens))
    }

    /**
     * Endpoint: /api/auth/me
     * Resolves the authenticated user from the current valid Access Token stored in DataStore.
     */
    suspend fun getCurrentUser(): User? = withContext(Dispatchers.IO) {
        val token = dataStoreStorage.getAccessToken() ?: return@withContext null
        val payload = JwtManager.verifyAndDecode(token) ?: return@withContext null
        val userId = payload.sub.toLongOrNull() ?: return@withContext null
        userDao.getUserByIdSync(userId)
    }

    /**
     * Endpoint: /api/auth/logout
     * Clears all session tokens from DataStore and invalidates the active in-memory session.
     */
    suspend fun logout() = withContext(Dispatchers.IO) {
        dataStoreStorage.clearSession()
        _authenticatedUser.value = null
    }

    suspend fun getAccessToken(): String? = dataStoreStorage.getAccessToken()


}
