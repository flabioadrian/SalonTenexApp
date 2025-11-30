package com.example.salontenexapp.Vista.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.salontenexapp.R
import com.example.salontenexapp.data.ReservationClient
import com.google.android.material.button.MaterialButton

class ClientReservationAdapter(
    private var reservations: List<ReservationClient>,
    private val onCancelClick: (ReservationClient) -> Unit
) : RecyclerView.Adapter<ClientReservationAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSalaName: TextView = itemView.findViewById(R.id.tvSalaName)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvService: TextView = itemView.findViewById(R.id.tvService)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvTotal: TextView = itemView.findViewById(R.id.tvTotal)

        val btnCancel: MaterialButton? = itemView.findViewById(R.id.btnCancel)
        val btnEdit: MaterialButton? = itemView.findViewById(R.id.btnEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reservation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reservation = reservations[position]
        holder.tvSalaName.text = reservation.nombre_sala
        holder.tvDate.text = reservation.fecha
        holder.tvTime.text = "${reservation.hora_inicio} - ${reservation.hora_fin}"
        holder.tvService.text = "Servicio: ${reservation.nombre_servicio ?: "No especificado"}"
        holder.tvStatus.text = reservation.estado
        holder.tvTotal.text = "$${reservation.total_pagar}"

        when (reservation.estado.lowercase()) {
            "confirmado" -> holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.green))
            "pendiente" -> holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.orange))
            "cancelado" -> holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.red))
            else -> holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.text_dark_brown))
        }

        holder.btnEdit?.visibility = View.GONE

        holder.btnCancel?.setOnClickListener {
            onCancelClick(reservation)
        }

        val isCancelled = reservation.estado.equals("cancelado", ignoreCase = true)

        holder.btnCancel?.isEnabled = !isCancelled
        holder.btnCancel?.alpha = if (isCancelled) 0.5f else 1f
        holder.btnCancel?.visibility = if (isCancelled) View.GONE else View.VISIBLE
    }

    override fun getItemCount() = reservations.size

    fun updateList(newList: List<ReservationClient>) {
        reservations = newList
        notifyDataSetChanged()
    }
}