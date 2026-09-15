package com.example.khedmati.util

import android.content.Context

class AppPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("khedmati_prefs", Context.MODE_PRIVATE)

    var language: String
        get() = prefs.getString("language", "en") ?: "en"
        set(value) = prefs.edit().putString("language", value).apply()

    var languageChosen: Boolean
        get() = prefs.getBoolean("language_chosen", false)
        set(value) = prefs.edit().putBoolean("language_chosen", value).apply()

    fun saveSession(name: String, email: String) {
        prefs.edit()
            .putString("session_name", name)
            .putString("session_email", email)
            .remove("session_role")
            .apply()
    }

    fun clearSession() {
        prefs.edit()
            .remove("session_name")
            .remove("session_email")
            .remove("session_role")
            .apply()
    }

    fun sessionName(): String? = prefs.getString("session_name", null)
    fun sessionEmail(): String? = prefs.getString("session_email", null)
}
