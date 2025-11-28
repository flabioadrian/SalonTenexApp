package com.example.salontenexapp.Vista

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.salontenexapp.Contrato.ManagerReservationContract
import com.example.salontenexapp.data.Reservation
import com.example.salontenexapp.Presentador.ManageReservationsPresenter
import com.example.salontenexapp.Vista.adapter.ReservationsAdapter
import com.example.salontenexapp.Vista.dialogs.CreateReservationDialog
import com.example.salontenexapp.Vista.dialogs.ReservationDetailDialog
import com.example.salontenexapp.data.api.APIService
import com.example.salontenexapp.data.api.RetrofitClient
import com.example.salontenexapp.databinding.FragmentManageReservationsBinding
import com.example.salontenexapp.util.SharedPreferencesManager

class ManageReservationsFragment : Fragment(), ManagerReservationContract.ManageReservationsView {

    private var _binding: FragmentManageReservationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var reservationsAdapter: ReservationsAdapter
    private lateinit var presenter: ManageReservationsPresenter

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

        // Inicializar presenter (necesitarás pasar la instancia de APIService)
        val apiService = RetrofitClient.retrofit.create(APIService::class.java)
            presenter = ManageReservationsPresenter(this, apiService)

        setupSpinner()
        setupRecyclerView()
        setupClickListeners()
        loadReservations()
    }

    private fun setupSpinner() {
        val filterOptions = arrayOf("Todas", "Pendientes", "Confirmadas", "Canceladas")
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, filterOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFilter.adapter = adapter

        // Listener para el filtro
        binding.spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedFilter = parent?.getItemAtPosition(position).toString()
                presenter.filterReservations(selectedFilter)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupRecyclerView() {
        reservationsAdapter = ReservationsAdapter { reservation ->
            showReservationDetails(reservation)
        }

        binding.rvReservations.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reservationsAdapter
        }

        // Configurar SwipeRefreshLayout si lo tienes en tu layout
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

    // Implementación de ManageReservationsView
    override fun onReservationsLoaded(reservations: List<Reservation>) {
        reservationsAdapter.submitList(reservations)

        // Mostrar/ocultar empty state
        if (reservations.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.rvReservations.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.rvReservations.visibility = View.VISIBLE
        }
    }

    override fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvReservations.visibility = View.GONE
    }

    override fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.rvReservations.visibility = View.VISIBLE
        binding.swipeRefreshLayout.isRefreshing = false
    }

    override fun showError(message: String) {
        // Mostrar error con Snackbar o Toast
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_LONG).show()

        // Mostrar empty state con mensaje de error
        binding.emptyState.visibility = View.VISIBLE
        binding.rvReservations.visibility = View.GONE
    }

    private fun showReservationDetails(reservation: Reservation) {
        ReservationDetailDialog.newInstance(reservation)
            .show(parentFragmentManager, "ReservationDetailDialog")
    }

    private fun showCreateReservationDialog() {
        CreateReservationDialog().show(parentFragmentManager, "CreateReservationDialog")
    }

    override fun showSessionExpired() {
        if (!isAdded || !::prefsManager.isInitialized) return
        val errorMessage = getString(com.example.salontenexapp.R.string.error_session_expired)
        android.widget.Toast.makeText(requireContext(), errorMessage, android.widget.Toast.LENGTH_LONG).show()
        prefsManager.logout()

        val intent = android.content.Intent(requireActivity(), com.example.salontenexapp.Vista.LoginActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}