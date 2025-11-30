package com.example.salontenexapp.Vista

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.salontenexapp.databinding.FragmentChangePasswordBinding
import com.example.salontenexapp.Presentador.ChangePasswordPresenter
import com.example.salontenexapp.Contrato.ChangePasswordContract
import com.example.salontenexapp.data.api.RetrofitClient
import com.example.salontenexapp.util.SharedPreferencesManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class ChangePasswordFragment : Fragment(), ChangePasswordContract.View {

    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!

    private lateinit var presenter: ChangePasswordPresenter
    private lateinit var prefsManager: SharedPreferencesManager
    private val userId: Int by lazy {
        prefsManager.getUserId()
    }

    private val userType: String by lazy {
        prefsManager.getUserType() ?: "client"
    }

    private val userEmail: String? by lazy {
        prefsManager.getUserEmail()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsManager = SharedPreferencesManager(requireContext())

        if (!prefsManager.isLoggedIn() || userId == -1) {
            showError("No hay una sesión activa. Por favor, inicie sesión nuevamente.")
            return
        }

        val apiService = RetrofitClient.apiService
        presenter = ChangePasswordPresenter(this, apiService)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnChangePassword.setOnClickListener {
            changePassword()
        }

        binding.btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun changePassword() {
        val currentPassword = binding.etCurrentPassword.text.toString()
        val newPassword = binding.etNewPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        // Validar que tenemos los datos necesarios
        if (userId == -1) {
            showError("Error: No se pudo identificar al usuario")
            return
        }

        if (userType.isBlank()) {
            showError("Error: Tipo de usuario no definido")
            return
        }

        // Ejecutar en un scope de corrutina
        CoroutineScope(Dispatchers.Main).launch {
            presenter.changePassword(
                userId,
                userType,
                currentPassword,
                newPassword,
                confirmPassword
            )
        }
    }

    // Implementación de la interfaz View
    override fun showLoading() {
        binding.btnChangePassword.isEnabled = false
        binding.btnChangePassword.text = "Cambiando..."
    }

    override fun hideLoading() {
        binding.btnChangePassword.isEnabled = true
        binding.btnChangePassword.text = "Cambiar Contraseña"
    }

    override fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onPasswordChangeSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()

        // Limpiar campos
        binding.etCurrentPassword.text?.clear()
        binding.etNewPassword.text?.clear()
        binding.etConfirmPassword.text?.clear()

        // Opcional: regresar al fragment anterior después de un éxito
        CoroutineScope(Dispatchers.Main).launch {
            delay(1500)
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}