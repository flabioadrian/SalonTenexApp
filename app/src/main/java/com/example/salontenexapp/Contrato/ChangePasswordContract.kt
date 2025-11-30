package com.example.salontenexapp.Contrato

interface ChangePasswordContract {
    interface View {
        fun showLoading()
        fun hideLoading()
        fun showError(message: String)
        fun onPasswordChangeSuccess(message: String)
    }

    interface Presenter {
        suspend fun changePassword(
            userId: Int,
            userType: String,
            currentPassword: String,
            newPassword: String,
            confirmPassword: String
        )
    }
}