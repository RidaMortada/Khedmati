package com.example.khedmati.ui.profile

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.khedmati.MainActivity
import com.example.khedmati.R
import com.example.khedmati.data.DummyCloudRepository
import com.example.khedmati.databinding.DialogCommentBinding
import com.example.khedmati.databinding.DialogReviewBinding
import com.example.khedmati.databinding.FragmentProfessionalProfileBinding
import com.example.khedmati.model.LocationMode
import com.example.khedmati.model.Post
import com.example.khedmati.model.Professional
import com.example.khedmati.ui.common.PostAdapter
import com.example.khedmati.ui.common.ReviewAdapter
import com.example.khedmati.ui.common.ServiceAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ProfessionalProfileFragment : Fragment() {

    private var _binding: FragmentProfessionalProfileBinding? = null
    private val binding get() = _binding!!
    private val host get() = requireActivity() as MainActivity
    private lateinit var professional: Professional
    private lateinit var serviceAdapter: ServiceAdapter
    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfessionalProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val id = requireArguments().getString(ARG_ID).orEmpty()
        professional = DummyCloudRepository.professionalById(id) ?: run {
            parentFragmentManager.popBackStack()
            return
        }
        setupLists()
        setupActions()
        renderProfile()
    }

    private fun setupLists() {
        serviceAdapter = ServiceAdapter { host.language() }
        reviewAdapter = ReviewAdapter()
        postAdapter = PostAdapter(
            languageProvider = { host.language() },
            onProfileClick = { },
            onLike = { post -> host.requireSignedIn { DummyCloudRepository.toggleLike(post.id); postAdapter.refresh(post.id) } },
            onComment = { post -> host.requireSignedIn { showCommentDialog(post) } },
            onShare = ::sharePost
        )
        binding.servicesList.layoutManager = LinearLayoutManager(requireContext())
        binding.servicesList.adapter = serviceAdapter
        binding.reviewsList.layoutManager = LinearLayoutManager(requireContext())
        binding.reviewsList.adapter = reviewAdapter
        binding.profilePostsList.layoutManager = LinearLayoutManager(requireContext())
        binding.profilePostsList.adapter = postAdapter
    }

    private fun setupActions() {
        binding.saveButton.setOnClickListener {
            host.requireSignedIn {
                DummyCloudRepository.toggleSaved(professional.id)
                updateSaveButton()
            }
        }
        binding.callButton.setOnClickListener {
            if (professional.phone.isBlank()) {
                Toast.makeText(requireContext(), R.string.phone_not_set, Toast.LENGTH_SHORT).show()
            } else {
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${professional.phone}")))
            }
        }
        binding.socialButton.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(professional.socialUrl)))
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(requireContext(), R.string.cannot_open_link, Toast.LENGTH_SHORT).show()
            }
        }
        binding.reviewButton.setOnClickListener {
            host.requireSignedIn { showReviewDialog() }
        }
    }

    private fun renderProfile() {
        val language = host.language()
        binding.profileName.text = professional.publicName.resolve(language)
        binding.profileCategory.text = DummyCloudRepository.categoryName(professional.primaryCategoryId, language)
        binding.profileRating.text = if (professional.reviewCount == 0) getString(R.string.no_reviews_yet)
        else getString(R.string.rating_summary, professional.rating, professional.reviewCount)
        binding.profileLocation.text = "📍 ${professional.locationLabel.resolve(language)} • ${professional.serviceRadiusKm} km\n${getString(R.string.location_privacy_format, locationModeLabel(professional.locationMode))}"
        binding.profileDescription.text = professional.description.resolve(language)
        serviceAdapter.submitList(professional.services)
        reviewAdapter.submitList(DummyCloudRepository.reviewsForProfessional(professional.id))
        postAdapter.submitList(DummyCloudRepository.postsForProfessional(professional.id))
        updateSaveButton()
    }

    private fun updateSaveButton() {
        binding.saveButton.text = if (DummyCloudRepository.isSaved(professional.id)) getString(R.string.unsave) else getString(R.string.save)
    }

    private fun showReviewDialog() {
        val dialogBinding = DialogReviewBinding.inflate(layoutInflater)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.write_review)
            .setView(dialogBinding.root)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.add) { _, _ ->
                val rating = dialogBinding.ratingBar.rating.toInt().coerceIn(1, 5)
                val text = dialogBinding.reviewInput.text?.toString()?.trim().orEmpty()
                if (text.isNotBlank()) {
                    DummyCloudRepository.addOrUpdateReview(professional.id, rating, text)
                    renderProfile()
                }
            }
            .show()
    }

    private fun showCommentDialog(post: Post) {
        val dialogBinding = DialogCommentBinding.inflate(layoutInflater)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_comment)
            .setView(dialogBinding.root)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.add) { _, _ ->
                val text = dialogBinding.commentInput.text?.toString()?.trim().orEmpty()
                if (text.isNotBlank()) {
                    DummyCloudRepository.addComment(post.id, text)
                    postAdapter.refresh(post.id)
                }
            }
            .show()
    }

    private fun sharePost(post: Post) {
        val text = "${professional.publicName.resolve(host.language())}\n\n${post.text.resolve(host.language())}\n\n${getString(R.string.share_fallback_note)}"
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }, getString(R.string.share)))
    }

    private fun locationModeLabel(mode: LocationMode): String = when (mode) {
        LocationMode.EXACT -> getString(R.string.location_exact)
        LocationMode.APPROXIMATE -> getString(R.string.location_approximate)
        LocationMode.CITY_ONLY -> getString(R.string.location_city_only)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_ID = "professional_id"
        fun newInstance(id: String) = ProfessionalProfileFragment().apply {
            arguments = Bundle().apply { putString(ARG_ID, id) }
        }
    }
}
