package com.example.salontenexapp.Vista.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.fragment.app.DialogFragment
import com.example.salontenexapp.data.Reservation
import com.example.salontenexapp.R
import com.example.salontenexapp.databinding.DialogReservationDetailBinding

class ReservationDetailDialog : DialogFragment() {

    private var _binding: DialogReservationDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var reservation: Reservation

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
        val reservation = arguments?.let { bundle ->
            BundleCompat.getParcelable(bundle, ARG_RESERVATION, Reservation::class.java)
        }
        reservation?.let { res ->
            // Usar los campos correctos del API
            binding.tvClientName.text = res.clientName
            binding.tvClientEmail.text = res.clientEmail
            binding.tvSalonName.text = res.salonName
            binding.tvServiceName.text = res.serviceName ?: "No especificado"
            binding.tvDate.text = res.getFormattedDate()
            binding.tvTime.text = res.calculateDuration()
            binding.tvTotalPrice.text = "$${res.totalPrice}"

            // Calcular duración aproximada
            val duration = calculateDuration(res.startTime, res.endTime)
            binding.tvDuration.text = duration

            // Configurar estado
            when (res.status.lowercase()) {
                "confirmada" -> {
                    binding.tvStatus.text = "Confirmada"
                    binding.tvStatus.setBackgroundResource(R.color.status_confirmed)
                }
                "pendiente" -> {
                    binding.tvStatus.text = "Pendiente"
                    binding.tvStatus.setBackgroundResource(R.color.status_pending)
                }
                "cancelada" -> {
                    binding.tvStatus.text = "Cancelada"
                    binding.tvStatus.setBackgroundResource(R.color.status_cancelled)
                }
                else -> {
                    binding.tvStatus.text = res.status
                    binding.tvStatus.setBackgroundResource(R.color.light_blue_600)
                }
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
            "4 horas" // Valor por defecto
        }
    }

    private fun setupClickListeners() {
        binding.btnEdit.setOnClickListener {
            // Abrir diálogo de edición
            dismiss()
        }

        binding.btnCancelReservation.setOnClickListener {
            // Mostrar confirmación y cancelar reserva
            showCancelConfirmation()
        }

        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }

    private fun showCancelConfirmation() {
        // Implementar diálogo de confirmación
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