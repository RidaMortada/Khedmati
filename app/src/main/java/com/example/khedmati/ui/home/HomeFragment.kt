package com.example.khedmati.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.khedmati.MainActivity
import com.example.khedmati.R
import com.example.khedmati.data.DummyCloudRepository
import com.example.khedmati.databinding.DialogCommentBinding
import com.example.khedmati.databinding.FragmentHomeBinding
import com.example.khedmati.model.Post
import com.example.khedmati.ui.common.PostAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var postAdapter: PostAdapter
    private val host get() = requireActivity() as MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCategories()
        setupPosts()
    }

    override fun onResume() {
        super.onResume()
        if (::postAdapter.isInitialized) {
            postAdapter.submitList(DummyCloudRepository.posts.filter { it.isActive })
        }
    }

    private fun setupCategories() {
        binding.categoryChips.removeAllViews()
        DummyCloudRepository.categories.forEach { category ->
            binding.categoryChips.addView(Chip(requireContext()).apply {
                text = "${category.icon} ${category.name.resolve(host.language())}"
                isCheckable = false
                setOnClickListener { host.openSearch(category.id) }
            })
        }
    }

    private fun setupPosts() {
        postAdapter = PostAdapter(
            languageProvider = { host.language() },
            onProfileClick = host::openProfessional,
            onLike = { post ->
                host.requireSignedIn {
                    DummyCloudRepository.toggleLike(post.id)
                    postAdapter.refresh(post.id)
                }
            },
            onComment = { post -> host.requireSignedIn { showCommentDialog(post) } },
            onShare = ::sharePost
        )
        binding.postsList.layoutManager = LinearLayoutManager(requireContext())
        binding.postsList.adapter = postAdapter
        postAdapter.submitList(DummyCloudRepository.posts.filter { it.isActive })
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
        val professional = DummyCloudRepository.professionalById(post.professionalId)
        val text = buildString {
            append(professional?.publicName?.resolve(host.language()) ?: getString(R.string.professional))
            append("\n\n")
            append(post.text.resolve(host.language()))
            append("\n\n")
            append(getString(R.string.share_fallback_note))
        }
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }, getString(R.string.share)))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
