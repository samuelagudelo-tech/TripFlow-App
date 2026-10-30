package com.tripflow.android.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripflow.android.data.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    object Success : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    /**
     * Inicia sesión con correo y contraseña en Supabase Auth
     */
    fun signIn(
        email: String,
        pass: String,
        onSuccess: () -> Unit
    ) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty()) {
            _uiState.value = AuthUiState.Error("Por favor ingresa tu correo electrónico")
            return
        }
        if (!isValidEmail(trimmedEmail)) {
            _uiState.value = AuthUiState.Error("Ingresa un correo electrónico válido")
            return
        }
        if (pass.isEmpty()) {
            _uiState.value = AuthUiState.Error("Por favor ingresa tu contraseña")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                SupabaseClient.client.auth.signInWith(Email) {
                    this.email = trimmedEmail
                    this.password = pass
                }
                _uiState.value = AuthUiState.Success
                onSuccess()
            } catch (t: Throwable) {
                _uiState.value = AuthUiState.Error(mapAuthErrorMessage(t))
            }
        }
    }

    /**
     * Registra un nuevo usuario con Nombre Completo, Correo y Contraseña en Supabase Auth
     */
    fun signUp(
        fullName: String,
        email: String,
        pass: String,
        confirmPass: String,
        termsAccepted: Boolean,
        onSuccess: () -> Unit
    ) {
        val trimmedName = fullName.trim()
        val trimmedEmail = email.trim()

        if (trimmedName.isEmpty()) {
            _uiState.value = AuthUiState.Error("Por favor ingresa tu nombre completo")
            return
        }
        if (trimmedEmail.isEmpty()) {
            _uiState.value = AuthUiState.Error("Por favor ingresa tu correo electrónico")
            return
        }
        if (!isValidEmail(trimmedEmail)) {
            _uiState.value = AuthUiState.Error("Ingresa un correo electrónico válido")
            return
        }
        if (!isValidPassword(pass)) {
            _uiState.value = AuthUiState.Error("La contraseña debe tener al menos 8 caracteres, una letra y un número")
            return
        }
        if (pass != confirmPass) {
            _uiState.value = AuthUiState.Error("Las contraseñas no coinciden")
            return
        }
        if (!termsAccepted) {
            _uiState.value = AuthUiState.Error("Debes aceptar los Términos y la Política de privacidad")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                SupabaseClient.client.auth.signUpWith(Email) {
                    this.email = trimmedEmail
                    this.password = pass
                    data = buildJsonObject {
                        put("full_name", trimmedName)
                    }
                }
                _uiState.value = AuthUiState.Success
                onSuccess()
            } catch (t: Throwable) {
                _uiState.value = AuthUiState.Error(mapAuthErrorMessage(t))
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return emailRegex.matches(email)
    }

    private fun isValidPassword(password: String): Boolean {
        // Al menos 8 caracteres, al menos una letra y al menos un número
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        return password.length >= 8 && hasLetter && hasDigit
    }

    private fun mapAuthErrorMessage(throwable: Throwable): String {
        val msg = throwable.message.orEmpty()
        return when {
            msg.contains("Invalid login credentials", ignoreCase = true) ->
                "Correo o contraseña incorrectos. Verifica tus datos."
            msg.contains("User already registered", ignoreCase = true) ||
            msg.contains("already exists", ignoreCase = true) ->
                "Ya existe una cuenta registrada con este correo electrónico."
            msg.contains("Password should be at least", ignoreCase = true) ->
                "La contraseña no cumple con los requisitos mínimos de seguridad."
            msg.contains("Unable to resolve host", ignoreCase = true) ||
            msg.contains("ConnectException", ignoreCase = true) ||
            msg.contains("timeout", ignoreCase = true) ->
                "Error de conexión. Verifica tu conexión a internet."
            else ->
                throwable.localizedMessage ?: "Ocurrió un error al procesar tu solicitud."
        }
    }
}
