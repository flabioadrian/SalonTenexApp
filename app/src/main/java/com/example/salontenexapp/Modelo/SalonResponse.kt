package com.example.salontenexapp.Modelo

import com.google.gson.annotations.SerializedName

data class SalonResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("id") val id: Int? = null,
    val error: String? = null
)