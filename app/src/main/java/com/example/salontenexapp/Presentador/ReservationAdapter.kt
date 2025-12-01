package com.example.salontenexapp.Presentador

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.salontenexapp.Modelo.ReservationClient
import com.example.salontenexapp.databinding.ItemReservationclientBinding

class ReservationAdapter(
    private var reservations: List<ReservationClient>,
    private val onCancelClick: (ReservationClient) -> Unit,
    private val onEditClick: (ReservationClient) -> Unit
) : RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder>() {

    inner class ReservationViewHolder(private val binding: ItemReservationclientBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reserva: ReservationClient) {
            binding.tvSalaName.text = reserva.nombreSala ?: "Sala ${reserva.idSala}"
            binding.tvDate.text = reserva.fecha
            binding.tvTime.text = "${reserva.horaInicio} - ${reserva.horaFin}"
            binding.tvService.text = "Servicio: ${reserva.nombreServicio ?: "No especificado"}"
            binding.tvStatus.text = reserva.estado
            binding.tvTotal.text = "$${reserva.totalPagar}"

            // Configurar colores según estado
            when (reserva.estado.lowercase()) {
                "confirmada" -> binding.tvStatus.setTextColor(binding.root.context.getColor(android.R.color.holo_green_dark))
                "pendiente" -> binding.tvStatus.setTextColor(binding.root.context.getColor(android.R.color.holo_orange_dark))
                "cancelada" -> binding.tvStatus.setTextColor(binding.root.context.getColor(android.R.color.holo_red_dark))
            }

            // Manejar visibilidad de botones según estado
            if (reserva.estado.equals("Cancelada", true)) {
                binding.btnCancel.isEnabled = false
                binding.btnEdit.isEnabled = false
                binding.btnCancel.alpha = 0.5f
                binding.btnEdit.alpha = 0.5f
            } else {
                binding.btnCancel.isEnabled = true
                binding.btnEdit.isEnabled = true
                binding.btnCancel.alpha = 1f
                binding.btnEdit.alpha = 1f

                binding.btnCancel.setOnClickListener { onCancelClick(reserva) }
                binding.btnEdit.setOnClickListener { onEditClick(reserva) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationViewHolder {
        val binding = ItemReservationclientBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReservationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReservationViewHolder, position: Int) {
        holder.bind(reservations[position])
    }

    override fun getItemCount(): Int = reservations.size

    fun updateList(newList: List<ReservationClient>) {
        reservations = newList
        notifyDataSetChanged()
    }
}