package com.example.salontenexapp.data.repository

import com.example.salontenexapp.Modelo.LoginRequest
import com.example.salontenexapp.Modelo.LoginResponse
import com.example.salontenexapp.data.api.APIService
import com.example.salontenexapp.data.api.RetrofitClient

class AuthRepository {

    private val apiService = RetrofitClient.getService(APIService::class.java)

    suspend fun attemptLogin(email: String, password: String): Result<LoginResponse> {
        return try {
            val request = LoginRequest(email, password)

            val response = apiService.login(request)

            if (response.success) {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}