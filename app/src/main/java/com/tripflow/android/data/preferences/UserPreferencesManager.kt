package com.tripflow.android.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Gestor de preferencias de usuario (Apariencia, Moneda predeterminada y Alertas).
 */
class UserPreferencesManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean(KEY_DARK_MODE, false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _preferredCurrency = MutableStateFlow(prefs.getString(KEY_CURRENCY, "USD") ?: "USD")
    val preferredCurrency: StateFlow<String> = _preferredCurrency.asStateFlow()

    private val _overdraftAlerts = MutableStateFlow(prefs.getBoolean(KEY_OVERDRAFT_ALERTS, true))
    val overdraftAlerts: StateFlow<Boolean> = _overdraftAlerts.asStateFlow()

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    fun setPreferredCurrency(currency: String) {
        _preferredCurrency.value = currency
        prefs.edit().putString(KEY_CURRENCY, currency).apply()
    }

    fun setOverdraftAlerts(enabled: Boolean) {
        _overdraftAlerts.value = enabled
        prefs.edit().putBoolean(KEY_OVERDRAFT_ALERTS, enabled).apply()
    }

    companion object {
        private const val PREFS_NAME = "tripflow_user_preferences"
        private const val KEY_DARK_MODE = "dark_mode_enabled"
        private const val KEY_CURRENCY = "preferred_currency"
        private const val KEY_OVERDRAFT_ALERTS = "overdraft_alerts_enabled"

        @Volatile
        private var INSTANCE: UserPreferencesManager? = null

        fun getInstance(context: Context): UserPreferencesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserPreferencesManager(context).also { INSTANCE = it }
            }
        }
    }
}
