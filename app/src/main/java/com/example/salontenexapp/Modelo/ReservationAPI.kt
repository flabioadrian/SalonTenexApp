package com.example.salontenexapp.Modelo

import com.example.salontenexapp.data.Reservation
import com.example.salontenexapp.data.Salon
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface ReservationAPI {

    @GET("reservaciones_api.php")
    suspend fun getReservations(): Response<List<Reservation>>

    @POST("reservaciones_api.php")
    suspend fun createReservation(@Body reservationData: Map<String, Any>): Response<SalonResponse>

    @PUT("reservaciones_api.php")
    suspend fun updateReservation(@Body updateData: Map<String, Any>): Response<SalonResponse>

    @PUT("reservaciones_api.php?action=cancelar")
    suspend fun cancelReservation(@Body cancelData: Map<String, Int>): Response<SalonResponse>
}