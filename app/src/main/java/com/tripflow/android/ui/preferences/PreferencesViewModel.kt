package com.tripflow.android.ui.preferences

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tripflow.android.data.preferences.UserPreferencesManager
import com.tripflow.android.data.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PreferencesViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = UserPreferencesManager.getInstance(application)

    val isDarkMode: StateFlow<Boolean> = preferencesManager.isDarkMode

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    init {
        loadCurrentUser()
    }

    fun loadCurrentUser() {
        try {
            val user = SupabaseClient.client.auth.currentUserOrNull()
            _userEmail.value = user?.email
        } catch (_: Exception) {
            _userEmail.value = null
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        preferencesManager.setDarkMode(enabled)
    }

    fun signOut(onSignedOut: () -> Unit) {
        viewModelScope.launch {
            try {
                SupabaseClient.client.auth.signOut()
            } catch (_: Exception) {
                // Si falla o no hay conexión, limpiamos el estado local igualmente
            }
            _userEmail.value = null
            onSignedOut()
        }
    }
}
