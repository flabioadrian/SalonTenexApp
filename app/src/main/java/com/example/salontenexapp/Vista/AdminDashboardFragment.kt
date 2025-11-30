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

        }
        binding.rvRecentReservations.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recentReservationsAdapter
        }
    }

    private fun loadDashboardData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = RetrofitClient.apiService
                val response = apiService.getReservations()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val recentReservations = response.body()
                        if (!recentReservations.isNullOrEmpty()) {
                            recentReservationsAdapter.submitList(recentReservations)
                        } else {
                            recentReservationsAdapter.submitList(emptyList())
                        }
                    } else {
                        recentReservationsAdapter.submitList(emptyList())
                    }

                    val todayReservations = 12
                    val monthlyRevenue = 8450.0
                    binding.tvTodayReservations.text = todayReservations.toString()
                    binding.tvMonthlyRevenue.text = "$${monthlyRevenue}"
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.tvTodayReservations.text = "N/A"
                    binding.tvMonthlyRevenue.text = "N/A"
                    recentReservationsAdapter.submitList(emptyList())
                }
            }
        }
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