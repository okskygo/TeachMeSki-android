package com.teachmeski.app.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.teachmeski.app.ui.component.ActiveRole
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RolePreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    suspend fun getLastActiveRole(userId: String): ActiveRole? {
        val key = stringPreferencesKey("last_active_role_$userId")
        val value = dataStore.data.map { it[key] }.first()
        return when (value) {
            "student" -> ActiveRole.Student
            "instructor" -> ActiveRole.Instructor
            else -> null
        }
    }

    suspend fun setLastActiveRole(userId: String, role: ActiveRole) {
        val key = stringPreferencesKey("last_active_role_$userId")
        dataStore.edit { prefs ->
            prefs[key] = when (role) {
                ActiveRole.Student -> "student"
                ActiveRole.Instructor -> "instructor"
            }
        }
    }
}
