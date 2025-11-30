package com.example.salontenexapp.Vista
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.salontenexapp.data.Salon
import com.example.salontenexapp.Vista.adapter.SalonsAdapter
import com.example.salontenexapp.Vista.dialogs.AddSalonDialog
import com.example.salontenexapp.Vista.dialogs.EditSalonDialog
import com.example.salontenexapp.data.api.RetrofitClient
import com.example.salontenexapp.databinding.FragmentManageSalonsBinding // Asegúrate de que el XML exista
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManageSalonsFragment : Fragment() {

    private var _binding: FragmentManageSalonsBinding? = null
    private val binding get() = _binding!!
    private lateinit var salonsAdapter: SalonsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageSalonsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        loadSalons()
        updateStatistics()
    }

    private fun setupRecyclerView() {
        salonsAdapter = SalonsAdapter(
            onEditClick = { salon ->
                showEditSalonDialog(salon)
            }
        )

        binding.rvSalons.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = salonsAdapter
        }
    }

    private fun setupClickListeners() {
        binding.fabAddSalon.setOnClickListener {
            showAddSalonDialog()
        }
    }

    private fun loadSalons() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = RetrofitClient.apiService
                val salons = apiService.getSalons()

                withContext(Dispatchers.Main) {
                    salonsAdapter.submitList(salons)
                    updateStatistics()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    salonsAdapter.submitList(emptyList())
                }
            }
        }
    }

    private fun updateStatistics() {
        val totalSalons = 8
        val availableSalons = 6

        binding.tvTotalSalons.text = totalSalons.toString()
        binding.tvAvailableSalons.text = availableSalons.toString()
    }

    private fun showAddSalonDialog() {
        val dialog = AddSalonDialog()
        dialog.setOnSalonAddedListener { newSalon ->
            val currentList = salonsAdapter.currentList.toMutableList()
            currentList.add(0, newSalon)
            salonsAdapter.submitList(currentList)
            updateStatistics()
        }
        dialog.show(parentFragmentManager, "AddSalonDialog")
    }

    private fun showEditSalonDialog(salon: Salon) {
        val dialog = EditSalonDialog.newInstance(salon)
        dialog.setOnSalonUpdatedListener { updatedSalon ->
            val currentList = salonsAdapter.currentList.toMutableList()
            val index = currentList.indexOfFirst { it.id == updatedSalon.id }
            if (index != -1) {
                currentList[index] = updatedSalon
                salonsAdapter.submitList(currentList)
            }
        }
        dialog.show(parentFragmentManager, "EditSalonDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}