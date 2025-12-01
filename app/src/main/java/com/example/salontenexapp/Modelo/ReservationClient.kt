package com.example.salontenexapp.Modelo

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReservationClient(
    @SerializedName("id_reserva") val id: Int,
    @SerializedName("id_usuario") val idUsuario: Int,
    @SerializedName("id_sala") val idSala: Int,
    @SerializedName("fecha") val fecha: String,
    @SerializedName("hora_inicio") val horaInicio: String,
    @SerializedName("hora_fin") val horaFin: String,
    @SerializedName("estado") val estado: String,
    @SerializedName("total_pagar") val totalPagar: Double,
    @SerializedName("id_servicio_fk") val idServicio: Int?,

    @SerializedName("nombre_sala") val nombreSala: String?,
    @SerializedName("nombre_servicio") val nombreServicio: String?,
    @SerializedName("precio_sala") val precioSala: Double?,
    @SerializedName("capacidad_sala") val capacidadSala: Int?,
    @SerializedName("descripcion_sala") val descripcionSala: String?
) : Parcelable
