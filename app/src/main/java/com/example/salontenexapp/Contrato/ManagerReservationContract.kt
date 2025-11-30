package com.example.salontenexapp.Contrato

import com.example.salontenexapp.data.Reservation
import retrofit2.Response

interface ManagerReservationContract {
    interface ManageReservationsView {
        fun onReservationsLoaded(reservations: Response<List<Reservation>>)
        fun onFilteredReservationsLoaded(reservations: List<Reservation>)
        fun showLoading()
        fun hideLoading()
        fun showError(message: String)
        fun showSessionExpired()
    }

    interface ManageReservationsPresenter {
        fun loadReservations()
        fun filterReservations(filter: String)
        fun refreshReservations()
        fun getOriginalList(): List<Reservation>?
    }
}