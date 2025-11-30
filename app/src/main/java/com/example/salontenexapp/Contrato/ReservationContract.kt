package com.example.salontenexapp.Contrato

import com.example.salontenexapp.Modelo.Client
import com.example.salontenexapp.data.Salon
import com.example.salontenexapp.Modelo.Servicio

interface ReservationContract {

    interface CreateReservationView {
        fun onClientsLoaded(clients: List<Client>)
        fun onSalonsLoaded(salons: List<Salon>)
        fun showLoading()
        fun hideLoading()
        fun showError(message: String)
        fun onServiciosLoaded(servicios: List<Servicio>)
        fun onReservationCreated()
        fun onReservationCreationFailed(error: String)
    }

    interface CreateReservationPresenter {
        fun loadInitialData()
        fun getClientByPosition(position: Int): Client?
        fun getSalonByPosition(position: Int): Salon?
        fun getServicioByPosition(position: Int): Servicio?
        fun createReservation(client: Client, salon: Salon, date: String, startTime: String, endTime: String, service: String)
    }
}