package com.example.salontenexapp.Modelo

import com.google.gson.annotations.SerializedName

data class ServicioResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<Servicio>?
)