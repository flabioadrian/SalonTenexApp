package com.example.salontenexapp.Vista

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.salontenexapp.Contrato.ManagerReservationContract
import com.example.salontenexapp.Presentador.ManageReservationsPresenter
import com.example.salontenexapp.Vista.adapter.RecentReservationsAdapter
import com.example.salontenexapp.Vista.dialogs.CreateReservationDialog
import com.example.salontenexapp.Vista.dialogs.ReservationDetailDialog
import com.example.salontenexapp.data.Reservation
import com.example.salontenexapp.data.api.APIService
import com.example.salontenexapp.data.api.RetrofitClient
import com.example.salontenexapp.databinding.FragmentManageReservationsBinding
import com.example.salontenexapp.util.SharedPreferencesManager
import retrofit2.Response

class ManageReservationsFragment : Fragment(), ManagerReservationContract.ManageReservationsView {

    private var _binding: FragmentManageReservationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var reservationsAdapter: RecentReservationsAdapter
    internal lateinit var presenter: ManageReservationsPresenter
    private lateinit var prefsManager: SharedPreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageReservationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsManager = SharedPreferencesManager(requireContext())

        if (!prefsManager.isLoggedIn()) {
            showSessionExpired()
            return
        }

        setupUI()
        loadReservations()
    }

    private fun setupUI() {
        val apiService = RetrofitClient.retrofit.create(APIService::class.java)
        presenter = ManageReservationsPresenter(this, apiService)

        setupSpinner()
        setupRecyclerView()
        setupClickListeners()
    }

    private fun setupSpinner() {
        val filterOptions = arrayOf("Todas", "Pendientes", "Confirmadas", "Canceladas")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, filterOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFilter.adapter = adapter

        binding.spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedFilter = parent?.getItemAtPosition(position).toString()
                presenter.filterReservations(selectedFilter)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupRecyclerView() {
        reservationsAdapter = RecentReservationsAdapter { reservation ->
            showReservationDetails(reservation)
        }

        binding.rvReservations.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reservationsAdapter
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            presenter.refreshReservations()
        }
    }

    private fun setupClickListeners() {
        binding.fabAddReservation.setOnClickListener {
            showCreateReservationDialog()
        }
    }

    private fun loadReservations() {
        presenter.loadReservations()
    }

    override fun onReservationsLoaded(reservations: Response<List<Reservation>>) {
        binding.swipeRefreshLayout.isRefreshing = false

        if (reservations.isSuccessful) {
            val reservationList = reservations.body() ?: emptyList()
            reservationsAdapter.submitList(reservationList)
            updateUIState(reservationList)
        } else {
            showError("Error al cargar las reservaciones: ${reservations.code()}")
            showEmptyState()
        }
    }

    override fun onFilteredReservationsLoaded(reservations: List<Reservation>) {
        binding.swipeRefreshLayout.isRefreshing = false
        reservationsAdapter.submitList(reservations)
        updateUIState(reservations)
    }

    override fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvReservations.visibility = View.GONE
        binding.emptyState.visibility = View.GONE
    }

    override fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    override fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun showSessionExpired() {
        if (!isAdded) return

        Toast.makeText(
            requireContext(),
            "La sesión ha expirado. Por favor inicie sesión nuevamente.",
            Toast.LENGTH_LONG
        ).show()

        prefsManager.logout()

        val intent = Intent(requireActivity(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }

    private fun updateUIState(reservations: List<Reservation>) {
        if (reservations.isEmpty()) {
            showEmptyState()
        } else {
            showReservationsList()
        }
    }

    private fun showReservationsList() {
        binding.rvReservations.visibility = View.VISIBLE
        binding.emptyState.visibility = View.GONE
    }

    private fun showEmptyState() {
        binding.rvReservations.visibility = View.GONE
        binding.emptyState.visibility = View.VISIBLE
    }

    private fun showReservationDetails(reservation: Reservation) {
        ReservationDetailDialog.newInstance(reservation)
            .show(parentFragmentManager, "ReservationDetailDialog")
    }

    private fun showCreateReservationDialog() {
        CreateReservationDialog().show(parentFragmentManager, "CreateReservationDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}