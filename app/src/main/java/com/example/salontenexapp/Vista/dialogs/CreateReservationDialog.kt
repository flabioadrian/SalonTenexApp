package com.example.salontenexapp.Vista.dialogs

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.salontenexapp.Contrato.ReservationContract
import com.example.salontenexapp.Modelo.*
import com.example.salontenexapp.Presentador.CreateReservationPresenter
import com.example.salontenexapp.Vista.adapter.ClientSpinnerAdapter
import com.example.salontenexapp.Vista.adapter.SalonSpinnerAdapter
import com.example.salontenexapp.data.Salon
import com.example.salontenexapp.data.api.APIService
import com.example.salontenexapp.data.api.RetrofitClient
import com.example.salontenexapp.databinding.DialogCreateReservationBinding
import java.text.SimpleDateFormat
import java.util.*

class CreateReservationDialog : DialogFragment(), ReservationContract.CreateReservationView {

    private lateinit var presenter: CreateReservationPresenter
    private lateinit var binding: DialogCreateReservationBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogCreateReservationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar presenter
        val apiService = RetrofitClient.retrofit.create(APIService::class.java)
            presenter = CreateReservationPresenter(this, apiService)

        setupUI()
        presenter.loadInitialData()
    }

    private fun setupUI() {
        // Configurar listeners, etc.
    }

    // Implementación de la interfaz CreateReservationView
    override fun onClientsLoaded(clients: List<Client>) {
        val adapter = ClientSpinnerAdapter(requireContext(), clients)
        binding.spinnerClient.adapter = adapter
    }

    override fun onSalonsLoaded(salons: List<Salon>) {
        val adapter = SalonSpinnerAdapter(requireContext(), salons)
        binding.spinnerSalon.adapter = adapter
    }

    override fun showLoading() {
        // Mostrar progress bar
    }

    override fun hideLoading() {
        // Ocultar progress bar
    }

    override fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    // Método para obtener los datos seleccionados al crear la reserva
    private fun getSelectedData(): Pair<Client?, Salon?> {
        val selectedClient = presenter.getClientByPosition(binding.spinnerClient.selectedItemPosition)
        val selectedSalon = presenter.getSalonByPosition(binding.spinnerSalon.selectedItemPosition)
        return Pair(selectedClient, selectedSalon)
    }
}