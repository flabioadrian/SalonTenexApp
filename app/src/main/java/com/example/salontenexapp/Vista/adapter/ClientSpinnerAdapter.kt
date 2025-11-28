// ClientSpinnerAdapter.kt
package com.example.salontenexapp.Vista.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.salontenexapp.Modelo.Client

class ClientSpinnerAdapter(context: Context, private val clients: List<Client>) :
    ArrayAdapter<Client>(context, android.R.layout.simple_spinner_item, clients) {

    override fun getItem(position: Int): Client? {
        return clients.getOrNull(position)
    }

    override fun getItemId(position: Int): Long {
        return clients.getOrNull(position)?.id?.toLong() ?: -1L
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

        val client = clients.getOrNull(position)
        view.text = client?.let {
            it.name?.let { name -> "$name (${it.email})" } ?: it.email
        } ?: "Seleccionar cliente"

        return view
    }
}