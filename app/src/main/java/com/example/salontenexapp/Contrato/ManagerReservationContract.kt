package com.example.salontenexapp.Contrato

import com.example.salontenexapp.data.Reservation

interface ManagerReservationContract {
    interface ManageReservationsView {
        fun onReservationsLoaded(reservations: List<Reservation>)
        fun showLoading()
        fun hideLoading()
        fun showError(message: String)
        fun showSessionExpired()
    }
}