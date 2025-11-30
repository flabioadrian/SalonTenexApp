// ProfilePresenter.kt
package com.example.salontenexapp.Presentador

import com.example.salontenexapp.Contrato.ProfileContract
import com.example.salontenexapp.data.api.ClientService
import com.example.salontenexapp.Modelo.Client
import com.example.salontenexapp.util.SharedPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class ProfilePresenter(
    private val view: ProfileContract.ProfileView,
    private val clientService: ClientService,
    private val prefs: SharedPreferencesManager
) {

    suspend fun loadClientProfile() {
        try {
            // Verificar que el usuario sea cliente
            if (prefs.getUserType() != "client") {
                view.showError("Acceso denegado: Solo para clientes")
                return
            }

            val clientId = prefs.getUserId()
            if (clientId == -1) {
                view.showError("No se pudo obtener la información del usuario")
                return
            }

            view.showLoading()

            val response: Response<Client> = withContext(Dispatchers.IO) {
                clientService.getClientById(clientId)
            }

            if (response.isSuccessful) {
                val client = response.body()
                if (client != null) {
                    view.showClientProfile(client)
                } else {
                    view.showError("No se encontró información del cliente")
                }
            } else {
                view.showError("Error al cargar perfil: ${response.code()}")
            }

        } catch (e: Exception) {
            view.showError("Error de conexión: ${e.message}")
        } finally {
            view.hideLoading()
        }
    }

    suspend fun updateClientProfile(updatedClient: Client) {
        try {
            // Verificar que el usuario sea cliente
            if (prefs.getUserType() != "client") {
                view.showError("Acceso denegado: Solo para clientes")
                return
            }

            val clientId = prefs.getUserId()
            if (clientId == -1) {
                view.showError("No se pudo obtener la información del usuario")
                return
            }

            // Asegurar que el ID del cliente en el objeto coincida con el de la sesión
            if (updatedClient.id != clientId) {
                view.showError("Error de seguridad: ID de cliente no coincide")
                return
            }

            view.showLoading()

            // CORRECCIÓN: Solo pasar el objeto client, sin el ID por separado
            val response: Response<Client> = withContext(Dispatchers.IO) {
                clientService.updateClient(updatedClient)
            }

            if (response.isSuccessful) {
                val updatedClientResponse = response.body()
                if (updatedClientResponse != null) {
                    view.onProfileUpdated(updatedClientResponse)
                    view.showSuccess("Perfil actualizado correctamente")
                } else {
                    view.showError("Error al actualizar el perfil")
                }
            } else {
                view.showError("Error al actualizar: ${response.code()}")
            }

        } catch (e: Exception) {
            view.showError("Error de conexión: ${e.message}")
        } finally {
            view.hideLoading()
        }
    }

    // Método para debuguear
    fun debugUserType() {
        val userType = prefs.getUserType()
        val userId = prefs.getUserId()
        val email = prefs.getUserEmail()

        println("DEBUG - UserType: $userType, UserId: $userId, Email: $email")
    }
}