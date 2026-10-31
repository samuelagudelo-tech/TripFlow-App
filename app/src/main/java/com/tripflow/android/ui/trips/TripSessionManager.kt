package com.tripflow.android.ui.trips

import android.content.Context
import com.tripflow.android.data.local.TripFlowDatabase
import com.tripflow.android.data.local.entity.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Gestor de sesión ligero para el Módulo de Viajes.
 * Garantiza que las operaciones locales en Room siempre cuenten con un usuario válido
 * y nunca fallen por restricciones de clave foránea (ForeignKey: users.id).
 */
object TripSessionManager {

    private const val PREFS_NAME = "tripflow_session_prefs"
    private const val KEY_ACTIVE_USER_ID = "active_user_id"

    /**
     * Obtiene el ID del usuario actualmente autenticado o asegura la existencia
     * de uno en la base de datos local Room.
     */
    suspend fun getActiveUserId(context: Context): Long = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedUserId = prefs.getLong(KEY_ACTIVE_USER_ID, -1L)

        val db = TripFlowDatabase.getDatabase(context)
        val userDao = db.userDao()

        // 1. Si ya tenemos un ID en caché y existe en la base de datos, lo retornamos
        if (savedUserId > 0) {
            val user = userDao.getUserById(savedUserId)
            if (user != null) {
                return@withContext savedUserId
            }
        }

        // 2. Si no hay ID en caché, verificar si ya existe el usuario principal (id = 1)
        val defaultUser = userDao.getUserById(1L)
        if (defaultUser != null) {
            prefs.edit().putLong(KEY_ACTIVE_USER_ID, defaultUser.id).apply()
            return@withContext defaultUser.id
        }

        // 3. Verificar si existe algún usuario por email
        val existingDemo = userDao.getUserByEmail("usuario@tripflow.com")
        if (existingDemo != null) {
            prefs.edit().putLong(KEY_ACTIVE_USER_ID, existingDemo.id).apply()
            return@withContext existingDemo.id
        }

        // 4. Crear un usuario inicial para evitar fallos de clave foránea
        val newUser = UserEntity(
            fullName = "Viajero TripFlow",
            email = "usuario@tripflow.com",
            passwordHash = "local_demo_hash"
        )
        val generatedId = userDao.insertUser(newUser)
        prefs.edit().putLong(KEY_ACTIVE_USER_ID, generatedId).apply()
        generatedId
    }
}
