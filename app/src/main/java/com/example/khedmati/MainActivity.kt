package com.example.khedmati

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.khedmati.data.DummyCloudRepository
import com.example.khedmati.databinding.ActivityMainBinding
import com.example.khedmati.databinding.DialogSignInBinding
import com.example.khedmati.ui.account.AccountFragment
import com.example.khedmati.ui.account.ManageProfileFragment
import com.example.khedmati.ui.account.NotificationsFragment
import com.example.khedmati.ui.home.HomeFragment
import com.example.khedmati.ui.post.CreatePostFragment
import com.example.khedmati.ui.profile.ProfessionalProfileFragment
import com.example.khedmati.ui.saved.SavedFragment
import com.example.khedmati.ui.search.SearchFragment
import com.example.khedmati.util.AppPreferences
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var preferences: AppPreferences
    private var currentRootItemId = R.id.nav_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = AppPreferences(this)
        restoreSession()
        applyStoredLanguage()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemInsets()
        setupNavigationDrawer()

        if (savedInstanceState == null) {
            showRoot(R.id.nav_home, HomeFragment(), getString(R.string.home))
            if (!preferences.languageChosen) showLanguageDialog(firstLaunch = true)
        } else {
            binding.navigationView.setCheckedItem(currentRootItemId)
            updateToolbarNavigation()
        }
    }

    private fun restoreSession() {
        val name = preferences.sessionName()
        val email = preferences.sessionEmail()
        if (name != null && email != null) DummyCloudRepository.restoreSession(name, email)
    }

    private fun applyStoredLanguage() {
        if (!preferences.languageChosen) return
        val requested = preferences.language
        if (AppCompatDelegate.getApplicationLocales().toLanguageTags() != requested) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(requested))
        }
    }

    private fun applySystemInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainContent) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
    }

    private fun setupNavigationDrawer() {
        binding.navigationView.setCheckedItem(R.id.nav_home)

        binding.navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_search -> showRoot(item.itemId, SearchFragment(), getString(R.string.search))
                R.id.nav_saved -> showRoot(item.itemId, SavedFragment(), getString(R.string.saved))
                R.id.nav_account -> showRoot(item.itemId, AccountFragment(), getString(R.string.account))
                else -> showRoot(R.id.nav_home, HomeFragment(), getString(R.string.home))
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        binding.toolbar.setNavigationOnClickListener {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        supportFragmentManager.addOnBackStackChangedListener {
            updateToolbarNavigation()
        }
    }

    private fun updateToolbarNavigation() {
        if (supportFragmentManager.backStackEntryCount == 0) {
            binding.toolbar.setNavigationIcon(R.drawable.ic_menu_24)
            binding.toolbar.title = rootTitle(currentRootItemId)
            binding.navigationView.setCheckedItem(currentRootItemId)
        } else {
            binding.toolbar.setNavigationIcon(android.R.drawable.ic_media_previous)
        }
    }

    private fun showRoot(itemId: Int, fragment: Fragment, title: String) {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        currentRootItemId = itemId
        binding.navigationView.setCheckedItem(itemId)
        binding.toolbar.setNavigationIcon(R.drawable.ic_menu_24)
        binding.toolbar.title = title
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    fun navigate(fragment: Fragment, title: String) {
        binding.toolbar.title = title
        binding.toolbar.setNavigationIcon(android.R.drawable.ic_media_previous)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(fragment::class.java.simpleName)
            .commit()
    }

    fun openSearch(categoryId: String? = null) {
        showRoot(R.id.nav_search, SearchFragment.newInstance(categoryId), getString(R.string.search))
    }

    fun openProfessional(professionalId: String) {
        val professional = DummyCloudRepository.professionalById(professionalId) ?: return
        navigate(
            ProfessionalProfileFragment.newInstance(professionalId),
            professional.publicName.resolve(preferences.language)
        )
    }

    fun openCreatePost() = navigate(CreatePostFragment(), getString(R.string.create_post))

    fun openManageProfile() = navigate(ManageProfileFragment(), getString(R.string.manage_professional_profile))

    fun openNotifications() = navigate(NotificationsFragment(), getString(R.string.notifications))

    fun showHome() = showRoot(R.id.nav_home, HomeFragment(), getString(R.string.home))

    fun showSignInDialog(onSuccess: (() -> Unit)? = null) {
        val dialogBinding = DialogSignInBinding.inflate(LayoutInflater.from(this))
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.sign_in)
            .setMessage(R.string.dummy_auth_notice)
            .setView(dialogBinding.root)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.continue_label) { _, _ ->
                val name = dialogBinding.nameInput.text?.toString()?.trim().orEmpty()
                    .ifBlank { getString(R.string.demo_user) }
                val email = dialogBinding.emailInput.text?.toString()?.trim().orEmpty()
                    .ifBlank { "demo@khedmati.local" }
                val user = DummyCloudRepository.signIn(name, email)
                preferences.saveSession(user.displayName, user.email)
                Toast.makeText(this, R.string.signed_in_dummy, Toast.LENGTH_SHORT).show()
                onSuccess?.invoke()
            }
            .show()
    }

    fun requireSignedIn(action: () -> Unit) {
        if (DummyCloudRepository.currentUser != null) action() else showSignInDialog(action)
    }

    fun showLanguageDialog(firstLaunch: Boolean = false) {
        val codes = arrayOf("en", "ar", "fr")
        val labels = arrayOf("English", "العربية", "Français")
        val selected = codes.indexOf(preferences.language).coerceAtLeast(0)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.choose_language)
            .setSingleChoiceItems(labels, selected) { dialog, which ->
                preferences.language = codes[which]
                preferences.languageChosen = true
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(codes[which]))
                dialog.dismiss()
            }
            .apply { if (firstLaunch) setCancelable(false) }
            .show()
    }

    fun signOut() {
        DummyCloudRepository.signOut()
        preferences.clearSession()
    }

    fun language(): String = preferences.language

    private fun rootTitle(itemId: Int): String = when (itemId) {
        R.id.nav_search -> getString(R.string.search)
        R.id.nav_saved -> getString(R.string.saved)
        R.id.nav_account -> getString(R.string.account)
        else -> getString(R.string.home)
    }
}
