// CreateReservationPresenter.kt
package com.example.salontenexapp.presenters

import com.example.salontenexapp.Contrato.CreateReservationContract
import com.example.salontenexapp.Modelo.ReservationClient
import com.example.salontenexapp.Modelo.Servicio
import com.example.salontenexapp.data.Salon
import com.example.salontenexapp.data.api.ClientService
import com.example.salontenexapp.data.api.RetrofitClient
import com.example.salontenexapp.util.SharedPreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ClientCreateReservationPresenter : CreateReservationContract.Presenter {

    private var view: CreateReservationContract.View? = null
    private lateinit var prefs: SharedPreferencesManager
    private lateinit var clientService: ClientService

    private var salasList = mutableListOf<Salon>()
    private var serviciosList = mutableListOf<Servicio>()
    private var selectedSala: Salon? = null
    private var selectedServicio: Servicio? = null
    private var selectedFecha: String = ""
    private var selectedHoraInicio: String = ""
    private var selectedHoraFin: String = ""

    private var isEditMode = false
    private var existingReservation: ReservationClient? = null

    override fun attachView(view: CreateReservationContract.View) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }

    override fun loadInitialData() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                view?.showLoading()

                // Load salons
                val salonsResponse = RetrofitClient.apiService.getSalons()
                salasList.clear()
                salasList.addAll(salonsResponse)
                view?.setSalas(salasList)

                // Load services
                loadServicios()

                if (isEditMode) {
                    existingReservation?.let { reservation ->
                        view?.setExistingReservation(reservation)
                    }
                }

            } catch (e: Exception) {
                view?.showError("Error al cargar datos: ${e.message}")
            } finally {
                view?.hideLoading()
            }
        }
    }

    private suspend fun loadServicios() {
        try {
            val serviciosResponse = RetrofitClient.apiService.getServicios()

            if (serviciosResponse.isSuccessful) {
                val responseBody = serviciosResponse.body()
                if (responseBody?.success == true) {
                    val servicios = responseBody.data ?: emptyList()
                    serviciosList.clear()
                    serviciosList.addAll(servicios)
                    view?.setServicios(serviciosList)
                } else {
                    view?.showError("Error al cargar servicios")
                }
            } else {
                view?.showError("Error HTTP al cargar servicios: ${serviciosResponse.code()}")
            }
        } catch (e: Exception) {
            view?.showError("Error al cargar servicios: ${e.message}")
        }
    }

    override fun onSalaSelected(sala: Salon) {
        selectedSala = sala
        view?.showSalaInfo(sala)
        calculateTotal(sala, selectedServicio)
    }

    override fun onServicioSelected(servicio: Servicio?) {
        selectedServicio = servicio
        calculateTotal(selectedSala, servicio)
    }

    override fun onDateSelected(date: String) {
        selectedFecha = date
    }

    override fun onStartTimeSelected(time: String) {
        selectedHoraInicio = time
    }

    override fun onEndTimeSelected(time: String) {
        selectedHoraFin = time
    }

    override fun validateTimeRange(startTime: String, endTime: String): Boolean {
        if (startTime.isEmpty() || endTime.isEmpty()) return true

        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        try {
            val startTimeObj = timeFormat.parse(startTime)
            val endTimeObj = timeFormat.parse(endTime)

            if (endTimeObj!!.before(startTimeObj) || endTimeObj == startTimeObj) {
                return false
            }

            // Validate minimum duration (1 hour)
            val duration = endTimeObj.time - startTimeObj.time
            val minDuration = 60 * 60 * 1000 // 1 hour in milliseconds
            return duration >= minDuration

        } catch (e: Exception) {
            return false
        }
    }

    override fun calculateTotal(sala: Salon?, servicio: Servicio?) {
        var total = 0.0
        sala?.let { total += it.price }
        servicio?.let { total += it.costo }
        view?.updateTotal(total)
    }

    override fun validateForm(
        sala: Salon?,
        fecha: String,
        horaInicio: String,
        horaFin: String
    ): Boolean {
        if (sala == null) {
            view?.showError("Debes seleccionar una sala")
            return false
        }
        if (fecha.isEmpty()) {
            view?.showError("Debes seleccionar una fecha")
            return false
        }
        if (horaInicio.isEmpty() || horaFin.isEmpty()) {
            view?.showError("Debes seleccionar el horario completo")
            return false
        }
        if (!validateTimeRange(horaInicio, horaFin)) {
            view?.showError("El rango de tiempo no es válido")
            return false
        }
        return true
    }

    override fun createReservation(
        sala: Salon,
        servicio: Servicio?,
        fecha: String,
        horaInicio: String,
        horaFin: String
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                view?.showLoading()

                val clientId = prefs.getUserId()
                val newReservation = ReservationClient(
                    id = 0,
                    idUsuario = clientId,
                    idSala = sala.id,
                    fecha = fecha,
                    horaInicio = horaInicio,
                    horaFin = horaFin,
                    estado = "Confirmada",
                    totalPagar = getTotalPrice(sala, servicio),
                    idServicio = servicio?.idServicio,
                    nombreSala = sala.name,
                    nombreServicio = servicio?.nombreServicio,
                    precioSala = sala.price,
                    capacidadSala = sala.capacity,
                    descripcionSala = sala.description
                )

                val response: Response<ReservationClient> =
                    clientService.createReservation(newReservation)

                if (response.isSuccessful) {
                    view?.showSuccess("Reserva creada exitosamente")
                    view?.closeFragment()
                } else {
                    view?.showError("Error al crear reserva: ${response.code()}")
                }
            } catch (e: Exception) {
                view?.showError("Error de conexión: ${e.message}")
            } finally {
                view?.hideLoading()
            }
        }
    }

    override fun updateReservation(
        reservation: ReservationClient,
        sala: Salon,
        servicio: Servicio?,
        fecha: String,
        horaInicio: String,
        horaFin: String
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                view?.showLoading()

                val updatedReservation = ReservationClient(
                    id = reservation.id,
                    idUsuario = reservation.idUsuario,
                    idSala = sala.id,
                    fecha = fecha,
                    horaInicio = horaInicio,
                    horaFin = horaFin,
                    estado = reservation.estado,
                    totalPagar = getTotalPrice(sala, servicio),
                    idServicio = servicio?.idServicio,
                    nombreSala = sala.name,
                    nombreServicio = servicio?.nombreServicio,
                    precioSala = sala.price,
                    capacidadSala = sala.capacity,
                    descripcionSala = sala.description
                )

                val response: Response<ReservationClient> =
                    clientService.updateReservation(updatedReservation)

                if (response.isSuccessful) {
                    view?.showSuccess("Reserva actualizada exitosamente")
                    view?.closeFragment()
                } else {
                    view?.showError("Error al actualizar reserva: ${response.code()}")
                }
            } catch (e: Exception) {
                view?.showError("Error de conexión: ${e.message}")
            } finally {
                view?.hideLoading()
            }
        }
    }

    override fun getExistingReservation(): ReservationClient? = existingReservation

    override fun isEditMode(): Boolean = isEditMode

    fun setEditMode(editMode: Boolean, reservation: ReservationClient? = null) {
        isEditMode = editMode
        existingReservation = reservation
    }

    fun initializeServices(prefs: SharedPreferencesManager, clientService: ClientService) {
        this.prefs = prefs
        this.clientService = clientService
    }

    private fun getTotalPrice(sala: Salon, servicio: Servicio?): Double {
        var total = sala.price
        total += servicio?.costo ?: 0.0
        return total
    }
}