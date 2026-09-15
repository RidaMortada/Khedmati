package com.example.khedmati.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.khedmati.MainActivity
import com.example.khedmati.R
import com.example.khedmati.data.DummyCloudRepository
import com.example.khedmati.databinding.DialogAddServiceBinding
import com.example.khedmati.databinding.FragmentManageProfileBinding
import com.example.khedmati.model.LocationMode
import com.example.khedmati.model.Professional
import com.example.khedmati.ui.common.ServiceAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ManageProfileFragment : Fragment() {

    private var _binding: FragmentManageProfileBinding? = null
    private val binding get() = _binding!!
    private val host get() = requireActivity() as MainActivity
    private lateinit var professional: Professional
    private lateinit var serviceAdapter: ServiceAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentManageProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = DummyCloudRepository.currentUser ?: run {
            host.showSignInDialog { parentFragmentManager.popBackStack() }
            return
        }
        professional = DummyCloudRepository.professionalById(user.professionalId) ?: return
        setupForm()
        setupServices()
        binding.saveProfileButton.setOnClickListener { saveProfile() }
        binding.addServiceButton.setOnClickListener { showAddServiceDialog() }
    }

    private fun setupForm() {
        binding.nameInput.setText(professional.publicName.resolve(host.language()))
        binding.descriptionInput.setText(professional.description.resolve(host.language()))
        binding.phoneInput.setText(professional.phone)
        binding.radiusInput.setText(professional.serviceRadiusKm.toString())
        binding.privacySpinner.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item,
            listOf(getString(R.string.location_exact), getString(R.string.location_approximate), getString(R.string.location_city_only))
        )
        binding.privacySpinner.setSelection(
            when (professional.locationMode) {
                LocationMode.EXACT -> 0
                LocationMode.APPROXIMATE -> 1
                LocationMode.CITY_ONLY -> 2
            }
        )
    }

    private fun setupServices() {
        serviceAdapter = ServiceAdapter { host.language() }
        binding.servicesList.layoutManager = LinearLayoutManager(requireContext())
        binding.servicesList.adapter = serviceAdapter
        serviceAdapter.submitList(professional.services)
    }

    private fun saveProfile() {
        val mode = when (binding.privacySpinner.selectedItemPosition) {
            0 -> LocationMode.EXACT
            2 -> LocationMode.CITY_ONLY
            else -> LocationMode.APPROXIMATE
        }
        DummyCloudRepository.updateProfessional(
            id = professional.id,
            name = binding.nameInput.text?.toString()?.trim().orEmpty().ifBlank { professional.publicName.resolve(host.language()) },
            description = binding.descriptionInput.text?.toString()?.trim().orEmpty(),
            phone = binding.phoneInput.text?.toString()?.trim().orEmpty(),
            radiusKm = binding.radiusInput.text?.toString()?.toIntOrNull()?.coerceIn(1, 200) ?: professional.serviceRadiusKm,
            locationMode = mode,
            profileImageUrl = null
        )
        Toast.makeText(requireContext(), R.string.saved_in_dummy_repository, Toast.LENGTH_SHORT).show()
    }

    private fun showAddServiceDialog() {
        val dialogBinding = DialogAddServiceBinding.inflate(layoutInflater)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_service)
            .setView(dialogBinding.root)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.add) { _, _ ->
                val title = dialogBinding.serviceTitleInput.text?.toString()?.trim().orEmpty()
                if (title.isNotBlank()) {
                    DummyCloudRepository.addService(
                        professional.id,
                        title,
                        dialogBinding.servicePriceInput.text?.toString()?.toDoubleOrNull(),
                        dialogBinding.serviceCurrencyInput.text?.toString()?.trim()?.ifBlank { null }
                    )
                    serviceAdapter.submitList(professional.services)
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
