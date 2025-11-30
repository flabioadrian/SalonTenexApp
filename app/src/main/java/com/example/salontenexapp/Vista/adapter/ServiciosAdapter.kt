package com.example.salontenexapp.Adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.salontenexapp.Modelo.Servicio
import com.example.salontenexapp.R

class ServiciosAdapter(
    private val onEditClick: (Servicio) -> Unit
) : ListAdapter<Servicio, ServiciosAdapter.ServicioViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service, parent, false)
        return ServicioViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServicioViewHolder, position: Int) {
        val servicio = getItem(position)
        holder.bind(servicio)
    }

    inner class ServicioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textServiceName: TextView = itemView.findViewById(R.id.textServiceName)
        private val textServiceStatus: TextView = itemView.findViewById(R.id.textServiceStatus)
        private val textServiceDescription: TextView = itemView.findViewById(R.id.textServiceDescription)
        private val textServiceCost: TextView = itemView.findViewById(R.id.textServiceCost)
        private val buttonEdit: Button = itemView.findViewById(R.id.buttonEdit)

        fun bind(servicio: Servicio) {
            textServiceName.text = servicio.nombreServicio
            textServiceDescription.text = servicio.descripcion
            textServiceCost.text = "$${servicio.costo}"

            // Configurar estado
            textServiceStatus.text = servicio.estado
            when (servicio.estado) {
                "Activo" -> {
                    textServiceStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.green))
                }
                "Desactivado" -> {
                    textServiceStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.red))
                }
                else -> {
                    textServiceStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.orange))
                }
            }

            buttonEdit.setOnClickListener {
                onEditClick(servicio)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Servicio>() {
        override fun areItemsTheSame(oldItem: Servicio, newItem: Servicio): Boolean {
            return oldItem.idServicio == newItem.idServicio
        }

        override fun areContentsTheSame(oldItem: Servicio, newItem: Servicio): Boolean {
            return oldItem == newItem
        }
    }
}