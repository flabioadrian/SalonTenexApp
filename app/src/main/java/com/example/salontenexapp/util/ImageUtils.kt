package com.example.salontenexapp.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ImageUtils {
    const val REQUEST_IMAGE_PICK = 1001
    const val REQUEST_IMAGE_CAPTURE = 1002

    // Comprimir imagen para reducir tamaño
    fun compressImage(context: Context, imageUri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Redimensionar si es muy grande
            val maxWidth = 800
            val maxHeight = 600
            val scaledBitmap = scaleBitmap(originalBitmap, maxWidth, maxHeight)

            // Comprimir
            val file = File(context.cacheDir, "compressed_image.jpg")
            val outputStream = FileOutputStream(file)
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            outputStream.flush()
            outputStream.close()

            file
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun scaleBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }

        val scaleFactor = Math.min(maxWidth.toFloat() / width, maxHeight.toFloat() / height)
        val scaledWidth = (width * scaleFactor).toInt()
        val scaledHeight = (height * scaleFactor).toInt()

        return Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
    }

    // Crear MultipartBody.Part desde File
    fun createImagePart(file: File): MultipartBody.Part {
        val mediaType = "image/*".toMediaTypeOrNull()
        val requestFile = file.asRequestBody(mediaType)

        return MultipartBody.Part.createFormData(name = "image", filename = file.name, body = requestFile)
    }

    // Obtener extensión del archivo
    fun getFileExtension(context: Context, uri: Uri): String? {
        val contentResolver = context.contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
    }
}