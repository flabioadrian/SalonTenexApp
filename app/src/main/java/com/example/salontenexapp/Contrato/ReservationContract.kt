package com.example.salontenexapp.Contrato

import com.example.salontenexapp.data.Reservation
import com.example.salontenexapp.Modelo.Client // Asumo estas clases de modelo
import com.example.salontenexapp.data.Salon
import com.example.salontenexapp.Modelo.Service

interface ReservationContract {

    interface View {
        fun showLoading(show: Boolean)
        fun showData(clients: List<Client>, salons: List<Salon>, services: List<Service>)
        fun showMessage(message: String)
        fun dismissDialog(reservation: Reservation?)
        fun setFormMode(isEdit: Boolean, reservation: Reservation?)
        fun populateForm(reservation: Reservation)
        fun getFormData(): Map<String, String>
        fun isFormValid(): Boolean
    }

    interface Presenter {
        fun attachView(view: View, reservationToEdit: Reservation?)
        fun detachView()
        fun loadInitialData() // Clientes, Salones, Servicios
        fun processReservationAction() // Crear o Editar
        fun dateSelected(date: String)
    }

    interface CreateReservationView {
        fun onClientsLoaded(clients: List<Client>)
        fun onSalonsLoaded(salons: List<Salon>)
        fun showLoading()
        fun hideLoading()
        fun showError(message: String)
    }
}