from pathlib import Path

path = Path('app/src/main/java/com/example/khedmati/MainActivity.kt')
text = path.read_text(encoding='utf-8')

old = '''        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(6), 0, 0)
        }
        val likeButton = Button(this).apply {
            isAllCaps = false
            text = likeActionText(post)
            textSize = 16f
            setTextColor(Color.parseColor("#B74C5C"))
            contentDescription = "Like"
            setOnClickListener {
                if (requireSignedIn()) {
                    val liked = DummyCloudRepository.toggleLike(post.id)
                    if (liked) likedPostsUi.add(post.id) else likedPostsUi.remove(post.id)
                    text = likeActionText(post)
                }
            }
        }
        val commentButton = Button(this).apply {
            isAllCaps = false
            text = "💬 ${post.comments}"
            textSize = 15f
            contentDescription = commentCountLabel(post.comments)
        }
        commentButton.setOnClickListener {
            showCommentsDialog(post, commentButton, commentPreview)
        }
        val shareButton = Button(this).apply {
            isAllCaps = false
            text = "↗ ${getString(R.string.share)}"
            textSize = 15f
            contentDescription = getString(R.string.share)
            setOnClickListener { sharePost(post, professional) }
        }
        actions.addView(likeButton, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        actions.addView(commentButton, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        actions.addView(shareButton, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        box.addView(actions)
'''

new = '''        val statsText = bodyText(socialStatsText(post)).apply {
            textSize = 13f
            setTextColor(Color.parseColor("#788580"))
            setPadding(0, dp(4), 0, dp(2))
        }
        box.addView(statsText)

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(2), 0, 0)
        }
        val likeButton = Button(this).apply {
            makeFlatSocialAction(this)
            text = likeActionText(post)
            setTextColor(Color.parseColor("#C8465A"))
            contentDescription = "Like"
            setOnClickListener {
                if (requireSignedIn()) {
                    val liked = DummyCloudRepository.toggleLike(post.id)
                    if (liked) likedPostsUi.add(post.id) else likedPostsUi.remove(post.id)
                    text = likeActionText(post)
                    statsText.text = socialStatsText(post)
                }
            }
        }
        val commentButton = Button(this).apply {
            makeFlatSocialAction(this)
            text = "💬"
            setTextColor(Color.parseColor("#44524E"))
            contentDescription = commentCountLabel(post.comments)
        }
        commentButton.setOnClickListener {
            showCommentsDialog(post, commentButton, commentPreview, statsText)
        }
        val shareButton = Button(this).apply {
            makeFlatSocialAction(this)
            text = "↗"
            setTextColor(Color.parseColor("#44524E"))
            contentDescription = getString(R.string.share)
            setOnClickListener { sharePost(post, professional) }
        }
        actions.addView(likeButton, LinearLayout.LayoutParams(0, dp(48), 1f))
        actions.addView(commentButton, LinearLayout.LayoutParams(0, dp(48), 1f))
        actions.addView(shareButton, LinearLayout.LayoutParams(0, dp(48), 1f))
        box.addView(actions)
'''

if old not in text:
    raise SystemExit('Expected action block not found')
text = text.replace(old, new, 1)

old_like = '''    private fun likeActionText(post: Post): String =
        "${if (post.id in likedPostsUi) "♥" else "♡"} ${post.likes}"
'''
new_like = '''    private fun likeActionText(post: Post): String =
        if (post.id in likedPostsUi) "♥" else "♡"

    private fun makeFlatSocialAction(button: Button) {
        button.isAllCaps = false
        button.background = null
        button.stateListAnimator = null
        button.minWidth = 0
        button.minimumWidth = 0
        button.minHeight = 0
        button.minimumHeight = 0
        button.textSize = 25f
        button.setPadding(dp(8), 0, dp(8), 0)
    }

    private fun socialStatsText(post: Post): String = when (currentLanguage()) {
        "ar" -> "${post.likes} إعجاب   ·   ${post.comments} تعليق"
        "fr" -> "${post.likes} J’aime   ·   ${post.comments} commentaire${if (post.comments > 1) "s" else ""}"
        else -> "${post.likes} like${if (post.likes != 1) "s" else ""}   ·   ${post.comments} comment${if (post.comments != 1) "s" else ""}"
    }
'''
if old_like not in text:
    raise SystemExit('Expected likeActionText block not found')
text = text.replace(old_like, new_like, 1)

old_sig = '    private fun showCommentsDialog(post: Post, button: Button, preview: LinearLayout) {'
new_sig = '    private fun showCommentsDialog(post: Post, button: Button, preview: LinearLayout, statsText: TextView) {'
if old_sig not in text:
    raise SystemExit('Expected comments dialog signature not found')
text = text.replace(old_sig, new_sig, 1)

old_update = '''                    button.text = "💬 ${post.comments}"
                    button.contentDescription = commentCountLabel(post.comments)
                    renderCommentPreview(preview, post)
'''
new_update = '''                    button.text = "💬"
                    button.contentDescription = commentCountLabel(post.comments)
                    statsText.text = socialStatsText(post)
                    renderCommentPreview(preview, post)
'''
if old_update not in text:
    raise SystemExit('Expected comment update block not found')
text = text.replace(old_update, new_update, 1)

path.write_text(text, encoding='utf-8')
