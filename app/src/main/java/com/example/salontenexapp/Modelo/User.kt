package com.example.salontenexapp.Modelo

data class User(
    val id: Int = 0,
    val email: String,
    val password: String,
    val name: String,
    val type: String, // "admin" o "client"
    val phone: String? = null,
    val createdAt: String? = null
)