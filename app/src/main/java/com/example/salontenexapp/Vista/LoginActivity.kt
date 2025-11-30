package com.example.salontenexapp.Vista

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.salontenexapp.R
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import com.example.salontenexapp.Contrato.LoginContract
import com.example.salontenexapp.Presentador.LoginPresenter
import com.example.salontenexapp.data.api.RetrofitClient
import com.example.salontenexapp.data.repository.AuthRepository
import com.example.salontenexapp.util.SharedPreferencesManager

class LoginActivity : AppCompatActivity(), LoginContract.View {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView

    private var presenter: LoginContract.Presenter? = null

    private lateinit var prefsManager: SharedPreferencesManager

    companion object {
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            prefsManager = SharedPreferencesManager(this)

            restoreSession()

            if (prefsManager.isLoggedIn() && prefsManager.getSessionCookie() != null) {
                val userType = prefsManager.getUserType() ?: "client"
                redirectToMainActivity(userType)
                return
            } else if (prefsManager.isLoggedIn()) {
                prefsManager.logout()
                RetrofitClient.clearSession()
            }
            setContentView(R.layout.activity_login)

            initializeViews()
            setupPresenter()
            setupClickListeners()
            setupCookieListener()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun restoreSession() {
        val savedCookie = prefsManager.getSessionCookie()
        if (savedCookie != null) {
            RetrofitClient.restoreSession(savedCookie)
        } else {
            RetrofitClient.clearSession()
        }
    }

    private fun setupCookieListener() {
        RetrofitClient.setOnNewCookieListener { newCookie ->
            Log.d(TAG, "Guardando nueva cookie de sesión en SharedPreferences")
            prefsManager.saveSessionCookie(newCookie)
        }
    }

    private fun initializeViews() {
        try {
            etEmail = findViewById(R.id.etEmail)
            etPassword = findViewById(R.id.etPassword)
            btnLogin = findViewById(R.id.btnLogin)
            progressBar = findViewById(R.id.progressBar)
            tvError = findViewById(R.id.tvError)
            Log.d(TAG, "Vistas inicializadas")
        } catch (e: Exception) {
            Log.e(TAG, "Error inicializando vistas: ${e.message}", e)
            throw e
        }
    }

    private fun setupPresenter() {
        try {
            val authRepository = AuthRepository()
            presenter = LoginPresenter(this, authRepository, prefsManager)
            Log.d(TAG, "Presenter configurado")
        } catch (e: Exception) {
            Log.e(TAG, "Error configurando presenter: ${e.message}", e)
            throw e
        }
    }

    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            presenter?.login(email, password)
        }
    }

    override fun showError(message: String) {
        runOnUiThread {
            tvError.text = message
            tvError.visibility = TextView.VISIBLE
        }
    }

    override fun onLoginSuccess(userType: String) {
        runOnUiThread {
            tvError.visibility = TextView.GONE
            redirectToMainActivity(userType)
        }
    }

    private fun redirectToMainActivity(userType: String) {
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("USER_TYPE", userType)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                tvError.text = "Error al iniciar aplicación: ${e.message}"
                tvError.visibility = TextView.VISIBLE
            }
        }
    }

    override fun showProgress() {
        runOnUiThread {
            progressBar.visibility = ProgressBar.VISIBLE
            btnLogin.isEnabled = false
            btnLogin.alpha = 0.5f
        }
    }

    override fun hideProgress() {
        runOnUiThread {
            progressBar.visibility = ProgressBar.GONE
            btnLogin.isEnabled = true
            btnLogin.alpha = 1.0f
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter?.onDestroy()
    }
}