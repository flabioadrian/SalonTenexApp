package com.example.salontenexapp.Vista.dialogs

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.BundleCompat
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.salontenexapp.data.Salon
import com.example.salontenexapp.databinding.DialogEditSalonBinding
import com.example.salontenexapp.util.ImageUtils

class EditSalonDialog : DialogFragment() {

    private var onSalonUpdatedListener: ((Salon) -> Unit)? = null
    private var _binding: DialogEditSalonBinding? = null
    private val binding get() = _binding!!
    private lateinit var currentSalon: Salon

    private var selectedImageUri: Uri? = null
    private var uploadedImageUrl: String? = null

    // Contract para selección de imágenes
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                binding.ivSalonImage.setImageURI(uri)
                binding.tvImageStatus.text = "Imagen seleccionada"
                uploadImageToServer(uri)
            }
        }
    }

    fun setOnSalonUpdatedListener(listener: (Salon) -> Unit) {
        this.onSalonUpdatedListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentSalon = arguments?.let {
            BundleCompat.getParcelable(it, ARG_SALON, Salon::class.java)
        } ?: throw IllegalStateException("Salon required")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogEditSalonBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadCurrentData()
        setupClickListeners()
    }

    private fun loadCurrentData() {
        binding.etSalonName.setText(currentSalon.name)
        binding.etCapacity.setText(currentSalon.capacity.toString())
        binding.etPrice.setText(currentSalon.price.toString())
        binding.etDescription.setText(currentSalon.description)

        // Cargar imagen actual si existe
        if (!currentSalon.imageUrl.isNullOrEmpty() && currentSalon.imageUrl != "default.jpg") {
            // Aquí usarías Glide o Picasso para cargar la imagen desde la URL
            binding.tvImageStatus.text = "Imagen actual cargada"
            uploadedImageUrl = currentSalon.imageUrl
        }
    }

    private fun setupClickListeners() {
        binding.btnSelectImage.setOnClickListener {
            openImagePicker()
        }

        binding.btnUpdate.setOnClickListener {
            if (validateFields()) {
                updateSalon()
            }
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun uploadImageToServer(uri: Uri) {
        binding.progressImage.visibility = View.VISIBLE
        binding.tvImageStatus.text = "Subiendo imagen..."

        // Simular subida de imagen (reemplaza con tu lógica real)
        simulateImageUpload()
    }

    private fun simulateImageUpload() {
        // Simular proceso de subida
        Thread {
            Thread.sleep(2000) // Simular tiempo de subida

            requireActivity().runOnUiThread {
                binding.progressImage.visibility = View.GONE
                binding.tvImageStatus.text = "Imagen subida exitosamente"
                uploadedImageUrl = "img/APIMobil/salon_${System.currentTimeMillis()}.jpg"
            }
        }.start()
    }

    private fun validateFields(): Boolean {
        val name = binding.etSalonName.text.toString()
        val capacity = binding.etCapacity.text.toString()
        val price = binding.etPrice.text.toString()

        if (name.isEmpty()) {
            binding.etSalonName.error = "El nombre es requerido"
            return false
        }

        if (capacity.isEmpty()) {
            binding.etCapacity.error = "La capacidad es requerida"
            return false
        }

        if (price.isEmpty()) {
            binding.etPrice.error = "El precio es requerido"
            return false
        }

        return true
    }

    private fun updateSalon() {
        val name = binding.etSalonName.text.toString()
        val capacity = binding.etCapacity.text.toString().toIntOrNull() ?: 0
        val price = binding.etPrice.text.toString().toDoubleOrNull() ?: 0.0
        val description = binding.etDescription.text.toString()

        val updatedSalon = currentSalon.copy(
            name = name,
            capacity = capacity,
            price = price,
            description = description,
            imageUrl = uploadedImageUrl ?: currentSalon.imageUrl
        )

        onSalonUpdatedListener?.invoke(updatedSalon)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_SALON = "salon_to_edit"

        fun newInstance(salon: Salon): EditSalonDialog {
            val fragment = EditSalonDialog()
            val args = Bundle().apply {
                putParcelable(ARG_SALON, salon)
            }
            fragment.arguments = args
            return fragment
        }
    }
}