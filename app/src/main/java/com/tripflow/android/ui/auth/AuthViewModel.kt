package com.tripflow.android.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tripflow.android.data.local.TripFlowDatabase
import com.tripflow.android.data.local.entity.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    object Success : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val database = TripFlowDatabase.getDatabase(application)
    private val userDao = database.userDao()

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    /**
     * Inicia sesión verificando credenciales en la base de datos local (Room)
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
                val hashedPass = hashPassword(pass)
                val user = userDao.authenticate(trimmedEmail, hashedPass)
                    ?: userDao.authenticate(trimmedEmail, pass) // compatibilidad con texto plano

                if (user != null) {
                    _uiState.value = AuthUiState.Success
                    onSuccess()
                } else {
                    val existingUser = userDao.getUserByEmail(trimmedEmail)
                    if (existingUser != null) {
                        _uiState.value = AuthUiState.Error("Contraseña incorrecta. Verifica tus datos.")
                    } else {
                        _uiState.value = AuthUiState.Error("No existe una cuenta registrada con este correo.")
                    }
                }
            } catch (t: Throwable) {
                _uiState.value = AuthUiState.Error(t.localizedMessage ?: "Error al iniciar sesión")
            }
        }
    }

    /**
     * Registra un nuevo usuario en la base de datos local (Room)
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
                val existing = userDao.getUserByEmail(trimmedEmail)
                if (existing != null) {
                    _uiState.value = AuthUiState.Error("Ya existe una cuenta registrada con este correo electrónico.")
                    return@launch
                }

                val newUser = UserEntity(
                    fullName = trimmedName,
                    email = trimmedEmail,
                    passwordHash = hashPassword(pass)
                )
                val id = userDao.insertUser(newUser)
                if (id > 0) {
                    _uiState.value = AuthUiState.Success
                    onSuccess()
                } else {
                    _uiState.value = AuthUiState.Error("No se pudo registrar el usuario localmente.")
                }
            } catch (t: Throwable) {
                _uiState.value = AuthUiState.Error(t.localizedMessage ?: "Error al registrar el usuario")
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return emailRegex.matches(email)
    }

    private fun isValidPassword(password: String): Boolean {
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        return password.length >= 8 && hasLetter && hasDigit
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
