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
import androidx.core.os.BundleCompat
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.salontenexapp.data.Salon
import com.example.salontenexapp.databinding.DialogEditSalonBinding
import com.example.salontenexapp.data.api.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream

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
            Glide.with(this)
                .load(currentSalon.getFullImageUrl())
                .into(binding.ivSalonImage)
            binding.tvImageStatus.text = "Imagen actual cargada"
            uploadedImageUrl = currentSalon.imageUrl
        } else {
            binding.tvImageStatus.text = "Sin imagen"
        }
    }

    private fun setupClickListeners() {
        binding.btnSelectImage.setOnClickListener {
            openImagePicker()
        }

        binding.btnUpdate.setOnClickListener {
            if (validateFields()) {
                updateSalonOnServer()
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

    private fun updateSalonOnServer() {
        val name = binding.etSalonName.text.toString()
        val capacity = binding.etCapacity.text.toString().toIntOrNull() ?: 0
        val price = binding.etPrice.text.toString().toDoubleOrNull() ?: 0.0
        val description = binding.etDescription.text.toString()

        // Mostrar progreso
        binding.progressImage.visibility = View.VISIBLE
        binding.tvImageStatus.text = "Actualizando salón..."

        val updatedSalon = currentSalon.copy(
            name = name,
            capacity = capacity,
            price = price,
            description = description,
            imageUrl = uploadedImageUrl ?: currentSalon.imageUrl
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = RetrofitClient.apiService
                val response = apiService.updateSalon(updatedSalon)

                withContext(Dispatchers.Main) {
                    binding.progressImage.visibility = View.GONE

                    if (response.isSuccessful) {
                        val salonResponse = response.body()
                        if (salonResponse?.success == true) {
                            onSalonUpdatedListener?.invoke(updatedSalon)
                            Toast.makeText(requireContext(), "Salón actualizado exitosamente", Toast.LENGTH_SHORT).show()
                            dismiss()
                        } else {
                            // Mostrar el mensaje de error del servidor
                            val errorMsg = salonResponse?.message ?: "Error desconocido"
                            Toast.makeText(requireContext(), "Error: $errorMsg", Toast.LENGTH_SHORT).show()
                            binding.tvImageStatus.text = "Error: $errorMsg"
                        }
                    } else {
                        val errorMsg = when (response.code()) {
                            500 -> "Error interno del servidor"
                            400 -> "Solicitud incorrecta"
                            401 -> "No autorizado"
                            else -> "Error ${response.code()}"
                        }
                        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
                        binding.tvImageStatus.text = errorMsg
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressImage.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
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