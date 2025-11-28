package com.example.salontenexapp.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.io.Serializable

// Reservation.kt
@Parcelize
data class Reservation(
    @SerializedName("id_reserva")
    val id: Int = 0,

    @SerializedName("id_usuario")
    val userId: Int = 0,

    @SerializedName("id_sala")
    val salonId: Int = 0,

    @SerializedName("id_servicio") // Este nombre NO cambia - es como viene en el JSON
    val serviceId: Int? = null,

    @SerializedName("email_cliente")
    val clientEmail: String,

    @SerializedName("nombre_cliente") // NUEVO campo que agregamos en el PHP
    val clientName: String? = null,

    @SerializedName("nombre_sala")
    val salonName: String,

    @SerializedName("nombre_servicio")
    val serviceName: String? = null,

    @SerializedName("fecha")
    val date: String,

    @SerializedName("hora_inicio")
    val startTime: String,

    @SerializedName("hora_fin")
    val endTime: String,

    @SerializedName("estado")
    val status: String,

    @SerializedName("total_pagar")
    val totalPrice: Double

    // Eliminamos los campos adicionales que ya no son necesarios
    // ya que ahora clientName viene del API
) : Parcelable, Serializable {

    // Función auxiliar para obtener el nombre del cliente (usar esta en lugar de la anterior)
    fun getDisplayClientName(): String {
        // Primero intenta usar el nombre que viene del API
        return if (!clientName.isNullOrBlank()) {
            clientName
        } else {
            // Si no viene nombre del API, genera uno desde el email
            getClientNameFromEmail()
        }
    }

    // Función para calcular la duración
    fun calculateDuration(): String {
        return try {
            val start = startTime.substring(0, 5)
            val end = endTime.substring(0, 5)
            "$start - $end"
        } catch (e: Exception) {
            "$startTime - $endTime"
        }
    }

    // Función para formatear la fecha
    fun getFormattedDate(): String {
        return try {
            val parts = date.split("-")
            if (parts.size == 3) {
                "${parts[2]}/${parts[1]}/${parts[0]}"
            } else {
                date
            }
        } catch (e: Exception) {
            date
        }
    }

    // Función auxiliar para obtener el nombre del cliente desde el email (como fallback)
    private fun getClientNameFromEmail(): String {
        return clientEmail.substringBefore("@")
            .replace(".", " ")
            .replace("_", " ")
            .split(" ")
            .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
    }
}