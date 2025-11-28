package com.example.salontenexapp.Modelo

import com.google.gson.annotations.SerializedName

data class SalonResponse(
    val success: Boolean,
    val message: String,
    val id: Int? = null
)