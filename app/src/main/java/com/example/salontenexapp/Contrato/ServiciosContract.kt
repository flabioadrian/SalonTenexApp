package com.example.salontenexapp.Contrato

import com.example.salontenexapp.Modelo.Servicio
import com.example.salontenexapp.Modelo.ServicioRequest

interface ServiciosContract {
    interface View {
        fun mostrarServicios(servicios: List<Servicio>)
        fun mostrarError(mensaje: String)
        fun mostrarCargando()
        fun ocultarCargando()
        fun mostrarMensajeExito(mensaje: String)
    }

    interface Presenter {
        fun cargarServicios()
        fun actualizarServicio(servicio: Servicio)
        fun cambiarEstadoServicio(idServicio: Int, nuevoEstado: String)
        fun crearServicio(servicioRequest: ServicioRequest)
    }
}