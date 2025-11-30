package com.example.salontenexapp.util

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesManager(context: Context) {

    private val PREFS_NAME = "SalonTenexPrefs"
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val KEY_USER_LOGGED_IN = "is_logged_in"
    private val KEY_USER_TYPE = "user_type"
    private val KEY_USER_EMAIL = "user_email"
    private val KEY_USER_ID = "user_id"
    private val KEY_USER_NAME = "user_name"

    private val KEY_SESSION_COOKIE = "session_cookie"
    private val KEY_SESSION_EXPIRY = "session_expiry"

    fun saveUserData(isLoggedIn: Boolean, userType: String, userId: Int, email: String, name: String) {
        prefs.edit().apply {
            putBoolean(KEY_USER_LOGGED_IN, isLoggedIn)
            putString(KEY_USER_TYPE, userType)
            putInt(KEY_USER_ID, userId)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_NAME, name)
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_USER_LOGGED_IN, false)
    }

    fun getUserType(): String? {
        return prefs.getString(KEY_USER_TYPE, null)
    }

    fun getUserId(): Int {
        return prefs.getInt(KEY_USER_ID, -1)
    }

    fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }

    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    fun hasCompleteUserData(): Boolean {
        return isLoggedIn() &&
                getUserType() != null &&
                getUserId() != -1 &&
                getUserEmail() != null
    }

    fun saveSessionCookie(cookie: String, expiryTime: Long = System.currentTimeMillis() + 24 * 60 * 60 * 1000) {
        prefs.edit().apply {
            putString(KEY_SESSION_COOKIE, cookie)
            putLong(KEY_SESSION_EXPIRY, expiryTime)
            apply()
        }
    }

    fun getSessionCookie(): String? {
        val expiry = prefs.getLong(KEY_SESSION_EXPIRY, 0)
        return if (System.currentTimeMillis() < expiry) {
            prefs.getString(KEY_SESSION_COOKIE, null)
        } else {
            clearSessionCookie()
            null
        }
    }
    fun hasValidSessionCookie(): Boolean {
        return getSessionCookie() != null
    }

    fun clearSessionCookie() {
        prefs.edit().apply {
            remove(KEY_SESSION_COOKIE)
            remove(KEY_SESSION_EXPIRY)
            apply()
        }
    }

    fun getSessionTimeRemaining(): Long {
        val expiry = prefs.getLong(KEY_SESSION_EXPIRY, 0)
        return expiry - System.currentTimeMillis()
    }

    fun logout() {
        prefs.edit().clear().apply()
    }

    fun hasValidSession(): Boolean {
        return isLoggedIn() && hasValidSessionCookie()
    }

    fun getSessionInfo(): String {
        return "LoggedIn: ${isLoggedIn()}, " +
                "UserType: ${getUserType()}, " +
                "UserId: ${getUserId()}, " +
                "Email: ${getUserEmail()}, " +
                "HasCookie: ${hasValidSessionCookie()}, " +
                "TimeRemaining: ${getSessionTimeRemaining() / 1000 / 60} minutes"
    }
}