package com.example.salontenexapp.Vista.dialogs

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.salontenexapp.Contrato.ReservationContract
import com.example.salontenexapp.Modelo.*
import com.example.salontenexapp.Presentador.CreateReservationPresenter
import com.example.salontenexapp.Vista.ManageReservationsFragment
import com.example.salontenexapp.Vista.adapter.ClientSpinnerAdapter
import com.example.salontenexapp.Vista.adapter.SalonSpinnerAdapter
import com.example.salontenexapp.Vista.adapter.ServicioSpinnerAdapter
import com.example.salontenexapp.data.Salon
import com.example.salontenexapp.data.api.APIService
import com.example.salontenexapp.data.api.RetrofitClient
import com.example.salontenexapp.databinding.DialogCreateReservationBinding
import java.text.SimpleDateFormat
import java.util.*

class CreateReservationDialog : DialogFragment(), ReservationContract.CreateReservationView {

    private lateinit var presenter: CreateReservationPresenter
    private lateinit var binding: DialogCreateReservationBinding
    private val calendar = Calendar.getInstance()

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
        // Configurar el botón Cancelar
        binding.btnCancel.setOnClickListener {
            dismiss() // Esto cierra el diálogo
        }

        // Configurar el selector de fecha
        binding.etDate.setOnClickListener {
            showDatePicker()
        }

        // Configurar el selector de hora de inicio
        binding.etStartTime.setOnClickListener {
            showTimePicker(true) // true para hora de inicio
        }

        // Configurar el selector de hora de fin
        binding.etEndTime.setOnClickListener {
            showTimePicker(false) // false para hora de fin
        }

        // Configurar el botón de crear reserva
        binding.btnCreate.setOnClickListener {
            createReservation()
        }
    }

    private fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                binding.etDate.setText(dateFormat.format(calendar.time))
            },
            year,
            month,
            day
        )

        // Opcional: Establecer fecha mínima como hoy
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }

    private fun showTimePicker(isStartTime: Boolean) {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                val timeString = String.format("%02d:%02d", selectedHour, selectedMinute)
                if (isStartTime) {
                    binding.etStartTime.setText(timeString)
                } else {
                    binding.etEndTime.setText(timeString)
                }
            },
            hour,
            minute,
            true // true para formato 24 horas
        )
        timePickerDialog.show()
    }

    private fun setupServiceSpinner() {
        val services = arrayOf("Ninguno", "Servicio de Catering", "Servicio de Audio", "Servicio de Limpieza")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, services)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerService.adapter = adapter
    }

    private fun createReservation() {
        val selectedClient = presenter.getClientByPosition(binding.spinnerClient.selectedItemPosition)
        val selectedSalon = presenter.getSalonByPosition(binding.spinnerSalon.selectedItemPosition)
        val selectedServicio = presenter.getServicioByPosition(binding.spinnerService.selectedItemPosition)
        val date = binding.etDate.text.toString()
        val startTime = binding.etStartTime.text.toString()
        val endTime = binding.etEndTime.text.toString()

        if (selectedClient == null) {
            showError("Por favor seleccione un cliente")
            return
        }

        if (selectedSalon == null) {
            showError("Por favor seleccione un salón")
            return
        }

        if (date.isEmpty()) {
            showError("Por favor seleccione una fecha")
            return
        }

        if (startTime.isEmpty()) {
            showError("Por favor seleccione la hora de inicio")
            return
        }

        if (endTime.isEmpty()) {
            showError("Por favor seleccione la hora de fin")
            return
        }

        if (startTime >= endTime) {
            showError("La hora de fin debe ser posterior a la hora de inicio")
            return
        }

        val serviceName = selectedServicio?.nombreServicio

        presenter.createReservation(
            selectedClient,
            selectedSalon,
            date,
            startTime,
            endTime,
            serviceName ?: "Ninguno"
        )
    }

    override fun onServiciosLoaded(servicios: List<Servicio>) {
        val serviciosConNinguno = mutableListOf<Servicio>().apply {
            add(Servicio(0, "Ninguno", 0.0, "", "Activo"))
            addAll(servicios)
        }

        val adapter = ServicioSpinnerAdapter(requireContext(), serviciosConNinguno)
        binding.spinnerService.adapter = adapter
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
        binding.progressBar.visibility = View.VISIBLE
        binding.btnCreate.isEnabled = false
    }

    override fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnCreate.isEnabled = true
    }

    override fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onReservationCreated() {
        Toast.makeText(requireContext(), "Reserva creada exitosamente", Toast.LENGTH_SHORT).show()
        dismiss()
        (parentFragment as? ManageReservationsFragment)?.let {
            it.presenter.refreshReservations()
        }
    }

    override fun onReservationCreationFailed(error: String) {
        showError("Error al crear la reserva: $error")
        Log.e(TAG, "Error al crear la reserva: $error") // Logea el error
    }
}