package com.example.salontenexapp.Contrato

import com.example.salontenexapp.Modelo.Client

interface ProfileContract {
    interface ProfileView {
        fun showClientProfile(client: Client)
        fun onProfileUpdated(client: Client)
        fun showLoading()
        fun hideLoading()
        fun showError(message: String)
        fun showSuccess(message: String)
    }
}