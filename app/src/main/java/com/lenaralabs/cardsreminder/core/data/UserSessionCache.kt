package com.lenaralabs.cardsreminder.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.lenaralabs.cardsreminder.core.model.ApiUser
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class UserSessionCache(
    context: Context,
) {
    private val dataStore = context.dataStore
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun save(user: ApiUser, requiredTermsVersion: String, firebaseUid: String) {
        val payload = json.encodeToString(
            CachedUserSession(
                user = user,
                requiredTermsVersion = requiredTermsVersion,
                firebaseUid = firebaseUid,
            ),
        )
        dataStore.edit { preferences ->
            preferences[CACHE_KEY] = payload
        }
    }

    suspend fun load(firebaseUid: String): CachedUserSession? {
        val payload = dataStore.data.first()[CACHE_KEY] ?: return null
        return runCatching {
            json.decodeFromString<CachedUserSession>(payload)
        }.getOrNull()?.takeIf { it.firebaseUid == firebaseUid }
    }

    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.remove(CACHE_KEY)
        }
    }

    @Serializable
    data class CachedUserSession(
        val user: ApiUser,
        val requiredTermsVersion: String,
        val firebaseUid: String,
    )

    private companion object {
        val CACHE_KEY = stringPreferencesKey("cached_user_session")
    }
}
