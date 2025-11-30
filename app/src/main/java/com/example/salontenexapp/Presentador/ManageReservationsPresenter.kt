package com.example.salontenexapp.Presentador

import com.example.salontenexapp.Contrato.ManagerReservationContract
import com.example.salontenexapp.data.Reservation
import com.example.salontenexapp.data.api.APIService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class ManageReservationsPresenter(
    private val view: ManagerReservationContract.ManageReservationsView,
    private val apiService: APIService
) : ManagerReservationContract.ManageReservationsPresenter {

    private var originalReservations: List<Reservation> = emptyList()
    private var currentFilter: String = "Todas"

    override fun loadReservations() {
        view.showLoading()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getReservations()
                withContext(Dispatchers.Main) {
                    view.hideLoading()
                    if (response.isSuccessful) {
                        originalReservations = response.body() ?: emptyList()
                        view.onReservationsLoaded(response)

                        if (currentFilter != "Todas") {
                            filterReservations(currentFilter)
                        }
                    } else {
                        view.showError("Error al cargar las reservaciones: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view.hideLoading()
                    view.showError("Error de conexión: ${e.message}")
                }
            }
        }
    }

    override fun filterReservations(filter: String) {
        currentFilter = filter

        if (filter == "Todas") {
            view.onFilteredReservationsLoaded(originalReservations)
            return
        }

        val filteredList = when (filter) {
            "Pendientes" -> originalReservations.filter { it.status.equals("pendiente", ignoreCase = true) }
            "Confirmadas" -> originalReservations.filter { it.status.equals("confirmada", ignoreCase = true) }
            "Canceladas" -> originalReservations.filter { it.status.equals("cancelada", ignoreCase = true) }
            else -> originalReservations
        }

        view.onFilteredReservationsLoaded(filteredList)
    }

    override fun refreshReservations() {
        loadReservations()
    }

    override fun getOriginalList(): List<Reservation>? {
        return originalReservations
    }
}