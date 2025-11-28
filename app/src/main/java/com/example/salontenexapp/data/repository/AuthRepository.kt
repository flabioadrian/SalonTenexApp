// AuthRepository.kt
package com.example.salontenexapp.data.repository

import com.example.salontenexapp.Modelo.LoginRequest
import com.example.salontenexapp.Modelo.LoginResponse
import com.example.salontenexapp.data.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class AuthRepository {

    private val apiService = RetrofitClient.apiService

    suspend fun attemptLogin(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))

            if (response.success) {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message ?: "Error de login"))
            }
        } catch (e: HttpException) {
            Result.failure(Exception("Error HTTP: ${e.code()} - ${e.message()}"))
        } catch (e: IOException) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }

    // Verificar si hay sesión activa
    fun hasActiveSession(): Boolean {
        return RetrofitClient.hasSession()
    }

    // Cerrar sesión
    fun logout() {
        RetrofitClient.clearCookies()
    }
}