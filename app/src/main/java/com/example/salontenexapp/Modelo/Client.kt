package com.example.salontenexapp.Modelo

import com.google.gson.annotations.SerializedName
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Client(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellido_paterno") val apellidoPaterno: String,
    @SerializedName("apellido_materno") val apellidoMaterno: String,
    @SerializedName("email") val email: String,
    @SerializedName("telefono") val telefono: String,
    @SerializedName("nombre_completo") val nombreCompleto: String
) : Parcelable {
    override fun toString(): String {
        return nombre?.let { "$it ($email)" } ?: email
    }
}