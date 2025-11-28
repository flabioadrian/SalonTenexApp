package com.example.salontenexapp.Vista.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.salontenexapp.data.Salon

class SalonSpinnerAdapter(context: Context, private val salons: List<Salon>) :
    ArrayAdapter<Salon>(context, android.R.layout.simple_spinner_item, salons) {

    override fun getItem(position: Int): Salon? {
        return salons[position]
    }

    override fun getItemId(position: Int): Long {
        return salons[position].id.toLong()
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

        view.text = salons[position].name
        return view
    }
}