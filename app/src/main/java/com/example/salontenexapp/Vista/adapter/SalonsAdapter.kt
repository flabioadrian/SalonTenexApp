package com.example.salontenexapp.Vista.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.salontenexapp.R
import com.example.salontenexapp.databinding.ItemSalonBinding
import com.example.salontenexapp.data.Salon
class SalonsAdapter(
    private val onEditClick: (Salon) -> Unit
) : ListAdapter<Salon, SalonsAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSalonBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val salon = getItem(position)
        holder.bind(salon)
    }

    inner class ViewHolder(private val binding: ItemSalonBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(salon: Salon) {
            binding.salon = salon

            binding.executePendingBindings()
            binding.tvAvailable.text = "Disponible"
            binding.tvAvailable.setTextColor(ContextCompat.getColor(binding.root.context, R.color.green))
            binding.availabilityIndicator.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.green))

            binding.btnEdit.setOnClickListener { onEditClick(salon) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Salon>() {
        override fun areItemsTheSame(oldItem: Salon, newItem: Salon): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Salon, newItem: Salon): Boolean {
            return oldItem == newItem
        }
    }
}