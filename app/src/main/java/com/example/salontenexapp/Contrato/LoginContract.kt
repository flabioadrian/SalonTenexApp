package com.example.salontenexapp.Contrato

interface LoginContract {
    interface View {
        fun showError(message: String)
        fun onLoginSuccess(userType: String)
        fun showProgress()
        fun hideProgress()
    }

    interface Presenter {
        fun login(email: String, password: String)
        fun validateFields(email: String, password: String): Boolean
        fun onDestroy()
    }
}