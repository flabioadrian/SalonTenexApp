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
import androidx.fragment.app.DialogFragment
import com.example.salontenexapp.data.Salon
import com.example.salontenexapp.databinding.DialogAddSalonBinding
import com.example.salontenexapp.util.ImageUtils

class AddSalonDialog : DialogFragment() {

    private var onSalonAddedListener: ((Salon) -> Unit)? = null
    private var _binding: DialogAddSalonBinding? = null
    private val binding get() = _binding!!

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

    fun setOnSalonAddedListener(listener: (Salon) -> Unit) {
        this.onSalonAddedListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddSalonBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnSelectImage.setOnClickListener {
            openImagePicker()
        }

        binding.btnSave.setOnClickListener {
            if (validateFields()) {
                saveSalon()
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

    private fun saveSalon() {
        val name = binding.etSalonName.text.toString()
        val capacity = binding.etCapacity.text.toString().toIntOrNull() ?: 0
        val price = binding.etPrice.text.toString().toDoubleOrNull() ?: 0.0
        val description = binding.etDescription.text.toString()

        val newSalon = Salon(
            id = 0,
            name = name,
            capacity = capacity,
            description = description,
            price = price,
            imageUrl = uploadedImageUrl ?: "default.jpg"
        )

        onSalonAddedListener?.invoke(newSalon)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}