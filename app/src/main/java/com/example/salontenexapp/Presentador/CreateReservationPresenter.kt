package com.example.salontenexapp.Presentador

import com.example.salontenexapp.Contrato.ReservationContract
import com.example.salontenexapp.Modelo.Client
import com.example.salontenexapp.Modelo.ReservationRequest
import com.example.salontenexapp.Modelo.SalonResponse
import com.example.salontenexapp.Modelo.Servicio
import com.example.salontenexapp.data.Salon
import com.example.salontenexapp.data.api.APIService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class CreateReservationPresenter(
    private val view: ReservationContract.CreateReservationView,
    private val apiService: APIService
) : ReservationContract.CreateReservationPresenter {

    private var clients: List<Client> = emptyList()
    private var salons: List<Salon> = emptyList()
    private var servicios: List<Servicio> = emptyList()

    override fun loadInitialData() {
        view.showLoading()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val clientsResponse = apiService.getClients()
                val salonsResponse = apiService.getSalons()

                val serviciosResponse = apiService.getServicios()

                clients = clientsResponse
                salons = salonsResponse

                servicios = if (serviciosResponse.isSuccessful) {
                    val responseBody = serviciosResponse.body()
                    if (responseBody?.success == true) {
                        responseBody.data ?: emptyList()
                    } else {
                        emptyList()
                    }
                } else {
                    try {
                        val errorBody = serviciosResponse.errorBody()?.string()
                        println("DEBUG: Error body: $errorBody")
                    } catch (e: Exception) {
                        println("DEBUG: No se pudo leer error body")
                    }
                    emptyList()
                }

                withContext(Dispatchers.Main) {
                    view.onClientsLoaded(clients)
                    view.onSalonsLoaded(salons)
                    view.onServiciosLoaded(servicios)
                    view.hideLoading()
                }

            } catch (e: Exception) {
                e.printStackTrace()

                withContext(Dispatchers.Main) {
                    view.hideLoading()
                    view.showError("Error al cargar datos: ${e.message}")
                }
            }
        }
    }

    override fun createReservation(client: Client, salon: Salon, date: String, startTime: String, endTime: String, service: String) {
        view.showLoading()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val serviceToSend = if (service != "Ninguno" && service.isNotEmpty()) service else null

                val requestBody = ReservationRequest(
                    email_cliente = client.email,
                    nombre_sala = salon.name,
                    fecha = date,
                    hora_inicio = startTime,
                    hora_fin = endTime,
                    estado = "Pendiente",
                    nombre_servicio = serviceToSend
                )

                println("REQUEST BODY: $requestBody")

                val response = apiService.createReservation(requestBody)

                withContext(Dispatchers.Main) {
                    view.hideLoading()

                    if (response.isSuccessful) {
                        val salonResponse = response.body()
                        println("RESPONSE SUCCESS: $salonResponse")
                        if (salonResponse?.success == true) {
                            view.onReservationCreated()
                        } else {
                            view.onReservationCreationFailed(
                                salonResponse?.message ?: "Error desconocido"
                            )
                        }
                    } else {
                        val errorBody = try {
                            response.errorBody()?.string() ?: "Sin detalles"
                        } catch (e: Exception) {
                            "No se pudo leer el cuerpo del error"
                        }
                        view.onReservationCreationFailed("Error ${response.code()}: $errorBody")
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view.hideLoading()
                    println("EXCEPTION: ${e.message}")
                    e.printStackTrace()
                    view.onReservationCreationFailed("Error de conexión: ${e.message}")
                }
            }
        }
    }

    override fun getClientByPosition(position: Int): Client? {
        return clients.getOrNull(position)
    }

    override fun getSalonByPosition(position: Int): Salon? {
        return salons.getOrNull(position)
    }

    override fun getServicioByPosition(position: Int): Servicio? {
        return servicios.getOrNull(position)
    }
}