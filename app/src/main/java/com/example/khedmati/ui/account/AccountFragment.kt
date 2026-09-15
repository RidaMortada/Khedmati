package com.example.khedmati.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.khedmati.MainActivity
import com.example.khedmati.R
import com.example.khedmati.data.DummyCloudRepository
import com.example.khedmati.data.DummyStorageService
import com.example.khedmati.databinding.FragmentAccountBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private val host get() = requireActivity() as MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.signInButton.setOnClickListener { host.showSignInDialog { renderState() } }
        binding.createPostButton.setOnClickListener { host.openCreatePost() }
        binding.manageProfileButton.setOnClickListener { host.openManageProfile() }
        binding.notificationsButton.setOnClickListener { host.openNotifications() }
        binding.signOutButton.setOnClickListener {
            host.signOut()
            renderState()
        }
        binding.changeLanguageButton.setOnClickListener { host.showLanguageDialog() }
        binding.legalButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.terms_privacy_rules)
                .setMessage(R.string.legal_placeholder)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
        binding.backendStatus.text = buildString {
            append(getString(R.string.dummy_backend_title)).append("\n\n")
            append(getString(R.string.dummy_backend_body)).append("\n\n")
            append(DummyCloudRepository.DATABASE_STATUS).append('\n')
            append(DummyStorageService.STATUS).append('\n')
            append(DummyCloudRepository.NOTIFICATION_STATUS)
        }
        renderState()
    }

    override fun onResume() {
        super.onResume()
        if (_binding != null) renderState()
    }

    private fun renderState() {
        val user = DummyCloudRepository.currentUser
        binding.guestSection.isVisible = user == null
        binding.userSection.isVisible = user != null
        if (user != null) {
            binding.accountName.text = user.displayName
            binding.accountEmail.text = user.email
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
