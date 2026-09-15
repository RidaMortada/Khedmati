package com.example.khedmati.ui.saved

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.khedmati.MainActivity
import com.example.khedmati.R
import com.example.khedmati.data.DummyCloudRepository
import com.example.khedmati.databinding.FragmentSavedBinding
import com.example.khedmati.ui.common.ProfessionalAdapter

class SavedFragment : Fragment() {

    private var _binding: FragmentSavedBinding? = null
    private val binding get() = _binding!!
    private val host get() = requireActivity() as MainActivity
    private lateinit var adapter: ProfessionalAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSavedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = ProfessionalAdapter({ host.language() }) { host.openProfessional(it.id) }
        binding.savedList.layoutManager = LinearLayoutManager(requireContext())
        binding.savedList.adapter = adapter
        binding.signInButton.setOnClickListener { host.showSignInDialog { renderState() } }
        renderState()
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) renderState()
    }

    private fun renderState() {
        val signedIn = DummyCloudRepository.currentUser != null
        val saved = if (signedIn) DummyCloudRepository.savedProfessionals() else emptyList()
        binding.signInButton.isVisible = !signedIn
        binding.savedMessage.isVisible = !signedIn || saved.isEmpty()
        binding.savedMessage.text = when {
            !signedIn -> getString(R.string.sign_in_to_save)
            saved.isEmpty() -> getString(R.string.no_saved_professionals)
            else -> ""
        }
        binding.savedList.isVisible = saved.isNotEmpty()
        adapter.submitList(saved)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
