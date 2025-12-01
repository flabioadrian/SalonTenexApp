package com.example.salontenexapp.Modelo

import com.google.gson.annotations.SerializedName

data class EditReservationRequest(
    @SerializedName("id_reserva") val idReserva: Int,
    @SerializedName("id_sala") val idSala: Int,
    @SerializedName("fecha") val fecha: String,
    @SerializedName("hora_inicio") val horaInicio: String,
    @SerializedName("hora_fin") val horaFin: String,
    @SerializedName("total_pagar") val totalPagar: Double,
    @SerializedName("id_servicio_fk") val idServicio: Int?
)
