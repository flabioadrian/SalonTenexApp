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
import com.example.salontenexapp.Vista.adapter.RecentReservationsAdapter
import java.text.SimpleDateFormat
import java.util.*

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
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        recentReservationsAdapter = RecentReservationsAdapter { reservation ->
            // Manejar clic en reservación si es necesario
        }
        binding.rvRecentReservations.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recentReservationsAdapter
        }
    }

    fun loadDashboardData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = RetrofitClient.apiService
                val response = apiService.getReservations()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val allReservations = response.body()
                        if (!allReservations.isNullOrEmpty()) {
                            recentReservationsAdapter.submitList(allReservations)

                            // Calcular métricas usando los datos de las reservaciones
                            calculateDashboardMetrics(allReservations)
                        } else {
                            recentReservationsAdapter.submitList(emptyList())
                            setDefaultMetrics()
                        }
                    } else {
                        recentReservationsAdapter.submitList(emptyList())
                        setDefaultMetrics()
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    setDefaultMetrics()
                    recentReservationsAdapter.submitList(emptyList())
                }
            }
        }
    }

    private fun calculateDashboardMetrics(reservations: List<Reservation>) {
        // Obtener fecha actual
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

        // Filtrar reservaciones de hoy
        val todayReservations = reservations.filter { reservation ->
            reservation.date == currentDate
        }

        // Filtrar reservaciones del mes actual y calcular ingresos
        val monthlyRevenue = reservations
            .filter { reservation ->
                reservation.date?.startsWith(currentMonth) == true
            }
            .sumOf { reservation ->
                reservation.totalPrice ?: 0.0
            }

        // Actualizar UI
        binding.tvTodayReservations.text = todayReservations.size.toString()
        binding.tvMonthlyRevenue.text = "$${monthlyRevenue}"
    }

    private fun setDefaultMetrics() {
        binding.tvTodayReservations.text = "0"
        binding.tvMonthlyRevenue.text = "$0.0"
    }

    private fun setupClickListeners() {
        binding.cardReservations.setOnClickListener {
            openNavigationDrawerAndSelectItem(R.id.nav_manage_reservations)
        }

        binding.cardSalons.setOnClickListener {
            openNavigationDrawerAndSelectItem(R.id.nav_manage_salons)
        }

        binding.cardServices.setOnClickListener {
            openNavigationDrawerAndSelectItem(R.id.nav_manage_services)
        }
    }

    private fun openNavigationDrawerAndSelectItem(menuItemId: Int) {
        val mainActivity = requireActivity() as? MainActivity
        mainActivity?.let { activity ->
            if (activity.isDrawerOpen()) {
                activity.closeDrawer()
            }

            activity.selectNavigationItem(menuItemId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}