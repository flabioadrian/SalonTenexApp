package com.example.salontenexapp.Contrato

import com.example.salontenexapp.data.Salon
import com.example.salontenexapp.Modelo.Service
import com.example.salontenexapp.Modelo.User
import kotlin.collections.List

interface AdminContract {
    interface View {
        fun showSalons(salons: List<Salon>)
        fun showServices(services: List<Service>)
        fun showAdmins(admins: List<User>)
        fun onItemCreated()
        fun onItemUpdated()
        fun onItemDeleted()
        fun showError(message: String)
    }

    interface Presenter {
        // CRUD para Salones
        fun loadSalons()
        fun createSalon(salon: Salon)
        fun updateSalon(salon: Salon)
        fun deleteSalon(salonId: Int)

        // CRUD para Servicios
        fun loadServices()
        fun createService(service: Service)
        fun updateService(service: Service)
        fun deleteService(serviceId: Int)

        // CRUD para Administradores
        fun loadAdmins()
        fun createAdmin(admin: User)
        fun deleteAdmin(adminId: Int)
    }
}