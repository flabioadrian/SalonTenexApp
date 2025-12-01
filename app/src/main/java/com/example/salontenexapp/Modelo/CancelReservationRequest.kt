package com.example.salontenexapp.Modelo

import com.google.gson.annotations.SerializedName

data class CancelReservationRequest(
    @SerializedName("id_reserva") val idReserva: Int,
    @SerializedName("estado") val estado: String = "Cancelada"
)
