package com.tripflow.android.data.session

import android.content.Context
import com.tripflow.android.data.local.TripFlowDatabase
import com.tripflow.android.data.local.entity.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * Gestor central de sesión de usuario en base de datos local (Room + SharedPreferences).
 * Provee métodos obligatorios de autenticación: estado del usuario actual, login y logout.
 */
class UserSessionManager private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val userDao = TripFlowDatabase.getDatabase(appContext).userDao()

    private val _activeUserId = MutableStateFlow(prefs.getLong(KEY_ACTIVE_USER_ID, -1L))
    val activeUserId: StateFlow<Long> = _activeUserId.asStateFlow()

    suspend fun getCurrentUser(): UserEntity? = withContext(Dispatchers.IO) {
        val uid = _activeUserId.value
        if (uid > 0) userDao.getUserById(uid) else null
    }

    suspend fun login(userId: Long) = withContext(Dispatchers.IO) {
        prefs.edit().putLong(KEY_ACTIVE_USER_ID, userId).apply()
        _activeUserId.value = userId
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        prefs.edit().remove(KEY_ACTIVE_USER_ID).apply()
        _activeUserId.value = -1L
    }

    fun isUserLoggedIn(): Boolean {
        return prefs.getLong(KEY_ACTIVE_USER_ID, -1L) > 0
    }

    companion object {
        private const val PREFS_NAME = "tripflow_session_prefs"
        private const val KEY_ACTIVE_USER_ID = "active_user_id"

        @Volatile
        private var INSTANCE: UserSessionManager? = null

        fun getInstance(context: Context): UserSessionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserSessionManager(context).also { INSTANCE = it }
            }
        }
    }
}
