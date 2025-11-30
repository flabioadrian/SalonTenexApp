package com.example.salontenexapp.Modelo

data class ChangePasswordRequest(
    val user_id: Int,
    val user_type: String,
    val current_password: String,
    val new_password: String
)
