package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthRepository
import com.example.data.auth.JwtManager
import com.example.data.auth.LoginRequest
import com.example.data.local.AppDatabase
import com.example.data.model.AccountStatus
import com.example.data.model.AdminStats
import com.example.data.model.Message
import com.example.data.model.PropertyStatus
import com.example.data.model.PropertyType
import com.example.data.model.PropertyWithDetails
import com.example.data.model.ReportStatus
import com.example.data.model.ReportWithDetails
import com.example.data.model.TransactionType
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.model.VisitRequest
import com.example.data.repository.RealEstateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * États d'authentification stricts pour l'espace administrateur.
 */
sealed class AdminAuthState {
    object Idle : AdminAuthState()
    object Loading : AdminAuthState()
    data class Authenticated(val adminUser: User) : AdminAuthState()
    data class AccessDenied(val reason: String, val attemptedRole: UserRole? = null) : AdminAuthState()
    data class Error(val message: String) : AdminAuthState()
}

/**
 * État de protection et de contrôle d'accès aux routes du dashboard.
 */
sealed class AdminRouteAccessState {
    object Verifying : AdminRouteAccessState()
    data class Granted(val adminUser: User) : AdminRouteAccessState()
    data class Denied(val reason: String) : AdminRouteAccessState()
}

/**
 * AdminViewModel dédié à la gestion sécurisée de l'authentification et des autorisations administratives.
 * Toutes les vérifications de rôle sont effectuées directement via la base de données Room (SQLite).
 * Protège hermétiquement l'accès au Dashboard et à toutes ses futures sous-routes.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val userDao = database.userDao()
    private val repository = RealEstateRepository(application)
    private val authRepository = AuthRepository(application)

    // État d'authentification administrateur
    private val _adminAuthState = MutableStateFlow<AdminAuthState>(AdminAuthState.Idle)
    val adminAuthState: StateFlow<AdminAuthState> = _adminAuthState.asStateFlow()

    // Identifiant de l'administrateur actif
    private val _currentAdminId = MutableStateFlow<Long?>(null)
    val currentAdminId: StateFlow<Long?> = _currentAdminId.asStateFlow()

    // Utilisateur administrateur réactif observé directement depuis la base Room
    val currentAdminUser: StateFlow<User?> = _currentAdminId
        .flatMapLatest { id ->
            if (id != null) userDao.getUserById(id) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Autorisation en temps réel dérivée directement de Room :
    // Si le rôle de l'utilisateur change ou si son compte est suspendu en base, l'accès est instantanément révoqué !
    val isAdminAuthorized: StateFlow<Boolean> = _currentAdminId
        .flatMapLatest { id ->
            if (id != null) userDao.observeIsUserActiveAdmin(id) else flowOf(false)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Message de notification ou d'erreur
    private val _adminUiMessage = MutableStateFlow<String?>(null)
    val adminUiMessage: StateFlow<String?> = _adminUiMessage.asStateFlow()

    fun clearAdminUiMessage() {
        _adminUiMessage.value = null
    }

    init {
        // Vérification de la session existante au démarrage via la base Room
        verifyAdminSessionFromStorage()
    }

    /**
     * Vérifie la session active en validant le rôle ADMIN directement dans la base Room.
     */
    fun verifyAdminSessionFromStorage() {
        viewModelScope.launch {
            _adminAuthState.value = AdminAuthState.Loading
            val sessionUser = authRepository.getCurrentUser()
            if (sessionUser != null) {
                // Vérification stricte en base Room
                val isDbAdmin = withContext(Dispatchers.IO) {
                    userDao.isUserActiveAdmin(sessionUser.id)
                }
                if (isDbAdmin) {
                    val dbAdminUser = withContext(Dispatchers.IO) {
                        userDao.getUserByIdSync(sessionUser.id)
                    }
                    if (dbAdminUser != null) {
                        _currentAdminId.value = dbAdminUser.id
                        _adminAuthState.value = AdminAuthState.Authenticated(dbAdminUser)
                    } else {
                        revokeAdminAccess("Compte administrateur introuvable en base.")
                    }
                } else {
                    revokeAdminAccess("Session active non-administrateur (${sessionUser.role}).")
                }
            } else {
                _currentAdminId.value = null
                _adminAuthState.value = AdminAuthState.Idle
            }
        }
    }

    /**
     * Authentification sécurisée de l'administrateur avec vérification du rôle dans la base Room.
     * Si les identifiants correspondent à un compte d'un autre rôle (USER, OWNER, AGENT),
     * l'accès est formellement refusé et la session immédiatement purgée.
     */
    fun loginAsAdmin(
        email: String,
        password: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank() || password.isBlank()) {
            val err = "Veuillez renseigner votre identifiant et votre mot de passe."
            _adminAuthState.value = AdminAuthState.Error(err)
            onError(err)
            return
        }

        viewModelScope.launch {
            _adminAuthState.value = AdminAuthState.Loading

            // 1. Recherche de l'utilisateur directement dans la base de données Room
            val dbUser = withContext(Dispatchers.IO) {
                userDao.getUserByEmail(cleanEmail)
            }

            if (dbUser == null) {
                val err = "Identifiants administrateur incorrects. Aucun compte associé à cet email."
                _adminAuthState.value = AdminAuthState.Error(err)
                onError(err)
                return@launch
            }

            // 2. Vérification du mot de passe haché
            val inputHash = JwtManager.hashPassword(password)
            if (dbUser.passwordHash != inputHash) {
                val err = "Mot de passe administrateur incorrect."
                _adminAuthState.value = AdminAuthState.Error(err)
                onError(err)
                return@launch
            }

            // 3. Vérification STRICTE du rôle ADMIN via la base Room
            if (dbUser.role != UserRole.ADMIN) {
                // Déconnexion et rejet immédiat
                authRepository.logout()
                _currentAdminId.value = null

                val reason = "Accès refusé : Le compte '${dbUser.email}' possède le rôle '${dbUser.role}'. L'accès au dashboard est strictement réservé aux comptes avec le rôle ADMIN."
                _adminAuthState.value = AdminAuthState.AccessDenied(reason, dbUser.role)
                onError(reason)
                return@launch
            }

            // 4. Vérification du statut actif et non suspendu
            if (dbUser.isSuspended || dbUser.accountStatus == AccountStatus.REJECTED) {
                authRepository.logout()
                _currentAdminId.value = null
                val err = "Ce compte administrateur a été désactivé."
                _adminAuthState.value = AdminAuthState.Error(err)
                onError(err)
                return@launch
            }

            // 5. Authentification réussie : enregistrement des jetons et mise à jour de l'état
            val tokens = JwtManager.generateTokens(dbUser)
            authRepository.login(LoginRequest(email = cleanEmail, password = password))

            _currentAdminId.value = dbUser.id
            _adminAuthState.value = AdminAuthState.Authenticated(dbUser)
            _adminUiMessage.value = "Bienvenue dans l'espace d'administration, ${dbUser.firstName} !"
            onSuccess()
        }
    }

    /**
     * Déconnexion sécurisée de l'administrateur et purge complète de la session.
     */
    fun logoutAdmin(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            authRepository.logout()
            _currentAdminId.value = null
            _adminAuthState.value = AdminAuthState.Idle
            _adminUiMessage.value = "Session administrateur fermée."
            onComplete()
        }
    }

    /**
     * Révocation immédiate des privilèges d'accès.
     */
    private fun revokeAdminAccess(reason: String) {
        _currentAdminId.value = null
        _adminAuthState.value = AdminAuthState.AccessDenied(reason)
    }

    /**
     * Fonction de garde pour protéger les futures routes ou sous-pages du dashboard.
     * Vérifie de manière synchrone et asynchrone la présence du rôle ADMIN en base Room.
     */
    suspend fun verifyRouteAccess(userId: Long? = _currentAdminId.value): AdminRouteAccessState = withContext(Dispatchers.IO) {
        if (userId == null) {
            return@withContext AdminRouteAccessState.Denied("Aucune session administrateur active.")
        }
        val user = userDao.getUserByIdSync(userId)
        if (user == null) {
            return@withContext AdminRouteAccessState.Denied("Utilisateur introuvable dans la base de données.")
        }
        if (user.role != UserRole.ADMIN) {
            return@withContext AdminRouteAccessState.Denied("Privilèges insuffisants : rôle ${user.role} non autorisé sur cette route.")
        }
        if (user.isSuspended) {
            return@withContext AdminRouteAccessState.Denied("Ce compte administrateur est suspendu.")
        }
        return@withContext AdminRouteAccessState.Granted(user)
    }

    // ==========================================
    // DONNÉES ET OPÉRATIONS DU DASHBOARD ADMIN
    // ==========================================

    val allProperties: StateFlow<List<PropertyWithDetails>> = repository.getAllPropertiesWithDetails()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingProperties: StateFlow<List<PropertyWithDetails>> = repository.getPendingProperties()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReports: StateFlow<List<ReportWithDetails>> = repository.getAllReportsWithDetails()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<User>> = repository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingApprovalUsers: StateFlow<List<User>> = repository.getPendingApprovalUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allVisits: StateFlow<List<VisitRequest>> = repository.getAllVisits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMessages: StateFlow<List<Message>> = repository.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Statistiques en temps réel calculées sur la base Room
    val adminStats: StateFlow<AdminStats> = combine(
        allProperties,
        allUsers,
        allReports,
        allVisits,
        allMessages
    ) { props, users, reports, visits, messages ->
        val totalUsers = users.size
        val owners = users.count { it.role == UserRole.OWNER }
        val agents = users.count { it.role == UserRole.AGENT }
        val regular = users.count { it.role == UserRole.USER }
        val admins = users.count { it.role == UserRole.ADMIN }

        val pending = props.count { it.property.status == PropertyStatus.PENDING }
        val published = props.count { it.property.status == PropertyStatus.PUBLISHED }
        val rejected = props.count { it.property.status == PropertyStatus.REJECTED }
        val rented = props.count { it.property.status == PropertyStatus.RENTED }
        val sold = props.count { it.property.status == PropertyStatus.SOLD }
        val archived = props.count { it.property.status == PropertyStatus.ARCHIVED || it.property.status == PropertyStatus.DRAFT }

        val houses = props.count { it.property.propertyType == PropertyType.HOUSE || it.property.propertyType == PropertyType.VILLA }
        val apartments = props.count { it.property.propertyType == PropertyType.APARTMENT || it.property.propertyType == PropertyType.STUDIO }
        val lands = props.count { it.property.propertyType == PropertyType.LAND }
        val commercial = props.count { it.property.propertyType == PropertyType.COMMERCIAL }

        val forRent = props.count { it.property.transactionType == TransactionType.RENT }
        val forSale = props.count { it.property.transactionType == TransactionType.SALE }

        val views = props.sumOf { it.property.viewCount }
        val visitCount = visits.size
        val rentalOrPurchaseOffers = messages.count { msg ->
            msg.content.contains("candidature", ignoreCase = true) ||
            msg.content.contains("offre d'achat", ignoreCase = true) ||
            msg.content.contains("dossier de location", ignoreCase = true)
        }

        AdminStats(
            totalUsersCount = totalUsers,
            ownersCount = owners,
            agentsCount = agents,
            regularUsersCount = regular,
            adminsCount = admins,
            totalProperties = props.size,
            housesCount = houses,
            apartmentsCount = apartments,
            landsCount = lands,
            commercialCount = commercial,
            forRentCount = forRent,
            forSaleCount = forSale,
            pendingCount = pending,
            publishedCount = published,
            rejectedCount = rejected,
            rentedCount = rented,
            soldCount = sold,
            archivedCount = archived,
            visitRequestsCount = visitCount,
            rentalAndPurchaseRequestsCount = rentalOrPurchaseOffers,
            totalReportsCount = reports.count { it.report.status == ReportStatus.PENDING },
            totalViewsCount = views
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdminStats())

    // Opérations administratives protégées par contrôle en base Room
    fun approveProperty(propertyId: Long) {
        val adminId = _currentAdminId.value ?: return
        viewModelScope.launch {
            val result = repository.updatePropertyStatus(adminId, propertyId, PropertyStatus.PUBLISHED)
            result.onSuccess {
                _adminUiMessage.value = "Annonce approuvée et publiée avec succès."
            }.onFailure { err ->
                _adminUiMessage.value = err.message ?: "Erreur lors de la validation."
            }
        }
    }

    fun rejectProperty(propertyId: Long, reason: String = "") {
        val adminId = _currentAdminId.value ?: return
        viewModelScope.launch {
            val result = repository.updatePropertyStatus(adminId, propertyId, PropertyStatus.REJECTED)
            result.onSuccess {
                _adminUiMessage.value = "Annonce refusée et retirée de la publication."
            }.onFailure { err ->
                _adminUiMessage.value = err.message ?: "Erreur lors du rejet."
            }
        }
    }

    fun approveUser(targetUserId: Long) {
        val adminId = _currentAdminId.value ?: return
        viewModelScope.launch {
            val result = repository.approveUser(adminId, targetUserId)
            result.onSuccess {
                _adminUiMessage.value = "Compte utilisateur validé avec succès."
            }.onFailure { err ->
                _adminUiMessage.value = err.message ?: "Erreur d'approbation."
            }
        }
    }

    fun rejectUser(targetUserId: Long) {
        val adminId = _currentAdminId.value ?: return
        viewModelScope.launch {
            val result = repository.rejectUser(adminId, targetUserId)
            result.onSuccess {
                _adminUiMessage.value = "Demande de compte refusée."
            }.onFailure { err ->
                _adminUiMessage.value = err.message ?: "Erreur de rejet."
            }
        }
    }

    fun toggleUserSuspension(targetUserId: Long) {
        val adminId = _currentAdminId.value ?: return
        viewModelScope.launch {
            val result = repository.toggleUserSuspension(adminId, targetUserId)
            result.onSuccess {
                _adminUiMessage.value = "Statut du compte mis à jour."
            }.onFailure { err ->
                _adminUiMessage.value = err.message ?: "Erreur lors de la mise à jour."
            }
        }
    }

    fun toggleUserVerification(targetUserId: Long) {
        val adminId = _currentAdminId.value ?: return
        viewModelScope.launch {
            val result = repository.toggleUserVerification(adminId, targetUserId)
            result.onSuccess {
                _adminUiMessage.value = "Vérification de l'utilisateur modifiée."
            }.onFailure { err ->
                _adminUiMessage.value = err.message ?: "Erreur."
            }
        }
    }

    fun updateUserRole(targetUserId: Long, newRole: UserRole) {
        val adminId = _currentAdminId.value ?: return
        viewModelScope.launch {
            val result = repository.updateUserRole(adminId, targetUserId, newRole)
            result.onSuccess {
                _adminUiMessage.value = "Rôle de l'utilisateur modifié en $newRole."
            }.onFailure { err ->
                _adminUiMessage.value = err.message ?: "Erreur de modification du rôle."
            }
        }
    }

    fun deleteUser(targetUserId: Long) {
        val adminId = _currentAdminId.value ?: return
        viewModelScope.launch {
            val result = repository.deleteUser(adminId, targetUserId)
            result.onSuccess {
                _adminUiMessage.value = "Compte utilisateur supprimé définitivement."
            }.onFailure { err ->
                _adminUiMessage.value = err.message ?: "Erreur lors de la suppression."
            }
        }
    }

    fun resolveReport(reportId: Long, status: ReportStatus) {
        val adminId = _currentAdminId.value ?: return
        viewModelScope.launch {
            val result = repository.resolveReport(adminId, reportId, status)
            result.onSuccess {
                _adminUiMessage.value = "Signalement traité (${status.label})."
            }.onFailure { err ->
                _adminUiMessage.value = err.message ?: "Erreur lors de la modération."
            }
        }
    }

    fun adminDeleteReportedProperty(reportId: Long, propertyId: Long) {
        val adminId = _currentAdminId.value ?: return
        viewModelScope.launch {
            val result = repository.adminDeleteReportedProperty(adminId, reportId, propertyId)
            result.onSuccess {
                _adminUiMessage.value = "Bien supprimé et signalement résolu."
            }.onFailure { err ->
                _adminUiMessage.value = err.message ?: "Erreur lors de la suppression."
            }
        }
    }

    fun deleteProperty(propertyId: Long) {
        val adminId = _currentAdminId.value ?: return
        viewModelScope.launch {
            val result = repository.deleteProperty(adminId, propertyId)
            result.onSuccess {
                _adminUiMessage.value = "Annonce supprimée définitivement."
            }.onFailure { err ->
                _adminUiMessage.value = err.message ?: "Erreur lors de la suppression de l'annonce."
            }
        }
    }

    fun updateVisitStatus(requestId: Long, status: com.example.data.model.VisitStatus) {
        val adminId = _currentAdminId.value ?: return
        viewModelScope.launch {
            val result = repository.updateVisitStatus(adminId, requestId, status)
            result.onSuccess {
                _adminUiMessage.value = "Statut de la demande de visite mis à jour : ${status.label}."
            }.onFailure { err ->
                _adminUiMessage.value = err.message ?: "Erreur lors de la mise à jour de la visite."
            }
        }
    }
}
