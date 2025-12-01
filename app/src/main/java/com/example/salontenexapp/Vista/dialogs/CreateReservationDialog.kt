package com.example.salontenexapp.Vista.dialogs

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.salontenexapp.Contrato.ReservationContract
import com.example.salontenexapp.Modelo.*
import com.example.salontenexapp.Presentador.CreateReservationPresenter
import com.example.salontenexapp.Vista.ManageReservationsFragment
import com.example.salontenexapp.Vista.adapter.ClientSpinnerAdapter
import com.example.salontenexapp.Vista.adapter.SalonSpinnerAdapter
import com.example.salontenexapp.Vista.adapter.ServicioSpinnerAdapter
import com.example.salontenexapp.data.Reservation
import com.example.salontenexapp.data.Salon
import com.example.salontenexapp.data.api.APIService
import com.example.salontenexapp.data.api.RetrofitClient
import com.example.salontenexapp.databinding.DialogCreateReservationBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class CreateReservationDialog : DialogFragment(), ReservationContract.CreateReservationView {

    private lateinit var presenter: CreateReservationPresenter
    private lateinit var binding: DialogCreateReservationBinding
    private val calendar = Calendar.getInstance()
    private var existingReservation: Reservation? = null
    private var onReservationUpdated: (() -> Unit)? = null

    fun setOnReservationUpdatedListener(listener: () -> Unit) {
        onReservationUpdated = listener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogCreateReservationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        existingReservation = arguments?.getParcelable<Reservation>(ARG_RESERVATION)

        // Inicializar presenter
        val apiService = RetrofitClient.retrofit.create(APIService::class.java)
        presenter = CreateReservationPresenter(this, apiService)

        setupUI()
        presenter.loadInitialData()

        // Configurar para edición después de cargar los datos
        if (existingReservation != null) {
            binding.btnCreate.text = "Actualizar Reservación"
        }
    }

    companion object {
        private const val ARG_RESERVATION = "reservation"

        fun newInstance(reservation: Reservation? = null): CreateReservationDialog {
            val fragment = CreateReservationDialog()
            if (reservation != null) {
                val args = Bundle().apply {
                    putParcelable(ARG_RESERVATION, reservation)
                }
                fragment.arguments = args
            }
            return fragment
        }
    }

    private fun setupUI() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.etDate.setOnClickListener {
            showDatePicker()
        }

        binding.etStartTime.setOnClickListener {
            showTimePicker(true)
        }

        binding.etEndTime.setOnClickListener {
            showTimePicker(false)
        }

        binding.btnCreate.setOnClickListener {
            if (existingReservation != null) {
                updateReservation()
            } else {
                createReservation()
            }
        }
        binding.spinnerSalon.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                updateTotalPrices()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.spinnerService.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                updateTotalPrices()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun fillFormWithExistingData(reservation: Reservation) {
        binding.etDate.setText(reservation.date)
        binding.etStartTime.setText(reservation.startTime)
        binding.etEndTime.setText(reservation.endTime)
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
                updateTotalPrices()
            },
            hour,
            minute,
            true
        )
        timePickerDialog.show()
    }

    private fun updateTotalPrices() {
        val selectedSalon = presenter.getSalonByPosition(binding.spinnerSalon.selectedItemPosition)

        val servicioPosition = binding.spinnerService.selectedItemPosition
        val selectedServicio = if (servicioPosition == 0) {
            null
        } else {
            presenter.getServicioByPosition(servicioPosition - 1)
        }

        val startTime = binding.etStartTime.text.toString()
        val endTime = binding.etEndTime.text.toString()

        val formattedStartTime = if (startTime.length == 5) "$startTime:00" else startTime
        val formattedEndTime = if (endTime.length == 5) "$endTime:00" else endTime

        val (salonPrice, hours) = calculateSalonPriceLogic(selectedSalon, formattedStartTime, formattedEndTime)

        Log.d("PRICE_DEBUG", "Salon: ${selectedSalon?.name}, Price: ${selectedSalon?.price}, Hours: $hours")
        Log.d("PRICE_DEBUG", "Servicio: ${selectedServicio?.nombreServicio}, Costo: ${selectedServicio?.costo}")

        val servicePrice = selectedServicio?.costo ?: 0.0

        val totalPrice = salonPrice + servicePrice

        val format = "%.2f"
        binding.tvSalonPrice.text = String.format(format, salonPrice)
        binding.tvServicePrice.text = String.format(format, servicePrice)
        binding.tvTotalPrice.text = String.format(format, totalPrice)
    }

    private fun calculateSalonPriceLogic(salon: Salon?, startTime: String, endTime: String): Pair<Double, Int> {
        if (salon == null || startTime.isEmpty() || endTime.isEmpty()) {
            Log.d("PRICE_DEBUG", "Datos insuficientes: salon=$salon, start=$startTime, end=$endTime")
            return Pair(0.0, 0)
        }

        return try {
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val start = timeFormat.parse(startTime)
            val end = timeFormat.parse(endTime)

            if (start == null || end == null) {
                return Pair(0.0, 0)
            }

            val diffMillis = end.time - start.time
            val hours = diffMillis / (1000.0 * 60 * 60)

            val actualHours = if (hours < 0) hours + 24 else hours

            val price = (salon.price ?: 0.0) * actualHours
            Log.d("PRICE_DEBUG", "Cálculo: ${salon.price} * $actualHours = $price")

            Pair(price, actualHours.toInt())
        } catch (e: Exception) {
            Log.e(TAG, "Error al calcular el precio del salón: ${e.message}")
            Pair(0.0, 0)
        }
    }

    private fun createReservation() {
        val selectedClient = presenter.getClientByPosition(binding.spinnerClient.selectedItemPosition)
        val selectedSalon = presenter.getSalonByPosition(binding.spinnerSalon.selectedItemPosition)
        val selectedServicio = presenter.getServicioByPosition(binding.spinnerService.selectedItemPosition)
        val date = binding.etDate.text.toString()
        val startTime = binding.etStartTime.text.toString()
        val endTime = binding.etEndTime.text.toString()

        if (!validateForm(selectedClient, selectedSalon, date, startTime, endTime)) {
            return
        }

        val serviceName = selectedServicio?.nombreServicio

        presenter.createReservation(
            selectedClient!!,
            selectedSalon!!,
            date,
            startTime,
            endTime,
            serviceName ?: "Ninguno"
        )
    }

    private fun updateReservation() {
        val selectedClient = presenter.getClientByPosition(binding.spinnerClient.selectedItemPosition)
        val selectedSalon = presenter.getSalonByPosition(binding.spinnerSalon.selectedItemPosition)
        val selectedServicio = presenter.getServicioByPosition(binding.spinnerService.selectedItemPosition)
        val date = binding.etDate.text.toString()
        val startTime = binding.etStartTime.text.toString()
        val endTime = binding.etEndTime.text.toString()

        if (!validateForm(selectedClient, selectedSalon, date, startTime, endTime)) {
            return
        }

        showLoading()

        val formattedStartTime = if (startTime.length == 5) "$startTime:00" else startTime
        val formattedEndTime = if (endTime.length == 5) "$endTime:00" else endTime

        val (newTotalSala, _) = calculateSalonPriceLogic(selectedSalon, formattedStartTime, formattedEndTime)
        val servicePrice = selectedServicio?.costo ?: 0.0
        val finalTotal = newTotalSala + servicePrice

        val editRequest = EditReservationRequest(
            idReserva = existingReservation!!.id ?: 0,
            idSala = selectedSalon!!.id ?: 0,
            fecha = date,
            horaInicio = formattedStartTime,
            horaFin = formattedEndTime,
            totalPagar = finalTotal,
            idServicio = selectedServicio?.idServicio
        )

        RetrofitClient.apiService.editReservation(editRequest).enqueue(object :
            Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                hideLoading()

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        Toast.makeText(requireContext(), "Reserva actualizada exitosamente", Toast.LENGTH_SHORT).show()
                        onReservationUpdated?.invoke()
                        dismiss()
                    } else {
                        showError("Error: ${apiResponse?.message ?: "Error desconocido"}")
                    }
                } else {
                    showError("Error del servidor: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                hideLoading()
                showError("Error de conexión: ${t.message}")
            }
        })
    }

    private fun validateForm(
        selectedClient: Client?,
        selectedSalon: Salon?,
        date: String,
        startTime: String,
        endTime: String
    ): Boolean {
        if (selectedClient == null) {
            showError("Por favor seleccione un cliente")
            return false
        }

        if (selectedSalon == null) {
            showError("Por favor seleccione un salón")
            return false
        }

        if (date.isEmpty()) {
            showError("Por favor seleccione una fecha")
            return false
        }

        if (startTime.isEmpty()) {
            showError("Por favor seleccione la hora de inicio")
            return false
        }

        if (endTime.isEmpty()) {
            showError("Por favor seleccione la hora de fin")
            return false
        }

        if (startTime >= endTime) {
            showError("La hora de fin debe ser posterior a la hora de inicio")
            return false
        }

        return true
    }

    override fun onClientsLoaded(clients: List<Client>) {
        val adapter = ClientSpinnerAdapter(requireContext(), clients)
        binding.spinnerClient.adapter = adapter

        existingReservation?.let { reservation ->
            val clientPosition = clients.indexOfFirst {
                it.email == reservation.clientEmail || it.nombre == reservation.clientName
            }
            if (clientPosition != -1) {
                binding.spinnerClient.setSelection(clientPosition)
            }

            fillFormWithExistingData(reservation)
        }
    }

    override fun onSalonsLoaded(salons: List<Salon>) {
        val adapter = SalonSpinnerAdapter(requireContext(), salons)
        binding.spinnerSalon.adapter = adapter

        existingReservation?.let { reservation ->
            val salonPosition = salons.indexOfFirst {
                it.name == reservation.salonName
            }
            if (salonPosition != -1) {
                binding.spinnerSalon.setSelection(salonPosition)
            }
        }
        updateTotalPrices()
    }

    override fun onServiciosLoaded(servicios: List<Servicio>) {
        val serviciosConNinguno = mutableListOf<Servicio>().apply {
            add(Servicio(0, "Ninguno", 0.0, "", "Activo"))
            addAll(servicios)
        }

        val adapter = ServicioSpinnerAdapter(requireContext(), serviciosConNinguno)
        binding.spinnerService.adapter = adapter

        existingReservation?.let { reservation ->
            val servicePosition = serviciosConNinguno.indexOfFirst {
                it.nombreServicio == reservation.serviceName
            }
            if (servicePosition != -1) {
                binding.spinnerService.setSelection(servicePosition)
            }
        }
        updateTotalPrices()
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
        onReservationUpdated?.invoke()
        dismiss()
        (parentFragment as? ManageReservationsFragment)?.let {
            it.presenter.refreshReservations()
        }
    }

    override fun onReservationCreationFailed(error: String) {
        showError("Error al crear la reserva: $error")
        Log.e(TAG, "Error al crear la reserva: $error")
    }

    private fun calculateTotal(salon: Salon, startTime: String, endTime: String): Double {
        return try {
            val startHour = startTime.substring(0, 2).toInt()
            val endHour = endTime.substring(0, 2).toInt()
            val hours = endHour - startHour
            salon.price ?: 0.0 * hours
        } catch (e: Exception) {
            (salon.price ?: 0.0) * 4
        }
    }
}