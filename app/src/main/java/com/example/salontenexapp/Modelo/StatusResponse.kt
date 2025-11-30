package com.example.salontenexapp.Modelo

import com.google.gson.annotations.SerializedName

data class StatusResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)
