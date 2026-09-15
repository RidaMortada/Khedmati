package com.example.khedmati.ui.common

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.khedmati.R
import com.example.khedmati.data.DummyCloudRepository
import com.example.khedmati.databinding.ItemPostBinding
import com.example.khedmati.model.Post

class PostAdapter(
    private val languageProvider: () -> String,
    private val onProfileClick: (String) -> Unit,
    private val onLike: (Post) -> Unit,
    private val onComment: (Post) -> Unit,
    private val onShare: (Post) -> Unit
) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    private val items = mutableListOf<Post>()

    fun submitList(values: List<Post>) {
        items.clear()
        items.addAll(values)
        notifyDataSetChanged()
    }

    fun refresh(postId: String) {
        val index = items.indexOfFirst { it.id == postId }
        if (index >= 0) notifyItemChanged(index)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])

    inner class ViewHolder(private val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) = with(binding) {
            val language = languageProvider()
            val professional = DummyCloudRepository.professionalById(post.professionalId)
            postAuthor.text = professional?.publicName?.resolve(language) ?: root.context.getString(R.string.professional)
            postTime.text = post.publishedLabel.resolve(language)
            postBody.text = post.text.resolve(language)
            postStats.text = statsText(post, language)
            likeButton.text = if (DummyCloudRepository.isLiked(post.id)) "♥" else "♡"

            val imageUrl = post.imageUrl
            val localImage = !imageUrl.isNullOrBlank() && (
                imageUrl.startsWith("content://") || imageUrl.startsWith("file://") || imageUrl.startsWith("android.resource://")
            )
            if (localImage) {
                postImage.visibility = View.VISIBLE
                postImagePlaceholder.visibility = View.GONE
                runCatching { postImage.setImageURI(Uri.parse(imageUrl)) }
                    .onFailure {
                        postImage.visibility = View.GONE
                        postImagePlaceholder.visibility = View.VISIBLE
                    }
            } else {
                postImage.visibility = View.GONE
                postImagePlaceholder.visibility = View.VISIBLE
                val icon = DummyCloudRepository.categories.firstOrNull { it.id == professional?.primaryCategoryId }?.icon ?: "📷"
                postImagePlaceholder.text = "$icon\n${root.context.getString(R.string.work_photo)}"
            }

            postAuthor.setOnClickListener { onProfileClick(post.professionalId) }
            likeButton.setOnClickListener { onLike(post) }
            commentButton.setOnClickListener { onComment(post) }
            shareButton.setOnClickListener { onShare(post) }
        }
    }

    private fun statsText(post: Post, language: String): String = when (language) {
        "ar" -> "${post.likes} إعجاب   ·   ${post.comments} تعليق"
        "fr" -> "${post.likes} J’aime   ·   ${post.comments} commentaire${if (post.comments > 1) "s" else ""}"
        else -> "${post.likes} like${if (post.likes != 1) "s" else ""}   ·   ${post.comments} comment${if (post.comments != 1) "s" else ""}"
    }
}
