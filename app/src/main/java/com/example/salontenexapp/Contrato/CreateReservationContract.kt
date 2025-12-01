package com.example.salontenexapp.Contrato

import com.example.salontenexapp.Modelo.ReservationClient
import com.example.salontenexapp.Modelo.Servicio
import com.example.salontenexapp.data.Salon

interface CreateReservationContract {
    interface View {
        fun showLoading()
        fun hideLoading()
        fun showError(message: String)
        fun showSuccess(message: String)
        fun showSalaInfo(sala: Salon)
        fun hideSalaInfo()
        fun updateTotal(total: Double)
        fun setSalas(salas: List<Salon>)
        fun setServicios(servicios: List<Servicio>)
        fun setExistingReservation(reservation: ReservationClient)
        fun closeFragment()
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun loadInitialData()
        fun onSalaSelected(sala: Salon)
        fun onServicioSelected(servicio: Servicio?)
        fun onDateSelected(date: String)
        fun onStartTimeSelected(time: String)
        fun onEndTimeSelected(time: String)
        fun validateTimeRange(startTime: String, endTime: String): Boolean
        fun calculateTotal(sala: Salon?, servicio: Servicio?)
        fun validateForm(
            sala: Salon?,
            fecha: String,
            horaInicio: String,
            horaFin: String
        ): Boolean
        fun createReservation(
            sala: Salon,
            servicio: Servicio?,
            fecha: String,
            horaInicio: String,
            horaFin: String
        )
        fun updateReservation(
            reservation: ReservationClient,
            sala: Salon,
            servicio: Servicio?,
            fecha: String,
            horaInicio: String,
            horaFin: String
        )
        fun getExistingReservation(): ReservationClient?
        fun isEditMode(): Boolean
    }
}