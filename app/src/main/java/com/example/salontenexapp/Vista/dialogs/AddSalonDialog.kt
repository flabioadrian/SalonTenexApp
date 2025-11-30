package com.example.salontenexapp.Vista.dialogs

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.example.salontenexapp.data.Salon
import com.example.salontenexapp.databinding.DialogAddSalonBinding
import com.example.salontenexapp.data.api.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream

class AddSalonDialog : DialogFragment() {

    private var onSalonAddedListener: ((Salon) -> Unit)? = null
    private var _binding: DialogAddSalonBinding? = null
    private val binding get() = _binding!!

    private var selectedImageUri: Uri? = null
    private var uploadedImageUrl: String? = null

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
                createSalonOnServer()
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

        try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            val imageBytes = inputStream?.readBytes() ?: ByteArray(0)
            val mediaType = (requireContext().contentResolver.getType(uri) ?: "image/*").toMediaTypeOrNull()
            val requestFile = imageBytes.toRequestBody(mediaType)

            val imagePart = MultipartBody.Part.createFormData("image", "image.jpg", requestFile)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val apiService = RetrofitClient.apiService
                    val response = apiService.uploadImage(imagePart)

                    if (response.isSuccessful) {
                        val uploadResponse = response.body()
                        if (uploadResponse?.success == true) {
                            uploadedImageUrl = uploadResponse.imageUrl
                            requireActivity().runOnUiThread {
                                binding.progressImage.visibility = View.GONE
                                binding.tvImageStatus.text = "Imagen subida exitosamente"
                            }
                        } else {
                            requireActivity().runOnUiThread {
                                binding.progressImage.visibility = View.GONE
                                binding.tvImageStatus.text = "Error: ${uploadResponse?.error}"
                            }
                        }
                    } else {
                        requireActivity().runOnUiThread {
                            binding.progressImage.visibility = View.GONE
                            binding.tvImageStatus.text = "Error en la subida"
                        }
                    }
                } catch (e: Exception) {
                    requireActivity().runOnUiThread {
                        binding.progressImage.visibility = View.GONE
                        binding.tvImageStatus.text = "Error: ${e.message}"
                    }
                }
            }

        } catch (e: Exception) {
            binding.progressImage.visibility = View.GONE
            binding.tvImageStatus.text = "Error al procesar imagen: ${e.message}"
        }
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

    private fun createSalonOnServer() {
        val name = binding.etSalonName.text.toString()
        val capacity = binding.etCapacity.text.toString().toIntOrNull() ?: 0
        val price = binding.etPrice.text.toString().toDoubleOrNull() ?: 0.0
        val description = binding.etDescription.text.toString()

        binding.progressImage.visibility = View.VISIBLE
        binding.tvImageStatus.text = "Creando salón..."

        val newSalon = Salon(
            id = 0,
            name = name,
            capacity = capacity,
            description = description,
            price = price,
            imageUrl = uploadedImageUrl ?: "default.jpg"
        )

        // Llamar a la API para crear en el servidor
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = RetrofitClient.apiService
                val response = apiService.createSalon(newSalon)

                withContext(Dispatchers.Main) {
                    binding.progressImage.visibility = View.GONE

                    if (response.isSuccessful) {
                        val salonResponse = response.body()
                        if (salonResponse?.success == true) {
                            val createdSalon = newSalon.copy(id = salonResponse.id ?: 0)
                            onSalonAddedListener?.invoke(createdSalon)
                            Toast.makeText(requireContext(), "Salón creado exitosamente", Toast.LENGTH_SHORT).show()
                            dismiss()
                        } else {
                            Toast.makeText(requireContext(), "Error: ${salonResponse?.message}", Toast.LENGTH_SHORT).show()
                            binding.tvImageStatus.text = "Error en creación"
                        }
                    } else {
                        Toast.makeText(requireContext(), "Error en la conexión: ${response.code()}", Toast.LENGTH_SHORT).show()
                        binding.tvImageStatus.text = "Error de conexión"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressImage.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    binding.tvImageStatus.text = "Error: ${e.message}"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): AddSalonDialog {
            return AddSalonDialog()
        }
    }
}