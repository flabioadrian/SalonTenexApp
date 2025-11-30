package com.example.salontenexapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Salon(
    val id: Int = 0, // -> id_sala (en PHP)
    val name: String, // -> nombre
    val capacity: Int, // -> capacidad
    val description: String, // -> descripcion
    val price: Double, // -> precio
    val imageUrl: String? = null // -> imagen
) : Parcelable {
    fun getFullImageUrl(): String {
        return if (imageUrl.isNullOrEmpty() || imageUrl == "default.jpg") {
            // Imagen por defecto
            "https://salonestenex.b-corpsolutions.com/img/default.jpg"
        } else if (imageUrl!!.startsWith("http")) {
            // Ya es una URL completa
            imageUrl
        } else if (imageUrl!!.startsWith("img/")) {
            // Es una ruta relativa del servidor
            "https://salonestenex.b-corpsolutions.com/$imageUrl"
        } else {
            // Solo el nombre del archivo
            "https://salonestenex.b-corpsolutions.com/img/$imageUrl"
        }
    }
}