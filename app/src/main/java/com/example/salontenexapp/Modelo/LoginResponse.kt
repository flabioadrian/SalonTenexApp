package com.example.salontenexapp.Modelo

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val user_type: String?,
    val user_data: User?
)