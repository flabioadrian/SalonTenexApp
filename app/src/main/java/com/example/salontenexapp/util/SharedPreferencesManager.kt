package com.example.salontenexapp.util
import android.content.Context
import android.content.SharedPreferences
import com.example.salontenexapp.Vista.MainActivity

class SharedPreferencesManager(context: Context) {

    private val PREFS_NAME = "SalonTenexPrefs"
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val KEY_USER_LOGGED_IN = "is_logged_in"
    private val KEY_USER_TYPE = "user_type"
    private val KEY_USER_EMAIL = "user_email"

    fun saveLoginStatus(isLoggedIn: Boolean, userType: String) {
        prefs.edit().apply {
            putBoolean(KEY_USER_LOGGED_IN, isLoggedIn)
            putString(KEY_USER_TYPE, userType)
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_USER_LOGGED_IN, false)
    }

    fun getUserType(): String? {
        return prefs.getString(KEY_USER_TYPE, null)
    }


    fun logout() {
        prefs.edit().apply {
            clear()
            apply()
        }
    }

    fun getUserEmail() : String? {
        val userEmail = prefs.getString(KEY_USER_EMAIL, null)
        return userEmail
    }

    fun saveUserEmail(email: String) {
        prefs.edit().apply {
            putString(KEY_USER_EMAIL, email)
            apply()
        }
    }
}