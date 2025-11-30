package com.example.salontenexapp.Vista.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.salontenexapp.Modelo.Servicio

class ServicioSpinnerAdapter(context: Context, private val servicios: List<Servicio>) :
    ArrayAdapter<Servicio>(context, android.R.layout.simple_spinner_item, servicios) {

    override fun getItem(position: Int): Servicio? {
        return servicios.getOrNull(position)
    }

    override fun getItemId(position: Int): Long {
        return servicios.getOrNull(position)?.idServicio?.toLong() ?: -1L
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = (convertView ?: LayoutInflater.from(context).inflate(
            android.R.layout.simple_spinner_dropdown_item, parent, false
        )) as TextView

        val servicio = servicios.getOrNull(position)
        view.text = servicio?.let {
            it.nombreServicio
        } ?: "Seleccionar servicio"

        return view
    }
}