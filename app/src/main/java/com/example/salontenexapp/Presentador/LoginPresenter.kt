package com.example.salontenexapp.Presentador

import com.example.salontenexapp.Contrato.LoginContract
import com.example.salontenexapp.data.repository.AuthRepository
import com.example.salontenexapp.util.SharedPreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class LoginPresenter(
    private val view: LoginContract.View,
    private val repository: AuthRepository,
    private val prefsManager: SharedPreferencesManager
) : LoginContract.Presenter {

    private val job = Job()
    private val presenterScope = CoroutineScope(Dispatchers.Main + job)

    override fun login(email: String, password: String) {
        if (validateFields(email, password)) {
            view.showError("Por favor complete todos los campos")
            return
        }

        presenterScope.launch {
            view.showProgress()

            val result = repository.attemptLogin(email, password)

            view.hideProgress()

            result.onSuccess { response ->
                val userType = response.user_type ?: "client"
                val userData = response.user_data

                if (userData != null) {
                    prefsManager.saveUserData(
                        isLoggedIn = true,
                        userType = userType,
                        userId = userData.id ?: -1,
                        email = userData.email ?: email,
                        name = userData.name ?: ""
                    )
                } else {
                    prefsManager.saveUserData(
                        isLoggedIn = true,
                        userType = userType,
                        userId = -1,
                        email = email,
                        name = ""
                    )
                }

                view.onLoginSuccess(userType)
            }.onFailure { exception ->
                view.showError(exception.message ?: "Error desconocido en el login.")
            }
        }
    }

    override fun validateFields(email: String, password: String): Boolean {
        return email.isEmpty() && password.isEmpty()
    }

    override fun onDestroy() {
        job.cancel()
    }
}