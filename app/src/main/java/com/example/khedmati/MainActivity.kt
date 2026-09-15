package com.example.khedmati

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.khedmati.data.DummyCloudRepository
import com.example.khedmati.data.DummyStorageService
import com.example.khedmati.model.LocationMode
import com.example.khedmati.model.Post
import com.example.khedmati.model.PriceMode
import com.example.khedmati.model.Professional
import com.example.khedmati.model.Service
import com.example.khedmati.util.AppPreferences
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var contentContainer: FrameLayout
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var preferences: AppPreferences

    private var activeTab = R.id.nav_home
    private var pendingPostImageUri: Uri? = null
    private var pendingPostPreview: ImageView? = null

    private data class FeedComment(
        val author: String,
        val text: String,
        val time: String
    )

    private val likedPostsUi = mutableSetOf<String>()
    private val feedComments = mutableMapOf<String, MutableList<FeedComment>>(
        "post-1" to mutableListOf(
            FeedComment("Maya", "Very clean work 👏", "18 min"),
            FeedComment("Karim", "Nice organization. Which breakers did you use?", "42 min"),
            FeedComment("Rami", "Professional finish, well done.", "1 h"),
            FeedComment("Nadine", "I like how clearly everything is labeled.", "1 h")
        ),
        "post-2" to mutableListOf(
            FeedComment("Hadi", "Great repair. The installation looks solid.", "5 h"),
            FeedComment("Lina", "Do you work around Zahle too?", "8 h")
        ),
        "post-3" to mutableListOf(
            FeedComment("Sara", "The before-and-after difference is impressive!", "2 d")
        )
    )

    private val workImagePicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            pendingPostImageUri = uri
            pendingPostPreview?.apply {
                visibility = View.VISIBLE
                setImageURI(uri)
            }
            Toast.makeText(this, R.string.work_image_selected, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = AppPreferences(this)
        restoreDummySession()
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        contentContainer = findViewById(R.id.contentContainer)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.appRoot)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            activeTab = item.itemId
            renderActiveTab()
            true
        }
        bottomNavigation.selectedItemId = R.id.nav_home

        if (!preferences.languageChosen) {
            showLanguageDialog(firstLaunch = true)
        } else {
            renderHome()
        }
    }

    private fun restoreDummySession() {
        val name = preferences.sessionName()
        val email = preferences.sessionEmail()
        if (name != null && email != null) {
            DummyCloudRepository.restoreSession(name, email)
        }
    }

    private fun currentLanguage(): String = preferences.language

    private fun renderActiveTab() {
        clearBackNavigation()
        when (activeTab) {
            R.id.nav_search -> renderSearch()
            R.id.nav_saved -> renderSaved()
            R.id.nav_account -> renderAccount()
            else -> renderHome()
        }
    }

    private fun clearBackNavigation() {
        toolbar.navigationIcon = null
        toolbar.setNavigationOnClickListener(null)
    }

    private fun showDetailBack(title: String, backAction: () -> Unit) {
        toolbar.title = title
        toolbar.setNavigationIcon(android.R.drawable.ic_media_previous)
        toolbar.setNavigationOnClickListener { backAction() }
    }

    private fun replaceContent(view: View) {
        contentContainer.removeAllViews()
        contentContainer.addView(
            view,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
    }

    private fun scrollColumn(): Pair<ScrollView, LinearLayout> {
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(Color.parseColor("#FFFDF9"))
        }
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(28))
        }
        scroll.addView(
            column,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        )
        return scroll to column
    }

    private fun renderHome() {
        clearBackNavigation()
        toolbar.title = getString(R.string.home)
        val (scroll, column) = scrollColumn()

        column.addView(titleText(getString(R.string.discover_local_services)))
        column.addView(bodyText(getString(R.string.home_subtitle)))
        column.addView(space(12))
        column.addView(sectionText(getString(R.string.categories)))

        val horizontal = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false }
        val chips = ChipGroup(this).apply { isSingleLine = true }
        DummyCloudRepository.categories.forEach { category ->
            chips.addView(Chip(this).apply {
                text = "${category.icon} ${category.name.resolve(currentLanguage())}"
                isCheckable = false
                setOnClickListener {
                    activeTab = R.id.nav_search
                    bottomNavigation.selectedItemId = R.id.nav_search
                    renderSearch(category.id)
                }
            })
        }
        horizontal.addView(chips)
        column.addView(horizontal)
        column.addView(space(22))
        column.addView(sectionText(getString(R.string.recent_posts)))

        DummyCloudRepository.posts.filter { it.isActive }.forEach { post ->
            column.addView(postCard(post))
            column.addView(space(12))
        }
        replaceContent(scroll)
    }

    private fun renderSearch(preselectedCategoryId: String? = null) {
        clearBackNavigation()
        toolbar.title = getString(R.string.search)
        val (scroll, column) = scrollColumn()

        column.addView(titleText(getString(R.string.find_professional)))
        val queryInput = EditText(this).apply {
            hint = getString(R.string.search_hint)
            inputType = InputType.TYPE_CLASS_TEXT
            contentDescription = getString(R.string.search_hint)
        }
        column.addView(queryInput, matchWrap())

        val categorySpinner = Spinner(this)
        val categoryLabels = mutableListOf(getString(R.string.all_categories))
        categoryLabels.addAll(DummyCloudRepository.categories.map { it.name.resolve(currentLanguage()) })
        categorySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categoryLabels)
        val requestedCategoryIndex = DummyCloudRepository.categories.indexOfFirst { it.id == preselectedCategoryId }
        if (requestedCategoryIndex >= 0) categorySpinner.setSelection(requestedCategoryIndex + 1)
        column.addView(labelText(getString(R.string.category)))
        column.addView(categorySpinner, matchWrap())

        val locations = listOf(
            getString(R.string.all_locations),
            "Beirut",
            "Bekaa",
            "Keserwan-Jbeil",
            "North"
        )
        val locationSpinner = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, locations)
        }
        column.addView(labelText(getString(R.string.location)))
        column.addView(locationSpinner, matchWrap())

        val ratingSpinner = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_spinner_dropdown_item,
                listOf(getString(R.string.any_rating), "3+", "4+", "4.5+")
            )
        }
        column.addView(labelText(getString(R.string.minimum_rating)))
        column.addView(ratingSpinner, matchWrap())

        val locationActions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        locationActions.addView(Button(this).apply {
            text = getString(R.string.near_me_dummy)
            setOnClickListener {
                MaterialAlertDialogBuilder(this@MainActivity)
                    .setTitle(R.string.current_location_dummy_title)
                    .setMessage(R.string.current_location_dummy_message)
                    .setPositiveButton(android.R.string.ok) { _, _ -> locationSpinner.setSelection(1) }
                    .show()
            }
        }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        locationActions.addView(Button(this).apply {
            text = getString(R.string.map_dummy)
            setOnClickListener {
                MaterialAlertDialogBuilder(this@MainActivity)
                    .setTitle(R.string.map_dummy_title)
                    .setMessage(R.string.map_dummy_message)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            }
        }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        column.addView(locationActions)

        val resultsContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        column.addView(Button(this).apply {
            text = getString(R.string.search)
            setOnClickListener {
                val categoryId = if (categorySpinner.selectedItemPosition == 0) null
                else DummyCloudRepository.categories[categorySpinner.selectedItemPosition - 1].id
                val governorate = if (locationSpinner.selectedItemPosition == 0) null
                else locations[locationSpinner.selectedItemPosition]
                val minimum = when (ratingSpinner.selectedItemPosition) {
                    1 -> 3.0
                    2 -> 4.0
                    3 -> 4.5
                    else -> 0.0
                }
                renderProfessionalResults(
                    resultsContainer,
                    DummyCloudRepository.searchProfessionals(
                        queryInput.text.toString(),
                        categoryId,
                        governorate,
                        minimum,
                        currentLanguage()
                    )
                )
            }
        }, matchWrap())
        column.addView(space(16))
        column.addView(sectionText(getString(R.string.results)))
        column.addView(resultsContainer, matchWrap())

        renderProfessionalResults(
            resultsContainer,
            DummyCloudRepository.searchProfessionals("", preselectedCategoryId, null, 0.0, currentLanguage())
        )
        replaceContent(scroll)
    }

    private fun renderProfessionalResults(container: LinearLayout, professionals: List<Professional>) {
        container.removeAllViews()
        if (professionals.isEmpty()) {
            container.addView(bodyText(getString(R.string.no_results)))
            return
        }
        professionals.forEach { professional ->
            container.addView(professionalCard(professional))
            container.addView(space(12))
        }
    }

    private fun renderSaved() {
        clearBackNavigation()
        toolbar.title = getString(R.string.saved)
        val (scroll, column) = scrollColumn()
        column.addView(titleText(getString(R.string.saved_professionals)))

        if (!requireSignedIn(showPrompt = false)) {
            column.addView(bodyText(getString(R.string.sign_in_to_save)))
            column.addView(Button(this).apply {
                text = getString(R.string.sign_in)
                setOnClickListener { showSignInDialog() }
            })
        } else {
            val saved = DummyCloudRepository.savedProfessionals()
            if (saved.isEmpty()) {
                column.addView(bodyText(getString(R.string.no_saved_professionals)))
            } else {
                saved.forEach { professional ->
                    column.addView(professionalCard(professional))
                    column.addView(space(12))
                }
            }
        }
        replaceContent(scroll)
    }

    private fun renderAccount() {
        clearBackNavigation()
        toolbar.title = getString(R.string.account)
        val (scroll, column) = scrollColumn()
        val user = DummyCloudRepository.currentUser

        if (user == null) {
            column.addView(titleText(getString(R.string.account_guest_title)))
            column.addView(bodyText(getString(R.string.account_guest_body)))
            column.addView(Button(this).apply {
                text = getString(R.string.sign_in)
                setOnClickListener { showSignInDialog() }
            })
        } else {
            column.addView(titleText(user.displayName))
            column.addView(bodyText(user.email))
            column.addView(bodyText(getString(R.string.single_account_explanation)))
            column.addView(space(12))
            column.addView(Button(this).apply {
                text = getString(R.string.create_post)
                setOnClickListener { renderCreatePost() }
            })
            column.addView(Button(this).apply {
                text = getString(R.string.manage_professional_profile)
                setOnClickListener { renderProfessionalManagement() }
            })
            column.addView(Button(this).apply {
                text = getString(R.string.notifications)
                setOnClickListener { renderNotifications() }
            })
            column.addView(Button(this).apply {
                text = getString(R.string.sign_out)
                setOnClickListener {
                    DummyCloudRepository.signOut()
                    preferences.clearSession()
                    renderAccount()
                }
            })
        }

        column.addView(space(20))
        column.addView(sectionText(getString(R.string.settings)))
        column.addView(Button(this).apply {
            text = getString(R.string.change_language)
            setOnClickListener { showLanguageDialog(firstLaunch = false) }
        })
        column.addView(Button(this).apply {
            text = getString(R.string.terms_privacy_rules)
            setOnClickListener {
                MaterialAlertDialogBuilder(this@MainActivity)
                    .setTitle(R.string.terms_privacy_rules)
                    .setMessage(R.string.legal_placeholder)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            }
        })
        column.addView(space(16))
        column.addView(dummyStatusCard())
        replaceContent(scroll)
    }

    private fun renderProfessionalProfile(professional: Professional) {
        showDetailBack(professional.publicName.resolve(currentLanguage())) { renderActiveTab() }
        val (scroll, column) = scrollColumn()

        column.addView(titleText(professional.publicName.resolve(currentLanguage())))
        column.addView(bodyText(DummyCloudRepository.categoryName(professional.primaryCategoryId, currentLanguage())))
        column.addView(ratingSummary(professional))
        column.addView(bodyText("📍 ${professional.locationLabel.resolve(currentLanguage())} • ${professional.serviceRadiusKm} km"))
        column.addView(bodyText(getString(R.string.location_privacy_format, locationModeText(professional.locationMode))))
        column.addView(space(8))
        column.addView(bodyText(professional.description.resolve(currentLanguage())))

        val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val saveButton = Button(this).apply {
            text = if (DummyCloudRepository.isSaved(professional.id)) getString(R.string.unsave) else getString(R.string.save)
            setOnClickListener {
                if (requireSignedIn()) {
                    val isSaved = DummyCloudRepository.toggleSaved(professional.id)
                    text = if (isSaved) getString(R.string.unsave) else getString(R.string.save)
                }
            }
        }
        val callButton = Button(this).apply {
            text = getString(R.string.call)
            setOnClickListener { confirmPhoneCall(professional.phone) }
        }
        actions.addView(saveButton, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        actions.addView(callButton, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        column.addView(actions)
        column.addView(Button(this).apply {
            text = getString(R.string.open_social_link)
            setOnClickListener { openExternalUrl(professional.socialUrl) }
        })

        column.addView(space(18))
        column.addView(sectionText(getString(R.string.services)))
        if (professional.services.isEmpty()) {
            column.addView(bodyText(getString(R.string.no_services_yet)))
        } else {
            professional.services.forEach { service ->
                column.addView(serviceCard(service))
                column.addView(space(10))
            }
        }

        column.addView(space(10))
        column.addView(sectionText(getString(R.string.reviews)))
        column.addView(bodyText(getString(R.string.unverified_review_notice)))
        if (DummyCloudRepository.currentUser != null) {
            column.addView(Button(this).apply {
                text = getString(R.string.write_review)
                setOnClickListener { showReviewDialog(professional) }
            })
        } else {
            column.addView(Button(this).apply {
                text = getString(R.string.sign_in_to_review)
                setOnClickListener { showSignInDialog() }
            })
        }

        val reviews = DummyCloudRepository.reviewsForProfessional(professional.id)
        if (reviews.isEmpty()) {
            column.addView(bodyText(getString(R.string.no_reviews_yet)))
        } else {
            reviews.forEach { review ->
                column.addView(bodyText("${"★".repeat(review.rating)}  ${review.authorName}\n${review.text}"))
                column.addView(space(8))
            }
        }

        column.addView(space(16))
        column.addView(sectionText(getString(R.string.posts)))
        val professionalPosts = DummyCloudRepository.postsForProfessional(professional.id)
        if (professionalPosts.isEmpty()) {
            column.addView(bodyText(getString(R.string.no_posts_yet)))
        } else {
            professionalPosts.forEach { post ->
                column.addView(postCard(post))
                column.addView(space(10))
            }
        }
        replaceContent(scroll)
    }

    private fun renderProfessionalManagement() {
        val user = DummyCloudRepository.currentUser
        val professional = user?.professionalId?.let { DummyCloudRepository.professionalById(it) }
        if (user == null || professional == null) {
            renderAccount()
            return
        }

        showDetailBack(getString(R.string.manage_professional_profile)) { renderAccount() }
        val (scroll, column) = scrollColumn()
        column.addView(titleText(getString(R.string.manage_professional_profile)))
        column.addView(bodyText(getString(R.string.professional_management_dummy_notice)))

        val nameInput = EditText(this).apply {
            hint = getString(R.string.public_name)
            setText(professional.publicName.resolve(currentLanguage()))
        }
        val descriptionInput = EditText(this).apply {
            hint = getString(R.string.description)
            setText(professional.description.resolve(currentLanguage()))
            minLines = 3
        }
        val phoneInput = EditText(this).apply {
            hint = getString(R.string.phone)
            setText(professional.phone)
            inputType = InputType.TYPE_CLASS_PHONE
        }
        val radiusInput = EditText(this).apply {
            hint = getString(R.string.service_radius)
            setText(professional.serviceRadiusKm.toString())
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        val privacySpinner = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_spinner_dropdown_item,
                listOf(
                    getString(R.string.location_exact),
                    getString(R.string.location_approximate),
                    getString(R.string.location_city_only)
                )
            )
            setSelection(
                when (professional.locationMode) {
                    LocationMode.EXACT -> 0
                    LocationMode.APPROXIMATE -> 1
                    LocationMode.CITY_ONLY -> 2
                }
            )
        }

        column.addView(nameInput, matchWrap())
        column.addView(descriptionInput, matchWrap())
        column.addView(phoneInput, matchWrap())
        column.addView(radiusInput, matchWrap())
        column.addView(labelText(getString(R.string.public_location_precision)))
        column.addView(privacySpinner, matchWrap())

        var profileImageUrl = professional.profileImageUrl
        val uploadStatus = bodyText(profileImageUrl ?: getString(R.string.no_dummy_image_attached))
        column.addView(Button(this).apply {
            text = getString(R.string.upload_profile_image_dummy)
            setOnClickListener {
                profileImageUrl = DummyStorageService.uploadImage("profile")
                uploadStatus.text = profileImageUrl
                Toast.makeText(this@MainActivity, R.string.dummy_upload_complete, Toast.LENGTH_SHORT).show()
            }
        })
        column.addView(uploadStatus)

        column.addView(Button(this).apply {
            text = getString(R.string.save_to_dummy_cloud)
            setOnClickListener {
                val mode = when (privacySpinner.selectedItemPosition) {
                    0 -> LocationMode.EXACT
                    2 -> LocationMode.CITY_ONLY
                    else -> LocationMode.APPROXIMATE
                }
                DummyCloudRepository.updateProfessional(
                    professional.id,
                    nameInput.text.toString().ifBlank { professional.publicName.resolve(currentLanguage()) },
                    descriptionInput.text.toString(),
                    phoneInput.text.toString(),
                    radiusInput.text.toString().toIntOrNull()?.coerceIn(1, 200) ?: professional.serviceRadiusKm,
                    mode,
                    profileImageUrl
                )
                Toast.makeText(this@MainActivity, R.string.saved_in_dummy_repository, Toast.LENGTH_SHORT).show()
            }
        })

        column.addView(space(20))
        column.addView(sectionText(getString(R.string.manage_services)))
        professional.services.forEach {
            column.addView(serviceCard(it))
            column.addView(space(8))
        }
        column.addView(Button(this).apply {
            text = getString(R.string.add_service)
            setOnClickListener { showAddServiceDialog(professional) }
        })
        replaceContent(scroll)
    }

    private fun renderCreatePost() {
        val user = DummyCloudRepository.currentUser
        if (user == null) {
            showSignInDialog()
            return
        }

        showDetailBack(getString(R.string.create_post)) { renderAccount() }
        val (scroll, column) = scrollColumn()
        column.addView(titleText(getString(R.string.create_post)))
        column.addView(bodyText(getString(R.string.post_dummy_notice)))

        val textInput = EditText(this).apply {
            hint = getString(R.string.post_text_hint)
            minLines = 4
            gravity = Gravity.TOP
        }
        column.addView(textInput, matchWrap())
        column.addView(space(8))

        pendingPostImageUri = null
        val preview = ImageView(this).apply {
            visibility = View.GONE
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundColor(Color.parseColor("#EEF8F4"))
            contentDescription = getString(R.string.work_photo)
        }
        pendingPostPreview = preview
        column.addView(preview, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(230)))
        column.addView(Button(this).apply {
            text = getString(R.string.attach_image_dummy)
            setOnClickListener { workImagePicker.launch(arrayOf("image/*")) }
        })
        column.addView(bodyText(getString(R.string.local_image_notice)))

        column.addView(Button(this).apply {
            text = getString(R.string.publish)
            setOnClickListener {
                val text = textInput.text.toString().trim()
                if (text.isBlank()) {
                    textInput.error = getString(R.string.required_field)
                } else {
                    DummyCloudRepository.createPost(user.professionalId, text, pendingPostImageUri?.toString())
                    pendingPostPreview = null
                    Toast.makeText(this@MainActivity, R.string.post_published_dummy, Toast.LENGTH_SHORT).show()
                    activeTab = R.id.nav_home
                    bottomNavigation.selectedItemId = R.id.nav_home
                    renderHome()
                }
            }
        })
        replaceContent(scroll)
    }

    private fun renderNotifications() {
        showDetailBack(getString(R.string.notifications)) { renderAccount() }
        val (scroll, column) = scrollColumn()
        column.addView(titleText(getString(R.string.notifications)))
        column.addView(bodyText(getString(R.string.notifications_dummy_notice)))
        DummyCloudRepository.notifications.forEach { notification ->
            val card = basicCard()
            val box = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(14), dp(12), dp(14), dp(12))
                addView(sectionText(notification.title.resolve(currentLanguage())))
                addView(bodyText(notification.body.resolve(currentLanguage())))
            }
            card.addView(box)
            column.addView(card)
            column.addView(space(10))
            notification.isRead = true
        }
        replaceContent(scroll)
    }

    private fun professionalCard(professional: Professional): View {
        val card = basicCard().apply {
            isClickable = true
            isFocusable = true
            setOnClickListener { renderProfessionalProfile(professional) }
        }
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
        }
        box.addView(sectionText(professional.publicName.resolve(currentLanguage())))
        box.addView(bodyText(DummyCloudRepository.categoryName(professional.primaryCategoryId, currentLanguage())))
        box.addView(ratingSummary(professional))
        box.addView(bodyText("📍 ${professional.locationLabel.resolve(currentLanguage())}"))
        box.addView(bodyText(professional.description.resolve(currentLanguage())))
        card.addView(box)
        return card
    }

    private fun postCard(post: Post): View {
        val professional = DummyCloudRepository.professionalById(post.professionalId)
        val card = basicCard()
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(12))
        }
        box.addView(TextView(this).apply {
            text = professional?.publicName?.resolve(currentLanguage()) ?: getString(R.string.professional)
            textSize = 16f
            setTextColor(Color.parseColor("#23453D"))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setOnClickListener { professional?.let { renderProfessionalProfile(it) } }
        })
        box.addView(bodyText(post.publishedLabel.resolve(currentLanguage())).apply {
            textSize = 13f
            setTextColor(Color.parseColor("#7A8984"))
        })
        box.addView(space(8))
        box.addView(postImageView(post, professional))
        box.addView(space(10))
        box.addView(bodyText(post.text.resolve(currentLanguage())))

        val commentPreview = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(4), 0, dp(4))
        }
        renderCommentPreview(commentPreview, post)
        box.addView(commentPreview)

        val statsText = bodyText(socialStatsText(post)).apply {
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
        card.addView(box)
        return card
    }

    private fun postImageView(post: Post, professional: Professional?): View {
        val imageUrl = post.imageUrl
        if (!imageUrl.isNullOrBlank() && (imageUrl.startsWith("content://") || imageUrl.startsWith("file://") || imageUrl.startsWith("android.resource://"))) {
            return ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(220))
                scaleType = ImageView.ScaleType.CENTER_CROP
                adjustViewBounds = true
                setBackgroundColor(Color.parseColor("#EEF8F4"))
                contentDescription = getString(R.string.work_photo)
                runCatching { setImageURI(Uri.parse(imageUrl)) }
                    .onFailure { setImageResource(android.R.drawable.ic_menu_gallery) }
            }
        }

        val category = professional?.let { DummyCloudRepository.categoryName(it.primaryCategoryId, currentLanguage()) }.orEmpty()
        val icon = DummyCloudRepository.categories.firstOrNull { it.id == professional?.primaryCategoryId }?.icon ?: "📷"
        return TextView(this).apply {
            text = "$icon\n${getString(R.string.work_photo)}\n$category"
            gravity = Gravity.CENTER
            textSize = 18f
            setTextColor(Color.parseColor("#315E53"))
            setBackgroundColor(Color.parseColor("#EAF7F2"))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(190))
        }
    }

    private fun serviceCard(service: Service): View {
        val card = basicCard()
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }
        box.addView(sectionText(service.title.resolve(currentLanguage())))
        box.addView(bodyText(service.description.resolve(currentLanguage())))
        box.addView(bodyText(priceText(service)))
        service.priceDetails?.let { box.addView(bodyText(it.resolve(currentLanguage()))) }
        card.addView(box)
        return card
    }

    private fun ratingSummary(professional: Professional): TextView =
        if (professional.reviewCount == 0) bodyText(getString(R.string.no_reviews_yet))
        else bodyText(getString(R.string.rating_summary, professional.rating, professional.reviewCount))

    private fun priceText(service: Service): String {
        val amount = service.minAmount?.let { formatAmount(it, service.currency) }
        val maxAmount = service.maxAmount?.let { formatAmount(it, service.currency) }
        return when (service.priceMode) {
            PriceMode.NONE -> getString(R.string.price_not_listed)
            PriceMode.FIXED -> getString(R.string.price_fixed, amount ?: "")
            PriceMode.STARTING_FROM -> getString(R.string.price_starting_from, amount ?: "")
            PriceMode.RANGE -> getString(R.string.price_range, amount ?: "", maxAmount ?: "")
            PriceMode.DESCRIPTION_ONLY -> getString(R.string.price_description_only)
        }
    }

    private fun formatAmount(amount: Double, currency: String?): String {
        val formatted = NumberFormat.getNumberInstance(Locale.US).format(amount)
        return if (currency.isNullOrBlank()) formatted else "$formatted $currency"
    }

    private fun showSignInDialog() {
        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), 0, dp(20), 0)
        }
        val name = EditText(this).apply { hint = getString(R.string.display_name) }
        val email = EditText(this).apply {
            hint = getString(R.string.email)
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }
        val password = EditText(this).apply {
            hint = getString(R.string.password)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        form.addView(name)
        form.addView(email)
        form.addView(password)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.sign_in)
            .setMessage(R.string.dummy_auth_notice)
            .setView(form)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.continue_label) { _, _ ->
                val displayName = name.text.toString().trim().ifBlank { getString(R.string.demo_user) }
                val address = email.text.toString().trim().ifBlank { "demo@khedmati.local" }
                val user = DummyCloudRepository.signIn(displayName, address)
                preferences.saveSession(user.displayName, user.email)
                Toast.makeText(this, R.string.signed_in_dummy, Toast.LENGTH_SHORT).show()
                renderAccount()
            }
            .show()
    }

    private fun requireSignedIn(showPrompt: Boolean = true): Boolean {
        if (DummyCloudRepository.currentUser != null) return true
        if (showPrompt) {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.sign_in_required)
                .setMessage(R.string.sign_in_required_message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.sign_in) { _, _ -> showSignInDialog() }
                .show()
        }
        return false
    }

    private fun commentsFor(post: Post): MutableList<FeedComment> =
        feedComments.getOrPut(post.id) { mutableListOf() }

    private fun likeActionText(post: Post): String =
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

    private fun renderCommentPreview(container: LinearLayout, post: Post) {
        container.removeAllViews()
        commentsFor(post).takeLast(2).forEach { comment ->
            container.addView(commentPreviewText(comment))
        }
    }

    private fun commentPreviewText(comment: FeedComment): TextView {
        val line = "${comment.author}  ${comment.text}"
        val styled = SpannableString(line).apply {
            setSpan(
                StyleSpan(android.graphics.Typeface.BOLD),
                0,
                comment.author.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return bodyText("").apply {
            text = styled
            textSize = 14f
            setPadding(0, dp(2), 0, dp(2))
        }
    }

    private fun commentDialogView(comment: FeedComment): View = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(0, dp(5), 0, dp(7))
        addView(TextView(this@MainActivity).apply {
            text = "${comment.author}  •  ${comment.time}"
            textSize = 14f
            setTextColor(Color.parseColor("#315E53"))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        })
        addView(bodyText(comment.text))
    }

    private fun showCommentsDialog(post: Post, button: Button, preview: LinearLayout, statsText: TextView) {
        val comments = commentsFor(post)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(4), dp(20), 0)
        }
        val commentsBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            if (comments.isEmpty()) {
                addView(bodyText(noCommentsLabel()))
            } else {
                comments.forEach { addView(commentDialogView(it)) }
            }
        }
        val scroll = ScrollView(this).apply {
            addView(
                commentsBox,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )
        }
        content.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(240)
            )
        )

        val user = DummyCloudRepository.currentUser
        val input = EditText(this).apply {
            hint = getString(R.string.comment_hint)
            minLines = 2
            maxLines = 4
        }
        if (user != null) {
            content.addView(input, matchWrap())
        } else {
            content.addView(bodyText(signInToCommentLabel()))
        }

        val builder = MaterialAlertDialogBuilder(this)
            .setTitle("💬 ${commentCountLabel(post.comments)}")
            .setView(content)
            .setNegativeButton(android.R.string.cancel, null)

        if (user == null) {
            builder.setPositiveButton(R.string.sign_in) { _, _ -> showSignInDialog() }
        } else {
            builder.setPositiveButton(R.string.add) { _, _ ->
                val value = input.text.toString().trim()
                if (value.isNotBlank()) {
                    comments.add(FeedComment(user.displayName, value, justNowLabel()))
                    DummyCloudRepository.addComment(post.id, value)
                    button.text = "💬"
                    button.contentDescription = commentCountLabel(post.comments)
                    statsText.text = socialStatsText(post)
                    renderCommentPreview(preview, post)
                }
            }
        }
        builder.show()
    }

    private fun commentCountLabel(count: Int): String = when (currentLanguage()) {
        "ar" -> "$count تعليق"
        "fr" -> "$count commentaire${if (count > 1) "s" else ""}"
        else -> "$count comment${if (count != 1) "s" else ""}"
    }

    private fun noCommentsLabel(): String = when (currentLanguage()) {
        "ar" -> "لا توجد تعليقات بعد. كن أول من يعلّق."
        "fr" -> "Aucun commentaire. Soyez le premier à commenter."
        else -> "No comments yet. Be the first to comment."
    }

    private fun signInToCommentLabel(): String = when (currentLanguage()) {
        "ar" -> "سجّل الدخول لإضافة تعليق."
        "fr" -> "Connectez-vous pour ajouter un commentaire."
        else -> "Sign in to add a comment."
    }

    private fun justNowLabel(): String = when (currentLanguage()) {
        "ar" -> "الآن"
        "fr" -> "À l’instant"
        else -> "Just now"
    }

    private fun showReviewDialog(professional: Professional) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), 0, dp(20), 0)
        }
        val rating = RatingBar(this, null, android.R.attr.ratingBarStyleSmall).apply {
            numStars = 5
            stepSize = 1f
            this.rating = 5f
        }
        val input = EditText(this).apply {
            hint = getString(R.string.review_hint)
            minLines = 3
        }
        box.addView(rating)
        box.addView(input)
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.write_review)
            .setMessage(R.string.unverified_review_notice)
            .setView(box)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.save) { _, _ ->
                val reviewText = input.text.toString().trim()
                if (reviewText.isNotBlank()) {
                    DummyCloudRepository.addOrUpdateReview(
                        professional.id,
                        rating.rating.toInt().coerceIn(1, 5),
                        reviewText
                    )
                    renderProfessionalProfile(professional)
                }
            }
            .show()
    }

    private fun showAddServiceDialog(professional: Professional) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), 0, dp(20), 0)
        }
        val title = EditText(this).apply { hint = getString(R.string.service_title) }
        val amount = EditText(this).apply {
            hint = getString(R.string.optional_price)
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val currency = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, listOf("USD", "LBP"))
        }
        box.addView(title)
        box.addView(amount)
        box.addView(currency)
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.add_service)
            .setView(box)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.add) { _, _ ->
                val serviceTitle = title.text.toString().trim()
                if (serviceTitle.isNotBlank()) {
                    DummyCloudRepository.addService(
                        professional.id,
                        serviceTitle,
                        amount.text.toString().toDoubleOrNull(),
                        currency.selectedItem.toString()
                    )
                    renderProfessionalManagement()
                }
            }
            .show()
    }

    private fun confirmPhoneCall(phone: String) {
        if (phone.isBlank()) {
            Toast.makeText(this, R.string.phone_not_set, Toast.LENGTH_SHORT).show()
            return
        }
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.confirm_call)
            .setMessage(getString(R.string.confirm_call_message, phone))
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.call) { _, _ ->
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(phone)}")))
            }
            .show()
    }

    private fun openExternalUrl(url: String) {
        runCatching { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
            .onFailure { Toast.makeText(this, R.string.cannot_open_link, Toast.LENGTH_SHORT).show() }
    }

    private fun sharePost(post: Post, professional: Professional?) {
        val body = buildString {
            professional?.let { append(it.publicName.resolve(currentLanguage())).append("\n") }
            append(post.text.resolve(currentLanguage()))
            append("\n")
            append(getString(R.string.share_fallback_note))
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, body)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share)))
    }

    private fun showLanguageDialog(firstLaunch: Boolean) {
        val labels = arrayOf("English", "العربية", "Français")
        MaterialAlertDialogBuilder(this)
            .setTitle(if (firstLaunch) R.string.choose_language else R.string.change_language)
            .setCancelable(!firstLaunch)
            .setSingleChoiceItems(
                labels,
                when (preferences.language) {
                    "ar" -> 1
                    "fr" -> 2
                    else -> 0
                }
            ) { dialog, which ->
                val tag = when (which) {
                    1 -> "ar"
                    2 -> "fr"
                    else -> "en"
                }
                preferences.language = tag
                preferences.languageChosen = true
                dialog.dismiss()
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
            }
            .show()
    }

    private fun locationModeText(mode: LocationMode): String = when (mode) {
        LocationMode.EXACT -> getString(R.string.location_exact)
        LocationMode.APPROXIMATE -> getString(R.string.location_approximate)
        LocationMode.CITY_ONLY -> getString(R.string.location_city_only)
    }

    private fun dummyStatusCard(): View {
        val card = basicCard()
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
        }
        box.addView(sectionText(getString(R.string.dummy_backend_title)))
        box.addView(bodyText(getString(R.string.dummy_backend_body)))
        box.addView(bodyText(DummyCloudRepository.DATABASE_STATUS))
        box.addView(bodyText(DummyStorageService.STATUS))
        box.addView(bodyText(DummyCloudRepository.NOTIFICATION_STATUS))
        card.addView(box)
        return card
    }

    private fun basicCard(): MaterialCardView = MaterialCardView(this).apply {
        radius = dp(18).toFloat()
        cardElevation = 0f
        strokeWidth = dp(1)
        strokeColor = Color.parseColor("#DCEBE5")
        setCardBackgroundColor(Color.WHITE)
        setContentPadding(0, 0, 0, 0)
    }

    private fun titleText(value: String): TextView = TextView(this).apply {
        text = value
        textSize = 26f
        setTextColor(Color.parseColor("#24483F"))
        setTypeface(typeface, android.graphics.Typeface.BOLD)
        setPadding(0, 0, 0, dp(6))
    }

    private fun sectionText(value: String): TextView = TextView(this).apply {
        text = value
        textSize = 18f
        setTextColor(Color.parseColor("#315E53"))
        setTypeface(typeface, android.graphics.Typeface.BOLD)
        setPadding(0, dp(4), 0, dp(6))
    }

    private fun labelText(value: String): TextView = TextView(this).apply {
        text = value
        textSize = 14f
        setTextColor(Color.parseColor("#456E64"))
        setTypeface(typeface, android.graphics.Typeface.BOLD)
        setPadding(0, dp(10), 0, 0)
    }

    private fun bodyText(value: String): TextView = TextView(this).apply {
        text = value
        textSize = 15f
        setTextColor(Color.parseColor("#46544F"))
        setLineSpacing(0f, 1.08f)
        setPadding(0, dp(3), 0, dp(3))
    }

    private fun space(heightDp: Int): View = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(1, dp(heightDp))
    }

    private fun matchWrap(): LinearLayout.LayoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
