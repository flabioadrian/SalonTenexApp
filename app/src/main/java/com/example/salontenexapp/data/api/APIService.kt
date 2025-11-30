package com.example.salontenexapp.data.api

import com.example.salontenexapp.Modelo.Client
import com.example.salontenexapp.Modelo.LoginRequest
import com.example.salontenexapp.Modelo.LoginResponse
import com.example.salontenexapp.data.Reservation
import com.example.salontenexapp.Modelo.ReservationRequest
import com.example.salontenexapp.data.Salon
import com.example.salontenexapp.Modelo.SalonResponse
import com.example.salontenexapp.Modelo.Servicio
import com.example.salontenexapp.Modelo.ServicioRequest
import com.example.salontenexapp.Modelo.ServicioResponse
import com.example.salontenexapp.Modelo.StatusResponse
import com.example.salontenexapp.Modelo.UploadImageResponse
import com.example.salontenexapp.data.ApiResponse
import com.example.salontenexapp.data.CancelReservationRequest
import com.example.salontenexapp.data.EditReservationRequest
import com.example.salontenexapp.data.ReservationClient
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

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
    suspend fun createSalon(@Body salon: Salon): Response<SalonResponse>

    @GET("reservaciones_api.php")
    suspend fun getReservations(): Response<List<Reservation>>

    @POST("reservaciones_api.php")
    suspend fun createReservation(@Body reservationData: ReservationRequest): Response<SalonResponse>

    @PUT("reservaciones_api.php")
    suspend fun updateReservation(@Body updateData: Map<String, Any>): Response<SalonResponse>

    @PUT("reservaciones_api.php?action=cancelar")
    suspend fun cancelReservation(@Body cancelData: Map<String, Int>): Response<SalonResponse>

    @PUT("salas.php")
    suspend fun updateSalon(@Body salon: Salon): Response<SalonResponse>

    // NUEVOS ENDPOINTS
    @GET("clientes.php")
    suspend fun getClients(): List<Client>

    @GET("ver_reservas_cliente.php")
    fun getClientReservations(): Call<List<ReservationClient>>

    @POST("cancelar_reserva.php")
    fun cancelReservation(@Body request: CancelReservationRequest): Call<ApiResponse>

    @POST("editar_reserva.php")
    fun editReservation(@Body request: EditReservationRequest): Call<ApiResponse>

    @GET("servicios_api.php")
    suspend fun getServicios(): Response<ServicioResponse>

    @POST("servicios_api.php")
    suspend fun createServicio(@Body servicio: ServicioRequest): Response<StatusResponse>

    @PUT("servicios_api.php")
    suspend fun updateServicio(@Body servicio: Servicio): Response<StatusResponse>

    @HTTP(method = "DELETE", path = "servicios_api.php", hasBody = true)
    suspend fun deleteServicio(@Body request: Servicio): Response<StatusResponse>

    @Multipart
    @POST("upload_image.php")
    suspend fun uploadImage(@Part image: MultipartBody.Part): Response<UploadImageResponse>
}