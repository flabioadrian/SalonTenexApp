package com.example.salontenexapp.Vista

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.salontenexapp.Adaptadores.ServiciosAdapter
import com.example.salontenexapp.Contrato.ServiciosContract
import com.example.salontenexapp.Modelo.Servicio
import com.example.salontenexapp.Modelo.ServicioRequest
import com.example.salontenexapp.Presentador.ServiciosPresenter
import com.example.salontenexapp.R
import com.example.salontenexapp.databinding.FragmentManageServicesBinding
import com.example.salontenexapp.data.api.RetrofitClient

class ManageServicesFragment : Fragment(), ServiciosContract.View {

    private var _binding: FragmentManageServicesBinding? = null
    private val binding get() = _binding!!

    private lateinit var presenter: ServiciosContract.Presenter
    private lateinit var serviciosAdapter: ServiciosAdapter
    private var currentDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageServicesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val apiService = RetrofitClient.apiService
        presenter = ServiciosPresenter(this, apiService)

        setupRecyclerView()
        setupFab()
        presenter.cargarServicios()
    }

    private fun setupRecyclerView() {
        serviciosAdapter = ServiciosAdapter { servicio ->
            mostrarDialogoEditarServicio(servicio)
        }

        binding.recyclerViewServices.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = serviciosAdapter
        }
    }

    private fun setupFab() {
        binding.fabAddService.setOnClickListener {
            mostrarDialogoCrearServicio()
        }
    }

    private fun mostrarDialogoCrearServicio() {
        mostrarDialogoServicio(null)
    }

    private fun mostrarDialogoEditarServicio(servicio: Servicio) {
        mostrarDialogoServicio(servicio)
    }

    private fun mostrarDialogoServicio(servicio: Servicio?) {
        val isEditMode = servicio != null
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_service, null)

        val editServiceName = dialogView.findViewById<EditText>(R.id.editServiceName)
        val editServiceCost = dialogView.findViewById<EditText>(R.id.editServiceCost)
        val editServiceDescription = dialogView.findViewById<EditText>(R.id.editServiceDescription)
        val textServiceStatus = dialogView.findViewById<TextView>(R.id.textServiceStatus)
        val buttonSave = dialogView.findViewById<Button>(R.id.buttonSave)
        val buttonDisable = dialogView.findViewById<Button>(R.id.buttonDisable)
        val buttonCancel = dialogView.findViewById<Button>(R.id.buttonCancel)
        val statusContainer = dialogView.findViewById<LinearLayout>(R.id.statusContainer)

        // Configurar el diálogo según el modo
        if (isEditMode) {
            // Modo edición
            val dialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)
            dialogTitle?.text = "Editar Servicio"

            buttonSave.text = "Guardar Cambios"
            statusContainer.visibility = View.VISIBLE

            // Llenar datos actuales
            editServiceName.setText(servicio!!.nombreServicio)
            editServiceCost.setText(servicio.costo.toString())
            editServiceDescription.setText(servicio.descripcion)
            textServiceStatus.text = servicio.estado

            // Configurar texto y color del botón Deshabilitar según el estado
            if (servicio.estado == "Activo") {
                buttonDisable.text = "Deshabilitar Servicio"
                buttonDisable.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.orange)
                textServiceStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
            } else {
                buttonDisable.text = "Habilitar Servicio"
                buttonDisable.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.green)
                textServiceStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            }
        } else {
            // Modo creación
            val dialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)
            dialogTitle?.text = "Crear Nuevo Servicio"

            buttonSave.text = "Crear Servicio"
            statusContainer.visibility = View.GONE
            buttonDisable.visibility = View.GONE
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        currentDialog = dialog

        buttonSave.setOnClickListener {
            val nombre = editServiceName.text.toString().trim()
            val costoText = editServiceCost.text.toString().trim()
            val descripcion = editServiceDescription.text.toString().trim()

            // Validaciones
            if (nombre.isEmpty()) {
                editServiceName.error = "El nombre es requerido"
                return@setOnClickListener
            }

            if (costoText.isEmpty()) {
                editServiceCost.error = "El costo es requerido"
                return@setOnClickListener
            }

            val costo = costoText.toDoubleOrNull()
            if (costo == null || costo <= 0) {
                editServiceCost.error = "El costo debe ser un número válido mayor a 0"
                return@setOnClickListener
            }

            if (descripcion.isEmpty()) {
                editServiceDescription.error = "La descripción es requerida"
                return@setOnClickListener
            }

            if (isEditMode) {
                val servicioActualizado = servicio!!.copy(
                    nombreServicio = nombre,
                    costo = costo,
                    descripcion = descripcion
                )
                presenter.actualizarServicio(servicioActualizado)
            } else {
                val nuevoServicio = ServicioRequest(
                    nombreServicio = nombre,
                    costo = costo,
                    descripcion = descripcion
                )
                presenter.crearServicio(nuevoServicio)
            }
            dialog.dismiss()
        }

        buttonDisable.setOnClickListener {
            if (isEditMode) {
                val nuevoEstado = if (servicio!!.estado == "Activo") "Desactivado" else "Activo"
                val accion = if (servicio.estado == "Activo") "deshabilitar" else "habilitar"
                val mensaje = if (servicio.estado == "Activo")
                    "¿Está seguro de que desea deshabilitar este servicio?\nLos clientes no podrán verlo."
                else
                    "¿Está seguro de que desea habilitar este servicio?\nLos clientes podrán verlo y reservarlo."

                AlertDialog.Builder(requireContext())
                    .setTitle("Confirmar $accion")
                    .setMessage(mensaje)
                    .setPositiveButton("Sí") { _, _ ->
                        presenter.cambiarEstadoServicio(servicio.idServicio, nuevoEstado)
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }

        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun mostrarServicios(servicios: List<Servicio>) {
        serviciosAdapter.submitList(servicios)

        if (servicios.isEmpty()) {
            binding.textEmpty.visibility = View.VISIBLE
            binding.recyclerViewServices.visibility = View.GONE
        } else {
            binding.textEmpty.visibility = View.GONE
            binding.recyclerViewServices.visibility = View.VISIBLE
        }
    }

    override fun mostrarError(mensaje: String) {
        Toast.makeText(requireContext(), "Error: $mensaje", Toast.LENGTH_LONG).show()
    }

    override fun mostrarCargando() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerViewServices.visibility = View.GONE
    }

    override fun ocultarCargando() {
        binding.progressBar.visibility = View.GONE
        binding.recyclerViewServices.visibility = View.VISIBLE
    }

    override fun mostrarMensajeExito(mensaje: String) {
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentDialog?.dismiss()
        _binding = null
    }
}