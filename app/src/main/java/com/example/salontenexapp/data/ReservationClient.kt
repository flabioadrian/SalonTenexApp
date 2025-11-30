package com.example.salontenexapp.data

class ReservationClient (
    val id_reserva: Int,
    val nombre_sala: String,
    val fecha: String,
    val hora_inicio: String,
    val hora_fin: String,
    val nombre_servicio: String?,
    val estado: String,
    val total_pagar: Double
    )

    data class ApiResponse(
        val success: Boolean? = null,
        val message: String? = null,
        val error: String? = null
    )

    data class EditReservationRequest(
        val id_reserva: Int,
        val fecha: String
    )

    data class CancelReservationRequest(
        val id_reserva: Int
    )