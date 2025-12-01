package com.example.salontenexapp.Vista

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.salontenexapp.Contrato.CreateReservationContract
import com.example.salontenexapp.Modelo.ReservationClient
import com.example.salontenexapp.Modelo.Servicio
import com.example.salontenexapp.Vista.adapter.SalonSpinnerAdapter
import com.example.salontenexapp.Vista.adapter.ServicioSpinnerAdapter
import com.example.salontenexapp.data.Salon
import com.example.salontenexapp.data.api.ClientService
import com.example.salontenexapp.data.api.RetrofitClient
import com.example.salontenexapp.databinding.FragmentCreateReservationBinding
import com.example.salontenexapp.presenters.ClientCreateReservationPresenter
import com.example.salontenexapp.util.SharedPreferencesManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateReservationFragment : Fragment(), CreateReservationContract.View {

    private var _binding: FragmentCreateReservationBinding? = null
    private val binding get() = _binding!!

    private lateinit var presenter: CreateReservationContract.Presenter
    private lateinit var prefs: SharedPreferencesManager
    private lateinit var clientService: ClientService

    private var salasList = mutableListOf<Salon>()
    private var serviciosList = mutableListOf<Servicio>()
    private var selectedSala: Salon? = null
    private var selectedServicio: Servicio? = null

    companion object {
        private const val ARG_RESERVATION = "reservation"

        fun newInstance(reservation: ReservationClient? = null): CreateReservationFragment {
            val fragment = CreateReservationFragment()
            reservation?.let {
                val args = Bundle()
                args.putParcelable(ARG_RESERVATION, it)
                fragment.arguments = args
            }
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateReservationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = SharedPreferencesManager(requireContext())
        clientService = RetrofitClient.retrofit.create(ClientService::class.java)

        // Initialize presenter
        presenter = ClientCreateReservationPresenter().apply {
            attachView(this@CreateReservationFragment)
            initializeServices(prefs, clientService)

            // Check if edit mode
            arguments?.getParcelable<ReservationClient>(ARG_RESERVATION)?.let { reservation ->
                setEditMode(true, reservation)
                binding.tvTitle.text = "Editar Reserva"
                binding.btnConfirmar.text = "Actualizar Reserva"
            }
        }

        setupUI()
        presenter.loadInitialData()
    }

    private fun setupUI() {
        // Date picker
        binding.etFecha.setOnClickListener {
            showDatePicker()
        }

        // Time pickers
        binding.etHoraInicio.setOnClickListener {
            showTimePicker(true)
        }

        binding.etHoraFin.setOnClickListener {
            showTimePicker(false)
        }

        // Buttons
        binding.btnCancelar.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.btnConfirmar.setOnClickListener {
            if (validateAndConfirm()) {
                if (presenter.isEditMode()) {
                    presenter.getExistingReservation()?.let { reservation ->
                        presenter.updateReservation(
                            reservation,
                            selectedSala!!,
                            selectedServicio,
                            binding.etFecha.text.toString(),
                            binding.etHoraInicio.text.toString() + ":00",
                            binding.etHoraFin.text.toString() + ":00"
                        )
                    }
                } else {
                    presenter.createReservation(
                        selectedSala!!,
                        selectedServicio,
                        binding.etFecha.text.toString(),
                        binding.etHoraInicio.text.toString() + ":00",
                        binding.etHoraFin.text.toString() + ":00"
                    )
                }
            }
        }
    }

    private fun validateAndConfirm(): Boolean {
        return presenter.validateForm(
            selectedSala,
            binding.etFecha.text.toString(),
            binding.etHoraInicio.text.toString() + ":00",
            binding.etHoraFin.text.toString() + ":00"
        )
    }

    // View implementation
    override fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnConfirmar.isEnabled = false
        binding.btnCancelar.isEnabled = false
    }

    override fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnConfirmar.isEnabled = true
        binding.btnCancelar.isEnabled = true
    }

    override fun showError(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_LONG).show()
        Log.e("CreateReservation", message)
    }

    override fun showSuccess(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
        Log.d("CreateReservation", message)
    }

    override fun showSalaInfo(sala: Salon) {
        binding.salaInfo.visibility = View.VISIBLE
        binding.ivSalaImage.visibility = View.VISIBLE

        binding.tvSalaNombre.text = sala.name
        binding.tvSalaCapacidad.text = "Capacidad: ${sala.capacity} personas"
        binding.tvSalaPrecio.text = "Precio: $${"%.2f".format(sala.price)}"
        binding.tvSalaDescripcion.text = sala.description

        val fullImageUrl = sala.getFullImageUrl()
        if (fullImageUrl.isNotEmpty()) {
            Glide.with(requireContext())
                .load(fullImageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(binding.ivSalaImage)
        } else {
            binding.ivSalaImage.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    override fun hideSalaInfo() {
        binding.salaInfo.visibility = View.GONE
        binding.ivSalaImage.visibility = View.GONE
    }

    override fun updateTotal(total: Double) {
        binding.tvTotalPagar.text = "$${"%.2f".format(total)}"
    }

    override fun setSalas(salas: List<Salon>) {
        salasList.clear()
        salasList.addAll(salas)
        setupSalaSpinner()
    }

    override fun setServicios(servicios: List<Servicio>) {
        serviciosList.clear()
        serviciosList.addAll(servicios)
        setupServicioSpinner()
    }

    override fun setExistingReservation(reservation: ReservationClient) {
        loadExistingData(reservation)
    }

    override fun closeFragment() {
        requireActivity().supportFragmentManager.popBackStack()
    }

    private fun setupSalaSpinner() {
        val adapter = SalonSpinnerAdapter(requireContext(), salasList)
        binding.spinnerSala.adapter = adapter

        binding.spinnerSala.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position >= 0 && position < salasList.size) {
                    selectedSala = salasList[position]
                    presenter.onSalaSelected(selectedSala!!)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupServicioSpinner() {
        val serviciosConNinguno = mutableListOf<Servicio>().apply {
            add(Servicio(0, "Sin servicio adicional", 0.0, "", "Activo"))
            addAll(serviciosList)
        }

        val adapter = ServicioSpinnerAdapter(requireContext(), serviciosConNinguno)
        binding.spinnerServicio.adapter = adapter

        binding.spinnerServicio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val servicioSeleccionado = serviciosConNinguno[position]
                selectedServicio = if (servicioSeleccionado.idServicio == 0) null else servicioSeleccionado
                presenter.onServicioSelected(selectedServicio)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun loadExistingData(reservation: ReservationClient) {
        Log.d("CreateReservation", "Cargando datos existentes para reserva: $reservation")

        val salaIndex = salasList.indexOfFirst { it.id == reservation.idSala }
        if (salaIndex >= 0) {
            binding.spinnerSala.setSelection(salaIndex)
        }

        reservation.idServicio?.let { servicioId ->
            val servicioIndex = serviciosList.indexOfFirst { it.idServicio == servicioId }
            if (servicioIndex >= 0) {
                binding.spinnerServicio.setSelection(servicioIndex + 1)
            } else {
                binding.spinnerServicio.setSelection(0)
            }
        } ?: run {
            binding.spinnerServicio.setSelection(0)
        }

        binding.etFecha.setText(reservation.fecha)
        binding.etHoraInicio.setText(reservation.horaInicio.substring(0, 5))
        binding.etHoraFin.setText(reservation.horaFin.substring(0, 5))

        presenter.onDateSelected(reservation.fecha)
        presenter.onStartTimeSelected(reservation.horaInicio)
        presenter.onEndTimeSelected(reservation.horaFin)
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = Calendar.getInstance().apply {
                set(selectedYear, selectedMonth, selectedDay)
            }

            if (selectedDate.before(Calendar.getInstance())) {
                showError("No puedes seleccionar una fecha pasada")
                return@DatePickerDialog
            }

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fecha = dateFormat.format(selectedDate.time)
            binding.etFecha.setText(fecha)
            presenter.onDateSelected(fecha)
        }, year, month, day)

        datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
        datePicker.show()
    }

    private fun showTimePicker(isStartTime: Boolean) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePicker = TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
            val time = String.format(Locale.getDefault(), "%02d:%02d:00", selectedHour, selectedMinute)

            if (isStartTime) {
                binding.etHoraInicio.setText(time.substring(0, 5))
                presenter.onStartTimeSelected(time)
            } else {
                binding.etHoraFin.setText(time.substring(0, 5))
                presenter.onEndTimeSelected(time)
            }

            presenter.validateTimeRange(
                binding.etHoraInicio.text.toString() + ":00",
                binding.etHoraFin.text.toString() + ":00"
            )
        }, hour, minute, true)

        timePicker.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detachView()
        _binding = null
    }
}