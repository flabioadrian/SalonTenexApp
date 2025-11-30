package com.example.salontenexapp.Modelo

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Servicio(
    @SerializedName("id_servicio") val idServicio: Int,
    @SerializedName("nombre_servicio") val nombreServicio: String,
    @SerializedName("costo") val costo: Double,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("estado") val estado: String
) : Parcelable