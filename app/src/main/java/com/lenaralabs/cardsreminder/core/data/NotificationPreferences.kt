package com.lenaralabs.cardsreminder.core.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first

class NotificationPreferences(
    private val context: Context,
) {
    suspend fun isEnabled(firebaseUid: String?): Boolean {
        if (firebaseUid.isNullOrBlank()) return false
        val preferences = context.dataStore.data.first()
        return preferences[enabledKey(firebaseUid)] ?: false
    }

    suspend fun setEnabled(firebaseUid: String?, enabled: Boolean) {
        if (firebaseUid.isNullOrBlank()) return
        context.dataStore.edit { preferences ->
            preferences[enabledKey(firebaseUid)] = enabled
        }
    }

    private fun enabledKey(firebaseUid: String) =
        booleanPreferencesKey("notifications_enabled.$firebaseUid")
}
