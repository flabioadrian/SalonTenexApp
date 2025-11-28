package com.example.salontenexapp.Modelo

import com.example.salontenexapp.data.Reservation
import com.example.salontenexapp.data.Salon

interface ReservationAPI {

    fun createReservation(reservation: Reservation, callback: (success: Boolean, message: String) -> Unit)

    fun updateReservation(reservation: Reservation, callback: (success: Boolean, message: String) -> Unit)

    fun getClients(callback: (List<Client>) -> Unit)
    fun getSalons(callback: (List<Salon>) -> Unit)
    fun getServices(callback: (List<Service>) -> Unit)
}