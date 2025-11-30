package com.example.salontenexapp.Presentador

import com.example.salontenexapp.Contrato.ChangePasswordContract
import com.example.salontenexapp.Modelo.ChangePasswordRequest
import com.example.salontenexapp.data.api.APIService

class ChangePasswordPresenter(
    private val view: ChangePasswordContract.View,
    private val apiService: APIService
) : ChangePasswordContract.Presenter {

    override suspend fun changePassword(
        userId: Int,
        userType: String,
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ) {
        // Validaciones locales
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            view.showError("Todos los campos son obligatorios")
            return
        }

        if (newPassword != confirmPassword) {
            view.showError("Las contraseñas no coinciden")
            return
        }

        if (newPassword.length < 6) {
            view.showError("La contraseña debe tener al menos 6 caracteres")
            return
        }

        if (!isPasswordValid(newPassword)) {
            view.showError("La contraseña debe contener al menos una mayúscula, un número y un carácter especial")
            return
        }

        try {
            view.showLoading()

            val request = ChangePasswordRequest(
                user_id = userId,
                user_type = userType,
                current_password = currentPassword,
                new_password = newPassword
            )

            val response = apiService.changePassword(request)

            if (response.success) {
                view.hideLoading()
                view.onPasswordChangeSuccess(response.message)
            } else {
                view.hideLoading()
                val errorMessage = when (response.error_code) {
                    "WRONG_CURRENT_PASSWORD" -> "La contraseña actual es incorrecta"
                    "WEAK_PASSWORD" -> "La nueva contraseña no cumple con los requisitos de seguridad"
                    "USER_NOT_FOUND" -> "Usuario no encontrado"
                    else -> response.message ?: "Error al cambiar la contraseña"
                }

                view.showError(errorMessage)
            }
        } catch (e: Exception) {
            view.hideLoading()
            view.showError("Error de conexión: ${e.message ?: "Intente nuevamente"}")
        }
    }

    private fun isPasswordValid(password: String): Boolean {
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }

        return password.length >= 8 && hasUpperCase && hasDigit && hasSpecialChar
    }
}