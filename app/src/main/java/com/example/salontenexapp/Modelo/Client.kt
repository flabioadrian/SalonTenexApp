package com.example.salontenexapp.Modelo

import com.google.gson.annotations.SerializedName
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Client(
    @SerializedName("id_cliente")
    val id: Int = 0,

    @SerializedName("email")
    val email: String,

    @SerializedName("nombre")
    val name: String? = null
) : Parcelable {
    // Para mostrar en el spinner
    override fun toString(): String {
        return name?.let { "$it ($email)" } ?: email
    }
}