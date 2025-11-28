// ManageReservationsPresenter.kt
package com.example.salontenexapp.Presentador

import com.example.salontenexapp.Contrato.ManagerReservationContract
import com.example.salontenexapp.data.Reservation
import com.example.salontenexapp.data.api.APIService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManageReservationsPresenter(
    private val view: ManagerReservationContract.ManageReservationsView,
    private val apiService: APIService
) {

    private var allReservations: List<Reservation> = emptyList()

    // En tu ManageReservationsPresenter
    fun loadReservations() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                view.showLoading()
                val reservations = apiService.getRecentReservations()
                allReservations = reservations

                withContext(Dispatchers.Main) {
                    view.hideLoading()
                    view.onReservationsLoaded(reservations)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view.hideLoading()

                    when {
                        e is retrofit2.HttpException && e.code() == 401 -> {
                            view.showSessionExpired()
                        }
                        else -> {
                            view.showError("Error al cargar reservas: ${e.message}")
                        }
                    }
                }
            }
        }
    }

    fun filterReservations(status: String) {
        val filtered = when (status) {
            "Pendientes" -> allReservations.filter { it.status.equals("Pendiente", ignoreCase = true) }
            "Confirmadas" -> allReservations.filter { it.status.equals("Confirmada", ignoreCase = true) }
            "Canceladas" -> allReservations.filter { it.status.equals("Cancelada", ignoreCase = true) }
            else -> allReservations
        }
        view.onReservationsLoaded(filtered)
    }

    fun refreshReservations() {
        loadReservations()
    }
}