package com.example.salontenexapp.data.api

import com.example.salontenexapp.Modelo.Client
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ClientService {

    // CORREGIDO: Usar @Query en lugar de @Path para parámetros GET
    @GET("get_client_by_id.php")
    suspend fun getClientById(@Query("id") id: Int): Response<Client>

    // CORREGIDO: Para PUT, enviar el ID en el cuerpo y usar endpoint sin {id}
    @PUT("update_client.php")
    suspend fun updateClient(@Body client: Client): Response<Client>

    // OPCIÓN ALTERNATIVA si prefieres enviar ID en la URL:
    // @PUT("update_client.php/{id}")
    // suspend fun updateClient(@Path("id") id: Int, @Body client: Client): Response<Client>

    @GET("clientes/email/{email}")
    suspend fun getClientByEmail(@Path("email") email: String): Response<Client>
}