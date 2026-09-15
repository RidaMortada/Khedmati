package com.example.khedmati.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.khedmati.MainActivity
import com.example.khedmati.R
import com.example.khedmati.data.DummyCloudRepository
import com.example.khedmati.databinding.FragmentSearchBinding
import com.example.khedmati.ui.common.ProfessionalAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val host get() = requireActivity() as MainActivity
    private lateinit var professionalAdapter: ProfessionalAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSpinners()
        setupResults()
        setupActions()
        applyPreselectedCategory()
        runSearch()
    }

    private fun setupSpinners() {
        val categoryLabels = mutableListOf(getString(R.string.all_categories))
        categoryLabels += DummyCloudRepository.categories.map { it.name.resolve(host.language()) }
        binding.categorySpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categoryLabels)

        binding.locationSpinner.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item,
            listOf(getString(R.string.all_locations), "Beirut", "Bekaa", "Keserwan-Jbeil", "North")
        )
        binding.ratingSpinner.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item,
            listOf(getString(R.string.any_rating), "3+", "4+", "4.5+")
        )
    }

    private fun setupResults() {
        professionalAdapter = ProfessionalAdapter(
            languageProvider = { host.language() },
            onClick = { host.openProfessional(it.id) }
        )
        binding.resultsList.layoutManager = LinearLayoutManager(requireContext())
        binding.resultsList.adapter = professionalAdapter
    }

    private fun setupActions() {
        binding.searchButton.setOnClickListener { runSearch() }
        binding.nearMeButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.current_location_dummy_title)
                .setMessage(R.string.current_location_dummy_message)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    binding.locationSpinner.setSelection(1)
                    runSearch()
                }
                .show()
        }
        binding.mapButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.map_dummy_title)
                .setMessage(R.string.map_dummy_message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
    }

    private fun applyPreselectedCategory() {
        val categoryId = arguments?.getString(ARG_CATEGORY_ID) ?: return
        val index = DummyCloudRepository.categories.indexOfFirst { it.id == categoryId }
        if (index >= 0) binding.categorySpinner.setSelection(index + 1)
    }

    private fun runSearch() {
        val categoryId = if (binding.categorySpinner.selectedItemPosition == 0) null
        else DummyCloudRepository.categories[binding.categorySpinner.selectedItemPosition - 1].id

        val locations = listOf(null, "Beirut", "Bekaa", "Keserwan-Jbeil", "North")
        val governorate = locations.getOrNull(binding.locationSpinner.selectedItemPosition)
        val minimumRating = when (binding.ratingSpinner.selectedItemPosition) {
            1 -> 3.0
            2 -> 4.0
            3 -> 4.5
            else -> 0.0
        }

        val results = DummyCloudRepository.searchProfessionals(
            query = binding.searchInput.text?.toString().orEmpty(),
            categoryId = categoryId,
            governorate = governorate,
            minimumRating = minimumRating,
            language = host.language()
        )
        professionalAdapter.submitList(results)
        binding.emptyResults.isVisible = results.isEmpty()
        binding.resultsList.isVisible = results.isNotEmpty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_CATEGORY_ID = "category_id"

        fun newInstance(categoryId: String? = null) = SearchFragment().apply {
            if (categoryId != null) arguments = Bundle().apply { putString(ARG_CATEGORY_ID, categoryId) }
        }
    }
}
