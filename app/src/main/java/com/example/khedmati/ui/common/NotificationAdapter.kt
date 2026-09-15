package com.example.khedmati.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.khedmati.databinding.ItemNotificationBinding
import com.example.khedmati.model.NotificationItem

class NotificationAdapter(
    private val languageProvider: () -> String
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    private val items = mutableListOf<NotificationItem>()

    fun submitList(values: List<NotificationItem>) {
        items.clear()
        items.addAll(values)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = items.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])

    inner class ViewHolder(private val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NotificationItem) = with(binding) {
            val language = languageProvider()
            notificationTitle.text = item.title.resolve(language)
            notificationBody.text = item.body.resolve(language)
        }
    }
}
