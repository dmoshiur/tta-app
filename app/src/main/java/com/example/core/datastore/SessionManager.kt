package com.example.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "thinktank_session")

class SessionManager(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_AVATAR_KEY = stringPreferencesKey("user_avatar")

        @Volatile
        private var INSTANCE: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return INSTANCE ?: synchronized(this) {
                val instance = SessionManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    val authToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    val userId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

    val userName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY]
    }

    val userEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_EMAIL_KEY]
    }

    val userAvatar: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_AVATAR_KEY]
    }

    suspend fun saveSession(token: String, id: String, name: String, email: String, avatar: String = "") {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[USER_ID_KEY] = id
            preferences[USER_NAME_KEY] = name
            preferences[USER_EMAIL_KEY] = email
            preferences[USER_AVATAR_KEY] = avatar
        }
    }

    suspend fun updateProfile(name: String, avatar: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = name
            preferences[USER_AVATAR_KEY] = avatar
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(USER_ID_KEY)
            preferences.remove(USER_NAME_KEY)
            preferences.remove(USER_EMAIL_KEY)
            preferences.remove(USER_AVATAR_KEY)
        }
    }
}
