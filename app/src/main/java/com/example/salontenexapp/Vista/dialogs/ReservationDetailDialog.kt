package com.example.salontenexapp.Vista.dialogs

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.BundleCompat
import androidx.fragment.app.DialogFragment
import com.example.salontenexapp.data.Reservation
import com.example.salontenexapp.R
import com.example.salontenexapp.databinding.DialogReservationDetailBinding
import com.example.salontenexapp.data.api.RetrofitClient
import com.example.salontenexapp.Modelo.CancelReservationRequest
import com.example.salontenexapp.Modelo.ApiResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReservationDetailDialog : DialogFragment() {

    private var _binding: DialogReservationDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var reservation: Reservation
    private var onReservationUpdated: (() -> Unit)? = null

    // Callback para notificar cuando se actualiza una reservación
    fun setOnReservationUpdatedListener(listener: () -> Unit) {
        onReservationUpdated = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reservation = arguments?.let { bundle ->
            BundleCompat.getParcelable(bundle, ARG_RESERVATION, Reservation::class.java)
        } ?: throw IllegalStateException("Reservation required")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogReservationDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        // Usar la reservación de la variable de clase
        binding.tvClientName.text = reservation.clientName ?: "N/A"
        binding.tvClientEmail.text = reservation.clientEmail ?: "N/A"
        binding.tvSalonName.text = reservation.salonName ?: "N/A"
        binding.tvServiceName.text = reservation.serviceName ?: "No especificado"
        binding.tvDate.text = reservation.getFormattedDate() ?: "N/A"
        binding.tvTime.text = reservation.calculateDuration() ?: "N/A"
        binding.tvTotalPrice.text = "$${reservation.totalPrice ?: 0}"

        // Calcular duración aproximada
        val duration = calculateDuration(reservation.startTime ?: "", reservation.endTime ?: "")
        binding.tvDuration.text = duration

        // Configurar estado
        when (reservation.status?.lowercase()) {
            "confirmada", "confirmed" -> {
                binding.tvStatus.text = "Confirmada"
                binding.tvStatus.setBackgroundResource(R.color.status_confirmed)
            }
            "pendiente", "pending" -> {
                binding.tvStatus.text = "Pendiente"
                binding.tvStatus.setBackgroundResource(R.color.status_pending)
            }
            "cancelada", "cancelled", "cancelado" -> {
                binding.tvStatus.text = "Cancelada"
                binding.tvStatus.setBackgroundResource(R.color.status_cancelled)
                // Deshabilitar botones si está cancelada
                binding.btnCancelReservation.isEnabled = false
                binding.btnEdit.isEnabled = false
            }
            else -> {
                binding.tvStatus.text = reservation.status ?: "Desconocido"
                binding.tvStatus.setBackgroundResource(R.color.light_blue_600)
            }
        }
    }

    private fun calculateDuration(start: String, end: String): String {
        return try {
            val startHour = start.substring(0, 2).toInt()
            val endHour = end.substring(0, 2).toInt()
            val duration = endHour - startHour
            "$duration horas"
        } catch (e: Exception) {
            "Duración no disponible"
        }
    }

    private fun setupClickListeners() {
        binding.btnEdit.setOnClickListener {
            openEditReservation()
        }

        binding.btnCancelReservation.setOnClickListener {
            showCancelConfirmation()
        }

        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }

    private fun showCancelConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Cancelar Reservación")
            .setMessage("¿Estás seguro de que deseas cancelar esta reservación?")
            .setPositiveButton("Sí, cancelar") { dialog, which ->
                cancelReservation()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun cancelReservation() {
        // Mostrar loading
        binding.btnCancelReservation.isEnabled = false

        val cancelRequest = CancelReservationRequest(
            idReserva = reservation.id ?: 0
        )

        RetrofitClient.apiService.cancelReservation(cancelRequest).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                binding.btnCancelReservation.isEnabled = true

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        Toast.makeText(requireContext(), "Reservación cancelada exitosamente", Toast.LENGTH_SHORT).show()
                        // Notificar que la reservación fue actualizada
                        onReservationUpdated?.invoke()
                        dismiss()
                    } else {
                        Toast.makeText(requireContext(), "Error: ${apiResponse?.message ?: "Error desconocido"}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Error del servidor: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                binding.btnCancelReservation.isEnabled = true
                Toast.makeText(requireContext(), "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun openEditReservation() {
        val editDialog = CreateReservationDialog.newInstance(reservation)
        editDialog.setOnReservationUpdatedListener {
            // Notificar que la reservación fue actualizada
            onReservationUpdated?.invoke()
        }
        editDialog.show(parentFragmentManager, "EditReservationDialog")
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_RESERVATION = "reservation"

        fun newInstance(reservation: Reservation): ReservationDetailDialog {
            val fragment = ReservationDetailDialog()
            val args = Bundle().apply {
                putParcelable(ARG_RESERVATION, reservation)
            }
            fragment.arguments = args
            return fragment
        }
    }
}