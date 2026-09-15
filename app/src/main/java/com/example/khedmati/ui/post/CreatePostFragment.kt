package com.example.khedmati.ui.post

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.khedmati.MainActivity
import com.example.khedmati.R
import com.example.khedmati.data.DummyCloudRepository
import com.example.khedmati.databinding.FragmentCreatePostBinding

class CreatePostFragment : Fragment() {

    private var _binding: FragmentCreatePostBinding? = null
    private val binding get() = _binding!!
    private val host get() = requireActivity() as MainActivity
    private var selectedImageUri: Uri? = null

    private val imagePicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                requireContext().contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            selectedImageUri = uri
            binding.postImagePreview.isVisible = true
            binding.postImagePreview.setImageURI(uri)
            Toast.makeText(requireContext(), R.string.work_image_selected, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreatePostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (DummyCloudRepository.currentUser == null) {
            host.showSignInDialog { }
        }
        binding.chooseImageButton.setOnClickListener { imagePicker.launch(arrayOf("image/*")) }
        binding.publishButton.setOnClickListener { publish() }
    }

    private fun publish() {
        val user = DummyCloudRepository.currentUser ?: run {
            host.showSignInDialog { publish() }
            return
        }
        val text = binding.postTextInput.text?.toString()?.trim().orEmpty()
        if (text.isBlank()) {
            binding.postTextInput.error = getString(R.string.required_field)
            return
        }
        DummyCloudRepository.createPost(user.professionalId, text, selectedImageUri?.toString())
        Toast.makeText(requireContext(), R.string.post_published_dummy, Toast.LENGTH_SHORT).show()
        host.showHome()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
