package com.example.salontenexapp.Vista.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.salontenexapp.data.Reservation
import com.example.salontenexapp.R
import com.example.salontenexapp.databinding.ItemReservationBinding

// ReservationsAdapter.kt
class ReservationsAdapter(
    private val onItemClick: (Reservation) -> Unit
) : ListAdapter<Reservation, ReservationsAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReservationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reservation = getItem(position)
        holder.bind(reservation)

        holder.itemView.setOnClickListener {
            onItemClick(reservation)
        }
    }

    inner class ViewHolder(private val binding: ItemReservationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(reservation: Reservation) {
            // Usar los campos reales del modelo Reservation
            itemView.findViewById<TextView>(R.id.tvClientName).text = reservation.getDisplayClientName()
            itemView.findViewById<TextView>(R.id.tvSalonName).text = reservation.salonName
            itemView.findViewById<TextView>(R.id.tvDate).text = reservation.getFormattedDate()
            itemView.findViewById<TextView>(R.id.tvTime).text = reservation.calculateDuration()
            itemView.findViewById<TextView>(R.id.tvStatus).text = reservation.status
            itemView.findViewById<TextView>(R.id.tvTotalPrice).text =
                "$${reservation.totalPrice}"
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Reservation>() {
            override fun areItemsTheSame(oldItem: Reservation, newItem: Reservation): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Reservation, newItem: Reservation): Boolean {
                return oldItem == newItem
            }
        }
    }
}