package com.example.salontenexapp.Vista

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.salontenexapp.databinding.FragmentManageAdminsBinding // Asegúrate de que el XML exista

class ManageAdminsFragment : Fragment() {

    private var _binding: FragmentManageAdminsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageAdminsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Lógica para que el Admin gestione a otros administradores
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}