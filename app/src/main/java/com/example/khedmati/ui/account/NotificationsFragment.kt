package com.example.khedmati.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.khedmati.MainActivity
import com.example.khedmati.data.DummyCloudRepository
import com.example.khedmati.databinding.FragmentNotificationsBinding
import com.example.khedmati.ui.common.NotificationAdapter

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private val host get() = requireActivity() as MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = NotificationAdapter { host.language() }
        binding.notificationsList.layoutManager = LinearLayoutManager(requireContext())
        binding.notificationsList.adapter = adapter
        adapter.submitList(DummyCloudRepository.notifications)
        DummyCloudRepository.notifications.forEach { it.isRead = true }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
