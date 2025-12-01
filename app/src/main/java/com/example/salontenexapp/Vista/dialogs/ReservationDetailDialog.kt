package com.example.salontenexapp.Vista.dialogs

import android.app.AlertDialog
import android.content.res.ColorStateList
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
    private var isCurrentlyCancellable = false

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
        binding.tvClientName.text = reservation.clientName ?: "N/A"
        binding.tvClientEmail.text = reservation.clientEmail ?: "N/A"
        binding.tvSalonName.text = reservation.salonName ?: "N/A"
        binding.tvServiceName.text = reservation.serviceName ?: "No especificado"
        binding.tvDate.text = reservation.getFormattedDate() ?: "N/A"
        binding.tvTime.text = reservation.calculateDuration() ?: "N/A"
        binding.tvTotalPrice.text = "$${reservation.totalPrice ?: 0}"

        val duration = calculateDuration(reservation.startTime ?: "", reservation.endTime ?: "")
        binding.tvDuration.text = duration

        when (reservation.status?.lowercase()) {
            "confirmada", "confirmed", "pendiente", "pending" -> {
                binding.tvStatus.text = if (reservation.status?.lowercase() == "pendiente") "Pendiente" else "Confirmada"
                binding.tvStatus.setBackgroundResource(if (reservation.status?.lowercase() == "pendiente") R.color.status_pending else R.color.status_confirmed)

                binding.btnCancelReservation.text = getString(R.string.cancel_reservation)
                binding.btnCancelReservation.isEnabled = true
                binding.btnEdit.isEnabled = true
                isCurrentlyCancellable = true
            }
            "cancelada", "cancelled", "cancelado" -> {
                binding.tvStatus.text = "Cancelada"
                binding.tvStatus.setBackgroundResource(R.color.status_cancelled)

                binding.btnCancelReservation.backgroundTintList = ColorStateList.valueOf(requireContext().getColor(R.color.status_confirmed))
                binding.btnCancelReservation.setTextColor(requireContext().getColor(R.color.white))
                binding.btnCancelReservation.text = getString(R.string.activar_reserva)
                binding.btnCancelReservation.isEnabled = true
                binding.btnEdit.isEnabled = false
                isCurrentlyCancellable = false
            }
            else -> {
                binding.tvStatus.text = reservation.status ?: "Desconocido"
                binding.tvStatus.setBackgroundResource(R.color.light_blue_600)
                binding.btnCancelReservation.isEnabled = false
                binding.btnEdit.isEnabled = false
                isCurrentlyCancellable = false
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
        val title = if (isCurrentlyCancellable) "Cancelar Reservación" else "Activar Reservación"
        val message = if (isCurrentlyCancellable)
            "¿Estás seguro de que deseas cancelar esta reservación?"
        else
            "¿Estás seguro de que deseas reactivar (confirmar) esta reservación?"
        val positiveButtonText = if (isCurrentlyCancellable) "Sí, cancelar" else "Sí, activar"

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButtonText) { dialog, which ->
                cancelReservation()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun cancelReservation() {
        val actionMessage = if (isCurrentlyCancellable) "cancelando" else "activando"
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
                        Toast.makeText(requireContext(), apiResponse.message ?: "Reservación actualizada exitosamente", Toast.LENGTH_SHORT).show()
                        onReservationUpdated?.invoke()
                        dismiss()
                    } else {
                        Toast.makeText(requireContext(), "Error al $actionMessage: ${apiResponse?.message ?: "Error desconocido"}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Error del servidor al $actionMessage: ${response.message()}", Toast.LENGTH_SHORT).show()
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