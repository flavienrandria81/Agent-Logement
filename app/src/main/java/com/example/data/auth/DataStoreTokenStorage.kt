package com.example.data.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.io.IOException

// Application context extension property for singleton DataStore instance
val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_session_preferences")

data class StoredAuthSession(
    val accessToken: String?,
    val refreshToken: String?,
    val userId: Long?,
    val email: String?,
    val role: String?
)

/**
 * Secure Session & Token Storage using Jetpack DataStore Preferences.
 * Ensures non-blocking, thread-safe, coroutine-based persistence of authentication tokens.
 */
class DataStoreTokenStorage(private val context: Context) {

    companion object {
        val KEY_ACCESS_TOKEN = stringPreferencesKey("jwt_access_token")
        val KEY_REFRESH_TOKEN = stringPreferencesKey("jwt_refresh_token")
        val KEY_USER_ID = longPreferencesKey("auth_user_id")
        val KEY_USER_EMAIL = stringPreferencesKey("auth_user_email")
        val KEY_USER_ROLE = stringPreferencesKey("auth_user_role")
    }

    val sessionFlow: Flow<StoredAuthSession> = context.authDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            StoredAuthSession(
                accessToken = preferences[KEY_ACCESS_TOKEN],
                refreshToken = preferences[KEY_REFRESH_TOKEN],
                userId = preferences[KEY_USER_ID],
                email = preferences[KEY_USER_EMAIL],
                role = preferences[KEY_USER_ROLE]
            )
        }

    suspend fun saveTokens(tokens: AuthTokens, user: User) {
        context.authDataStore.edit { preferences ->
            preferences[KEY_ACCESS_TOKEN] = tokens.accessToken
            preferences[KEY_REFRESH_TOKEN] = tokens.refreshToken
            preferences[KEY_USER_ID] = user.id
            preferences[KEY_USER_EMAIL] = user.email
            preferences[KEY_USER_ROLE] = user.role.name
        }
    }

    suspend fun updateAccessToken(newAccessToken: String) {
        context.authDataStore.edit { preferences ->
            preferences[KEY_ACCESS_TOKEN] = newAccessToken
        }
    }

    suspend fun getAccessToken(): String? {
        val prefs = context.authDataStore.data.firstOrNull() ?: return null
        return prefs[KEY_ACCESS_TOKEN]
    }

    suspend fun getRefreshToken(): String? {
        val prefs = context.authDataStore.data.firstOrNull() ?: return null
        return prefs[KEY_REFRESH_TOKEN]
    }

    suspend fun getSavedUserId(): Long? {
        val prefs = context.authDataStore.data.firstOrNull() ?: return null
        return prefs[KEY_USER_ID]
    }

    suspend fun clearSession() {
        context.authDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
