package com.example.khedmati.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.khedmati.R
import com.example.khedmati.data.DummyCloudRepository
import com.example.khedmati.databinding.ItemProfessionalBinding
import com.example.khedmati.model.Professional

class ProfessionalAdapter(
    private val languageProvider: () -> String,
    private val onClick: (Professional) -> Unit
) : RecyclerView.Adapter<ProfessionalAdapter.ViewHolder>() {

    private val items = mutableListOf<Professional>()

    fun submitList(values: List<Professional>) {
        items.clear()
        items.addAll(values)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProfessionalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])

    inner class ViewHolder(private val binding: ItemProfessionalBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(professional: Professional) = with(binding) {
            val language = languageProvider()
            professionalName.text = professional.publicName.resolve(language)
            professionalCategory.text = DummyCloudRepository.categoryName(professional.primaryCategoryId, language)
            professionalRating.text = if (professional.reviewCount == 0) {
                root.context.getString(R.string.no_reviews_yet)
            } else {
                root.context.getString(R.string.rating_summary, professional.rating, professional.reviewCount)
            }
            professionalLocation.text = "📍 ${professional.locationLabel.resolve(language)}"
            professionalDescription.text = professional.description.resolve(language)
            root.setOnClickListener { onClick(professional) }
        }
    }
}
