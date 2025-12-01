package com.example.salontenexapp.data.api

import com.example.salontenexapp.Modelo.CancelReservationRequest
import com.example.salontenexapp.Modelo.Client
import com.example.salontenexapp.Modelo.ReservationClient
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ClientService {
    // Servicios existentes para Client
    @GET("get_client_by_id.php")
    suspend fun getClientById(@Query("id") id: Int): Response<Client>

    @PUT("update_client.php")
    suspend fun updateClient(@Body client: Client): Response<Client>

    @GET("clientes/email/{email}")
    suspend fun getClientByEmail(@Path("email") email: String): Response<Client>

    // NUEVOS SERVICIOS PARA RESERVAS
    @GET("get_client_reservations.php")
    suspend fun getClientReservations(@Query("id_cliente") idCliente: Int): Response<List<ReservationClient>>

    @PUT("cancel_reservation.php")
    suspend fun cancelReservation(@Body request: CancelReservationRequest): Response<ReservationClient>

    @PUT("update_reservation.php")
    suspend fun updateReservation(@Body reserva: ReservationClient): Response<ReservationClient>

    @POST("create_reservation.php")
    suspend fun createReservation(@Body reserva: ReservationClient): Response<ReservationClient>
}