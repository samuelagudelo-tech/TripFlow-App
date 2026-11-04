package com.tripflow.android.ui.preferences

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.tripflow.android.data.preferences.UserPreferencesManager
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel exclusivo de Preferencias de Usuario.
 * Solo gestiona ajustes de la app (apariencia, etc.).
 * La lógica de sesión (cerrar sesión) está en AuthViewModel.
 */
class PreferencesViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = UserPreferencesManager.getInstance(application)

    /** Estado del modo oscuro (persiste entre sesiones vía DataStore/SharedPreferences) */
    val isDarkMode: StateFlow<Boolean> = preferencesManager.isDarkMode

    /** Activa o desactiva el modo oscuro */
    fun toggleDarkMode(enabled: Boolean) {
        preferencesManager.setDarkMode(enabled)
    }
}
