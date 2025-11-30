package com.example.salontenexapp.Modelo

import com.google.gson.annotations.SerializedName

data class ServicioRequest(
    @SerializedName("nombre_servicio") val nombreServicio: String,
    @SerializedName("costo") val costo: Double,
    @SerializedName("descripcion") val descripcion: String
)
