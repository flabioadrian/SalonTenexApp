// Archivo: Vista/AdminDashboardFragment.kt
package com.example.salontenexapp.Vista

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.salontenexapp.data.Reservation
import com.example.salontenexapp.databinding.FragmentAdminDashboardBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.salontenexapp.data.api.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.salontenexapp.R
import kotlinx.coroutines.withContext
import android.util.Log
import com.example.salontenexapp.Vista.adapter.RecentReservationsAdapter

class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var recentReservationsAdapter: RecentReservationsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadDashboardData()
    }

    private fun setupRecyclerView() {
        recentReservationsAdapter = RecentReservationsAdapter()
        binding.rvRecentReservations.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recentReservationsAdapter
        }
    }

    private fun loadDashboardData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = RetrofitClient.apiService
                val recentReservations = apiService.getRecentReservations()

                withContext(Dispatchers.Main) {
                    if (recentReservations.isNotEmpty()) {
                        recentReservationsAdapter.submitList(recentReservations)
                        Log.d("API", "Reservas cargadas exitosamente: ${recentReservations.size}")
                    } else {
                        Log.d("API", "La API devolvió una lista de reservas vacía.")
                    }
                    val todayReservations = 12
                    val monthlyRevenue = 8450.0
                    binding.tvTodayReservations.text = todayReservations.toString()
                    binding.tvMonthlyRevenue.text = "$${monthlyRevenue}"
                }

            } catch (e: Exception) {
                Log.e("API_ERROR", "Error al cargar datos del dashboard: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    binding.tvTodayReservations.text = "N/A"
                    binding.tvMonthlyRevenue.text = "N/A"
                }
            }
        }
    }

    // Métodos para los clicks de las acciones rápidas
    fun onManageReservationsClick(view: View) {
        // Navegar al fragment de gestión de reservas
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ManageReservationsFragment())
            .addToBackStack(null)
            .commit()
    }

    fun onManageSalonsClick(view: View) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ManageSalonsFragment())
            .addToBackStack(null)
            .commit()
    }

    fun onManageServicesClick(view: View) {
        // Navegar al fragment de gestión de servicios
        // requireActivity().supportFragmentManager.beginTransaction()...
    }

    fun onManageAdminsClick(view: View) {
        // Navegar al fragment de gestión de administradores
        // requireActivity().supportFragmentManager.beginTransaction()...
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}