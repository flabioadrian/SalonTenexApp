package com.example.salontenexapp.Modelo

data class Service(
    val id: Int = 0,
    val name: String,
    val description: String,
    val price: Double,
    val available: Boolean = true
)