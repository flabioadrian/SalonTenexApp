package com.example.salontenexapp.data.api

import com.example.salontenexapp.Modelo.Client
import com.example.salontenexapp.Modelo.LoginRequest
import com.example.salontenexapp.Modelo.LoginResponse
import com.example.salontenexapp.data.Reservation
import com.example.salontenexapp.Modelo.ReservationIdWrapper
import com.example.salontenexapp.data.Salon
import com.example.salontenexapp.Modelo.SalonResponse
import okhttp3.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query // Importación necesaria

interface APIService {
    @POST("procesar_login.php")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

    @GET("ver_reservas.php")
    suspend fun getReservationsClient(
        @Query("id_cliente") id: Int
    ): List<Reservation>

    @GET("salas.php")
    suspend fun getSalons(): List<Salon>

    @POST("salas.php")
    suspend fun createSalon(@Body salon: Salon): SalonResponse

    // 💡 Método Admin: Obtiene todas las reservas (requiere sesión admin)
    @GET("reservaciones_api.php")
    suspend fun getRecentReservations(): List<Reservation>

    @POST("reservaciones_api.php")
    suspend fun createReservation(@Body reservation: Reservation): SalonResponse

    @PUT("reservaciones_api.php")
    suspend fun updateReservation(@Body reservation: Reservation): SalonResponse

    @PUT("reservaciones_api.php?action=cancelar")
    suspend fun cancelReservation(@Body idWrapper: ReservationIdWrapper): SalonResponse

    @PUT("salas.php/{id}")
    suspend fun updateSalon(@Path("id") salonId: Int, @Body salon: Salon): SalonResponse

    // NUEVOS ENDPOINTS
    @GET("clientes.php") // Necesitarás crear este endpoint en tu backend
    suspend fun getClients(): List<Client>
}