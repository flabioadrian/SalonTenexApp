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
import com.example.salontenexapp.data.repository.AuthRepository
import com.example.salontenexapp.util.SharedPreferencesManager

class LoginActivity : AppCompatActivity(), LoginContract.View {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView

    private var presenter: LoginContract.Presenter? = null // Cambiar a nullable

    private lateinit var prefsManager: SharedPreferencesManager

    companion object {
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate iniciado")

        try {
            // Inicializar primero
            prefsManager = SharedPreferencesManager(this)
            Log.d(TAG, "SharedPreferencesManager inicializado")

            // Verificar si ya hay sesión activa
            if (prefsManager.isLoggedIn()) {
                Log.d(TAG, "Usuario ya logueado, redirigiendo a MainActivity")
                val userType = prefsManager.getUserType() ?: "client"
                redirectToMainActivity(userType)
                return
            }

            Log.d(TAG, "Mostrando UI de login")
            setContentView(R.layout.activity_login)

            initializeViews()
            setupPresenter()
            setupClickListeners()

            Log.d(TAG, "LoginActivity configurada exitosamente")

        } catch (e: Exception) {
            Log.e(TAG, "ERROR CRÍTICO en onCreate: ${e.message}", e)
            e.printStackTrace()
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
            Log.d(TAG, "Intentando login con email: $email")
            presenter?.login(email, password) // Usar safe call
        }
    }

    override fun showError(message: String) {
        Log.e(TAG, "Error de login: $message")
        runOnUiThread {
            tvError.text = message
            tvError.visibility = TextView.VISIBLE
        }
    }

    override fun onLoginSuccess(userType: String) {
        Log.d(TAG, "Login exitoso, redirigiendo a MainActivity con userType: $userType")
        runOnUiThread {
            tvError.visibility = TextView.GONE
            redirectToMainActivity(userType)
        }
    }

    private fun redirectToMainActivity(userType: String) {
        try {
            Log.d(TAG, "Creando Intent para MainActivity")
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("USER_TYPE", userType)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            Log.d(TAG, "Iniciando MainActivity")
            startActivity(intent)
            Log.d(TAG, "Finalizando LoginActivity")
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "ERROR al redirigir a MainActivity: ${e.message}", e)
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
        presenter?.onDestroy() // Safe call
    }
}