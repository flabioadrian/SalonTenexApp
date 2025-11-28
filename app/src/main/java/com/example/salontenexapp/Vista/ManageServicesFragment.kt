package com.example.salontenexapp.Vista

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.salontenexapp.databinding.FragmentManageServicesBinding // Asegúrate de que el XML exista

class ManageServicesFragment : Fragment() {

    private var _binding: FragmentManageServicesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageServicesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Lógica para que el Admin gestione la lista de servicios
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}