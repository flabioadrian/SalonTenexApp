package com.example.salontenexapp.Modelo

data class ReservationRequest(
    val email_cliente: String,
    val nombre_sala: String,
    val fecha: String,
    val hora_inicio: String,
    val hora_fin: String,
    val estado: String,
    val nombre_servicio: String? = null
)