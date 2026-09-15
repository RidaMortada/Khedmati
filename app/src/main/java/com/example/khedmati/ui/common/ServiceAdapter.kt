package com.example.khedmati.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.khedmati.R
import com.example.khedmati.databinding.ItemServiceBinding
import com.example.khedmati.model.PriceMode
import com.example.khedmati.model.Service
import java.text.NumberFormat
import java.util.Locale

class ServiceAdapter(private val languageProvider: () -> String) : RecyclerView.Adapter<ServiceAdapter.ViewHolder>() {
    private val items = mutableListOf<Service>()

    fun submitList(values: List<Service>) {
        items.clear()
        items.addAll(values)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemServiceBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = items.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])

    inner class ViewHolder(private val binding: ItemServiceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(service: Service) = with(binding) {
            val language = languageProvider()
            serviceTitle.text = service.title.resolve(language)
            serviceDescription.text = service.description.resolve(language)
            servicePrice.text = priceText(service)
        }

        private fun priceText(service: Service): String {
            val context = binding.root.context
            fun amount(value: Double?): String {
                if (value == null) return ""
                val formatted = NumberFormat.getNumberInstance(Locale.US).format(value)
                return if (service.currency.isNullOrBlank()) formatted else "$formatted ${service.currency}"
            }
            return when (service.priceMode) {
                PriceMode.NONE -> context.getString(R.string.price_not_listed)
                PriceMode.FIXED -> context.getString(R.string.price_fixed, amount(service.minAmount))
                PriceMode.STARTING_FROM -> context.getString(R.string.price_starting_from, amount(service.minAmount))
                PriceMode.RANGE -> context.getString(R.string.price_range, amount(service.minAmount), amount(service.maxAmount))
                PriceMode.DESCRIPTION_ONLY -> service.priceDetails?.resolve(languageProvider()) ?: context.getString(R.string.price_description_only)
            }
        }
    }
}
