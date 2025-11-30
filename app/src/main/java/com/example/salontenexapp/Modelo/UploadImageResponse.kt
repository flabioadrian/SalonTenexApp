package com.example.salontenexapp.Modelo

data class UploadImageResponse(
    val success: Boolean,
    val imageUrl: String?,
    val message: String?,
    val error: String?
)
