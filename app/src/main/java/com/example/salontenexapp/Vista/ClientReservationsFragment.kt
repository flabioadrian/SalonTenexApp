package com.example.salontenexapp.Vista
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.salontenexapp.Modelo.ReservationClient
import com.example.salontenexapp.Presentador.ReservationAdapter
import com.example.salontenexapp.data.api.ClientService
import com.example.salontenexapp.data.api.RetrofitClient
import com.example.salontenexapp.databinding.FragmentClientReservationsBinding
import com.example.salontenexapp.util.SharedPreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response

class ClientReservationsFragment : Fragment() {

    private var _binding: FragmentClientReservationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefs: SharedPreferencesManager
    private lateinit var clientService: ClientService
    private lateinit var adapter: ReservationAdapter

    private var reservationsList = mutableListOf<ReservationClient>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClientReservationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = SharedPreferencesManager(requireContext())
        clientService = RetrofitClient.retrofit.create(ClientService::class.java)

        setupRecyclerView()
        loadReservations()
    }

    private fun setupRecyclerView() {
        adapter = ReservationAdapter(
            reservationsList,
            onCancelClick = { reserva ->
                showCancelConfirmationDialog(reserva)
            },
            onEditClick = { reserva ->
                navigateToEditReservation(reserva)
            }
        )
        binding.rvReservations.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReservations.adapter = adapter
    }

    private fun loadReservations() {
        // Validar que sea cliente
        if (prefs.getUserType() != "client") {
            showError("Acceso denegado: Solo para clientes")
            return
        }

        val clientId = prefs.getUserId()
        if (clientId == -1) {
            showError("No se pudo obtener la información del usuario")
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                showLoading()
                val response: Response<List<ReservationClient>> = clientService.getClientReservations(clientId)

                if (response.isSuccessful) {
                    val reservations = response.body() ?: emptyList()
                    reservationsList.clear()
                    reservationsList.addAll(reservations)
                    adapter.notifyDataSetChanged()

                    // Mostrar empty state si no hay reservas
                    if (reservations.isEmpty()) {
                        binding.emptyState.visibility = View.VISIBLE
                        binding.rvReservations.visibility = View.GONE
                    } else {
                        binding.emptyState.visibility = View.GONE
                        binding.rvReservations.visibility = View.VISIBLE
                    }

                    showSuccess("Reservas cargadas correctamente")
                } else {
                    showError("Error al cargar reservas: ${response.code()}")
                }
            } catch (e: Exception) {
                showError("Error de conexión: ${e.message}")
            } finally {
                hideLoading()
            }
        }
    }

    private fun showCancelConfirmationDialog(reserva: ReservationClient) {
        AlertDialog.Builder(requireContext())
            .setTitle("Cancelar Reserva")
            .setMessage("¿Estás seguro de que deseas cancelar esta reserva?\n\nEsta acción no se puede deshacer.")
            .setPositiveButton("Sí, Cancelar") { _, _ ->
                cancelReservation(reserva)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun cancelReservation(reserva: ReservationClient) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                showLoading()
                val request = com.example.salontenexapp.Modelo.CancelReservationRequest(reserva.id)
                val response: Response<ReservationClient> = clientService.cancelReservation(request)

                if (response.isSuccessful) {
                    val updatedReserva = response.body()
                    updatedReserva?.let {
                        val index = reservationsList.indexOfFirst { it.id == reserva.id }
                        if (index != -1) {
                            reservationsList[index] = it
                            adapter.notifyItemChanged(index)
                        }
                    }
                    showSuccess("Reserva cancelada correctamente")
                } else {
                    showError("Error al cancelar la reserva: ${response.code()}")
                }
            } catch (e: Exception) {
                showError("Error de conexión: ${e.message}")
            } finally {
                hideLoading()
            }
        }
    }

    private fun navigateToEditReservation(reserva: ReservationClient) {
        val editFragment = CreateReservationFragment.newInstance(reserva)
        val mainActivity = activity as? MainActivity
        mainActivity?.replaceFragment(editFragment, addToBackStack = true)
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showError(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_LONG).show()
    }

    private fun showSuccess(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}