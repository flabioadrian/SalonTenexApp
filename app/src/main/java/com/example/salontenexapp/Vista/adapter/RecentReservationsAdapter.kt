package com.example.salontenexapp.Vista.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.salontenexapp.databinding.ItemRecentReservationBinding
import com.example.salontenexapp.data.Reservation
import com.example.salontenexapp.R

class RecentReservationsAdapter : ListAdapter<Reservation, RecentReservationsAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecentReservationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reservation = getItem(position)
        holder.bind(reservation)
    }

    class ViewHolder(private val binding: ItemRecentReservationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reservation: Reservation) {
            binding.tvClientName.text = reservation.clientEmail ?: "Cliente"
            binding.tvSalonName.text = reservation.salonName ?: "Salón"
            binding.tvDate.text = "${reservation.date} (${reservation.startTime} - ${reservation.endTime})"
            binding.tvService.text = reservation.serviceName ?: "Servicio"
            binding.tvPrice.text = "$${reservation.totalPrice}"

            val statusColor = when (reservation.status) {
                "confirmed" -> R.color.light_blue_600
                "pending" -> R.color.button_golden_brown
                "cancelled" -> R.color.error_red
                else -> R.color.text_medium_brown
            }
            binding.tvStatus.setTextColor(binding.root.context.getColor(statusColor))
            binding.tvStatus.text = reservation.status
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Reservation>() {
        override fun areItemsTheSame(oldItem: Reservation, newItem: Reservation): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Reservation, newItem: Reservation): Boolean {
            return oldItem == newItem
        }
    }
}