// ProfileFragment.kt
package com.example.salontenexapp.Vista

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.salontenexapp.Contrato.ProfileContract
import com.example.salontenexapp.Modelo.Client
import com.example.salontenexapp.Presentador.ProfilePresenter
import com.example.salontenexapp.data.api.ClientService
import com.example.salontenexapp.databinding.FragmentProfileBinding
import com.example.salontenexapp.util.SharedPreferencesManager
import com.example.salontenexapp.data.api.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileFragment : Fragment(), ProfileContract.ProfileView {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var presenter: ProfilePresenter
    private lateinit var prefs: SharedPreferencesManager
    private lateinit var clientService: ClientService

    private var currentClient: Client? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = SharedPreferencesManager(requireContext())
        clientService = RetrofitClient.retrofit.create(ClientService::class.java)
        presenter = ProfilePresenter(this, clientService, prefs)

        setupUI()
        loadProfileData()
    }

    private fun setupUI() {
        binding.btnSaveProfile.setOnClickListener {
            saveProfileChanges()
        }

        binding.btnChangePassword.setOnClickListener {
            navigateToChangePassword()
        }
    }

    private fun navigateToChangePassword() {
        val mainActivity = activity

        if (mainActivity is MainActivity) {
            mainActivity.replaceFragment(ChangePasswordFragment(), addToBackStack = true)

            if (mainActivity.isDrawerOpen()) {
                mainActivity.closeDrawer()
            }
        } else {
            showError("Error de navegación: Activity contenedora incorrecta.")
        }
    }

    private fun loadProfileData() {
        CoroutineScope(Dispatchers.Main).launch {
            presenter.loadClientProfile()
        }
    }

    private fun saveProfileChanges() {
        val nombre = binding.etFirstName.text.toString().trim()
        val apellidoPaterno = binding.etLastName.text.toString().trim()
        val apellidoMaterno = binding.etMiddleName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val telefono = binding.etPhone.text.toString().trim()
        val direccion = binding.etDirection.text.toString().trim()

        if (validateInput(nombre, apellidoPaterno, apellidoMaterno, email, telefono)) {
            currentClient?.let { client ->
                val updatedClient = Client(
                    id = client.id,
                    nombre = nombre,
                    apellidoPaterno = apellidoPaterno,
                    apellidoMaterno = apellidoMaterno,
                    email = email,
                    telefono = telefono,
                    direccion = direccion,
                )

                CoroutineScope(Dispatchers.Main).launch {
                    presenter.updateClientProfile(updatedClient)
                }
            } ?: run {
                showError("No se pudo cargar la información del cliente")
            }
        }
    }

    private fun validateInput(
        nombre: String,
        apellidoPaterno: String,
        apellidoMaterno: String,
        email: String,
        telefono: String
    ): Boolean {
        if (nombre.isEmpty() || apellidoPaterno.isEmpty() || email.isEmpty()) {
            showError("Nombre, apellido paterno y email son obligatorios")
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Por favor ingresa un email válido")
            return false
        }

        if (telefono.isNotEmpty() && telefono.length != 10) {
            showError("El teléfono debe tener 10 dígitos")
            return false
        }

        return true
    }

    override fun showClientProfile(client: Client) {
        currentClient = client

        binding.etFirstName.setText(client.nombre)
        binding.etLastName.setText(client.apellidoPaterno)
        binding.etMiddleName.setText(client.apellidoMaterno)
        binding.etEmail.setText(client.email)
        binding.etPhone.setText(client.telefono)
        binding.etDirection.setText(client.direccion ?: "")
    }

    override fun onProfileUpdated(client: Client) {
        currentClient = client
        prefs.saveUserData(
            isLoggedIn = true,
            userType = "client",
            userId = client.id,
            email = client.email,
            name = "${client.nombre} ${client.apellidoPaterno}"
        )
    }

    override fun showLoading() {
        binding.btnSaveProfile.isEnabled = false
        binding.btnSaveProfile.text = "Guardando..."
    }

    override fun hideLoading() {
        binding.btnSaveProfile.isEnabled = true
        binding.btnSaveProfile.text = "Guardar Cambios"
    }

    override fun showError(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_LONG).show()
    }

    override fun showSuccess(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}