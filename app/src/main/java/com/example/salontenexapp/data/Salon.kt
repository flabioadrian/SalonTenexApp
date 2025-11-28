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
) : Parcelable