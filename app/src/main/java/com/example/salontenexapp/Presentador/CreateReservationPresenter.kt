package com.example.salontenexapp.Presentador

import com.example.salontenexapp.Contrato.ReservationContract
import com.example.salontenexapp.Modelo.Client
import com.example.salontenexapp.data.Salon
import com.example.salontenexapp.data.api.APIService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateReservationPresenter(
    private val view: ReservationContract.CreateReservationView,
    private val apiService: APIService
) {

    private var clients: List<Client> = emptyList()
    private var salons: List<Salon> = emptyList()

    fun loadInitialData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Cargar clientes y salones en paralelo
                val clientsDeferred = withContext(Dispatchers.IO) { apiService.getClients() }
                val salonsDeferred = withContext(Dispatchers.IO) { apiService.getSalons() }

                clients = clientsDeferred
                salons = salonsDeferred

                withContext(Dispatchers.Main) {
                    view.onClientsLoaded(clients)
                    view.onSalonsLoaded(salons)
                    view.hideLoading()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view.hideLoading()
                    view.showError("Error al cargar datos: ${e.message}")
                }
            }
        }
    }

    fun getClients(): List<Client> = clients
    fun getSalons(): List<Salon> = salons

    fun getClientByPosition(position: Int): Client? {
        return clients.getOrNull(position)
    }

    fun getSalonByPosition(position: Int): Salon? {
        return salons.getOrNull(position)
    }
}