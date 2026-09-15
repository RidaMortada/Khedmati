package com.example.khedmati.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.khedmati.databinding.ItemReviewBinding
import com.example.khedmati.model.Review

class ReviewAdapter : RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {
    private val items = mutableListOf<Review>()

    fun submitList(values: List<Review>) {
        items.clear()
        items.addAll(values)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = items.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])

    class ViewHolder(private val binding: ItemReviewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(review: Review) = with(binding) {
            reviewHeader.text = "${"★".repeat(review.rating)}  ${review.authorName}"
            reviewBody.text = review.text
        }
    }
}
