package com.example.salontenexapp.Vista.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.salontenexapp.R
import com.example.salontenexapp.data.Reservation
import com.google.android.material.button.MaterialButton

class ReservationClientAdapter(
    private var reservations: List<Reservation>,
    private val onCancelClick: (Reservation) -> Unit,
    private val onEditClick: (Reservation) -> Unit
) : RecyclerView.Adapter<ReservationClientAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSalaName: TextView = itemView.findViewById(R.id.tvSalaName)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvService: TextView = itemView.findViewById(R.id.tvService)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvTotal: TextView = itemView.findViewById(R.id.tvTotal)
        val btnCancel: MaterialButton = itemView.findViewById(R.id.btnCancel)
        val btnEdit: MaterialButton = itemView.findViewById(R.id.btnEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reservation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reservation = reservations[position]

        holder.tvSalaName.text = reservation.salonName
        holder.tvDate.text = reservation.date
        holder.tvTime.text = "${reservation.startTime} - ${reservation.endTime}"
        holder.tvService.text = "Servicio: ${reservation.serviceName ?: "No especificado"}"
        holder.tvStatus.text = reservation.status
        holder.tvTotal.text = "$${reservation.totalPrice}"

        when (reservation.status.lowercase()) {
            "confirmado" -> holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.green))
            "pendiente" -> holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.orange))
            "cancelado" -> holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.red))
            else -> holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.text_dark_brown))
        }

        holder.btnCancel.setOnClickListener {
            onCancelClick(reservation)
        }

        holder.btnEdit.setOnClickListener {
            onEditClick(reservation)
        }

        if (reservation.status.equals("cancelado", ignoreCase = true)) {
            holder.btnCancel.isEnabled = false
            holder.btnEdit.isEnabled = false
            holder.btnCancel.alpha = 0.5f
            holder.btnEdit.alpha = 0.5f
        } else {
            holder.btnCancel.isEnabled = true
            holder.btnEdit.isEnabled = true
            holder.btnCancel.alpha = 1f
            holder.btnEdit.alpha = 1f
        }
    }

    override fun getItemCount() = reservations.size

    fun updateList(newList: List<Reservation>) {
        reservations = newList
        notifyDataSetChanged()
    }
}