package com.example.data.auth

import android.content.Context
import android.util.Base64
import com.example.data.model.User
import com.example.data.model.UserRole
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val password: String,
    val role: UserRole
)

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long = 3600L // 1 hour in seconds
)

data class AuthResult(
    val user: User,
    val tokens: AuthTokens
)

data class JwtPayload(
    val sub: String, // User ID as string
    val email: String,
    val role: String,
    val status: String = "ACTIVE",
    val name: String,
    val exp: Long,
    val iat: Long,
    val type: String // "access" or "refresh"
)

/**
 * JWT Token Provider & Validator.
 * Generates and validates standard HMAC-SHA256 JWT tokens.
 */
object JwtManager {
    // Standard secure HMAC key for local cryptographically signed JWTs
    private const val JWT_SECRET = "logement_secure_jwt_secret_key_2026_super_safe_token"
    private const val ACCESS_TOKEN_VALIDITY_MS = 60 * 60 * 1000L // 1 hour
    private const val REFRESH_TOKEN_VALIDITY_MS = 30 * 24 * 60 * 60 * 1000L // 30 days

    fun generateTokens(user: User): AuthTokens {
        val now = System.currentTimeMillis()
        val accessTokenExp = now + ACCESS_TOKEN_VALIDITY_MS
        val refreshTokenExp = now + REFRESH_TOKEN_VALIDITY_MS

        val accessPayload = JSONObject().apply {
            put("sub", user.id.toString())
            put("email", user.email)
            put("role", user.role.name)
            put("status", user.accountStatus.name)
            put("name", user.fullName)
            put("iat", now / 1000)
            put("exp", accessTokenExp / 1000)
            put("type", "access")
        }

        val refreshPayload = JSONObject().apply {
            put("sub", user.id.toString())
            put("email", user.email)
            put("role", user.role.name)
            put("status", user.accountStatus.name)
            put("iat", now / 1000)
            put("exp", refreshTokenExp / 1000)
            put("type", "refresh")
        }

        val accessToken = signJwt(accessPayload)
        val refreshToken = signJwt(refreshPayload)

        return AuthTokens(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = ACCESS_TOKEN_VALIDITY_MS / 1000
        )
    }

    private fun signJwt(payload: JSONObject): String {
        val header = JSONObject().apply {
            put("alg", "HS256")
            put("typ", "JWT")
        }

        val headerBase64 = base64UrlEncode(header.toString().toByteArray(StandardCharsets.UTF_8))
        val payloadBase64 = base64UrlEncode(payload.toString().toByteArray(StandardCharsets.UTF_8))
        val contentToSign = "$headerBase64.$payloadBase64"

        val signature = hmacSha256(contentToSign, JWT_SECRET)
        val signatureBase64 = base64UrlEncode(signature)

        return "$contentToSign.$signatureBase64"
    }

    fun verifyAndDecode(token: String): JwtPayload? {
        val parts = token.split(".")
        if (parts.size != 3) return null

        val contentToSign = "${parts[0]}.${parts[1]}"
        val expectedSig = base64UrlEncode(hmacSha256(contentToSign, JWT_SECRET))

        if (expectedSig != parts[2]) {
            return null // Invalid signature
        }

        return try {
            val payloadJson = String(base64UrlDecode(parts[1]), StandardCharsets.UTF_8)
            val json = JSONObject(payloadJson)
            val exp = json.optLong("exp", 0)
            val nowSec = System.currentTimeMillis() / 1000

            if (exp > 0 && exp < nowSec) {
                return null // Expired
            }

            JwtPayload(
                sub = json.getString("sub"),
                email = json.getString("email"),
                role = json.optString("role", UserRole.USER.name),
                status = json.optString("status", "ACTIVE"),
                name = json.optString("name", ""),
                exp = exp,
                iat = json.optLong("iat", 0),
                type = json.optString("type", "access")
            )
        } catch (e: Exception) {
            null
        }
    }

    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray(StandardCharsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    private fun hmacSha256(data: String, secret: String): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(secret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
        mac.init(secretKey)
        return mac.doFinal(data.toByteArray(StandardCharsets.UTF_8))
    }

    private fun base64UrlEncode(bytes: ByteArray): String {
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    private fun base64UrlDecode(str: String): ByteArray {
        return Base64.decode(str, Base64.URL_SAFE or Base64.NO_WRAP)
    }
}

/**
 * Secure Token Storage using Android Encrypted preferences/private Mode.
 */
class TokenStorage(context: Context) {
    private val prefs = context.getSharedPreferences("logement_secure_auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ACCESS_TOKEN = "jwt_access_token"
        private const val KEY_REFRESH_TOKEN = "jwt_refresh_token"
        private const val KEY_USER_ID = "auth_user_id"
        private const val KEY_USER_EMAIL = "auth_user_email"
        private const val KEY_USER_ROLE = "auth_user_role"
    }

    fun saveTokens(tokens: AuthTokens, user: User) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, tokens.accessToken)
            .putString(KEY_REFRESH_TOKEN, tokens.refreshToken)
            .putLong(KEY_USER_ID, user.id)
            .putString(KEY_USER_EMAIL, user.email)
            .putString(KEY_USER_ROLE, user.role.name)
            .apply()
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)
    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)
    fun getSavedUserId(): Long? {
        val id = prefs.getLong(KEY_USER_ID, -1L)
        return if (id != -1L) id else null
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
