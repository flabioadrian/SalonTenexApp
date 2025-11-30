package com.example.salontenexapp.Presentador

import android.content.ContentValues.TAG
import android.util.Log
import com.example.salontenexapp.Contrato.ServiciosContract
import com.example.salontenexapp.Modelo.Servicio
import com.example.salontenexapp.Modelo.ServicioRequest
import com.example.salontenexapp.data.api.APIService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class ServiciosPresenter(
    private val view: ServiciosContract.View,
    private val apiService: APIService
) : ServiciosContract.Presenter {

    override fun cargarServicios() {
        view.mostrarCargando()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getServicios()
                withContext(Dispatchers.Main) {
                    view.ocultarCargando()
                    if (response.isSuccessful) {
                        val servicioResponse = response.body()
                        if (servicioResponse?.success == true) {
                            val servicios = servicioResponse.data ?: emptyList()
                            view.mostrarServicios(servicios)
                        } else {
                            val errorMsg =
                                servicioResponse?.message ?: "Error desconocido en respuesta"
                            view.mostrarError("Error: $errorMsg")
                        }
                    } else {
                        view.mostrarError("Error del servidor: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view.ocultarCargando()
                    view.mostrarError("Error de conexión: ${e.message}")
                }
            }
        }
    }

    override fun actualizarServicio(servicio: Servicio) {
        view.mostrarCargando()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val servicioData = Servicio(
                    idServicio = servicio.idServicio,
                    nombreServicio = servicio.nombreServicio,
                    costo = servicio.costo,
                    descripcion = servicio.descripcion,
                    estado = servicio.estado
                )
                val response = apiService.updateServicio(servicioData)

                withContext(Dispatchers.Main) {
                    view.ocultarCargando()

                    if (response.isSuccessful) {
                        val servicioResponse = response.body()
                        if (servicioResponse?.success == true) {
                            val mensaje = servicioResponse.message ?: "Servicio actualizado correctamente"
                            view.mostrarMensajeExito(mensaje)
                            cargarServicios()
                        } else {
                            val errorMsg = servicioResponse?.message ?: "Error desconocido"
                            view.mostrarError("Error al actualizar: $errorMsg")
                        }
                    } else {
                        view.mostrarError("Error HTTP: ${response.code()} - ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view.ocultarCargando()
                    view.mostrarError("Error de conexión: ${e.message}")
                }
            }
        }
    }

    override fun crearServicio(servicioRequest: ServicioRequest) {
        view.mostrarCargando()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.createServicio(servicioRequest)

                withContext(Dispatchers.Main) {
                    val statusResponse = response.body()

                    if (response.isSuccessful) {
                        if (statusResponse?.success == true) {
                            val mensaje = statusResponse.message ?: "Servicio creado correctamente"

                            view.mostrarMensajeExito(mensaje)
                            cargarServicios()
                        } else {
                            val errorMsg = statusResponse?.message ?: "Error desconocido en la creación"
                            view.mostrarError("Error al crear: $errorMsg")
                            view.ocultarCargando()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Sin detalles"
                        view.mostrarError("Error del servidor (${response.code()}): $errorBody")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view.ocultarCargando()
                    view.mostrarError("Error de conexión: ${e.message}")
                }
            }
        }
    }

    override fun cambiarEstadoServicio(idServicio: Int, nuevoEstado: String) {
        view.mostrarCargando()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = Servicio(
                    idServicio = idServicio,
                    nombreServicio = "",
                    costo = 0.0,
                    descripcion = "",
                    estado = nuevoEstado
                )

                Log.d(TAG, "Cambiando estado de $idServicio a $nuevoEstado")
                val response = apiService.deleteServicio(request)

                withContext(Dispatchers.Main) {
                    view.ocultarCargando()

                    if (response.isSuccessful) {
                        val statusResponse = response.body() // Asumo que usas StatusResponse
                        if (statusResponse?.success == true) {
                            val accionMensaje = if (nuevoEstado == "Activo") "habilitado" else "deshabilitado"
                            val mensaje = statusResponse.message ?: "Servicio $accionMensaje correctamente"

                            view.mostrarMensajeExito(mensaje)
                            cargarServicios() // Actualizar la lista
                        } else {
                            view.mostrarError("Error al cambiar estado: ${statusResponse?.message ?: "Error desconocido"}")
                        }
                    } else {
                        view.mostrarError("Error HTTP: ${response.code()} - ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view.ocultarCargando()
                    view.mostrarError("Error de conexión al cambiar estado: ${e.message}")
                }
            }
        }
    }
}