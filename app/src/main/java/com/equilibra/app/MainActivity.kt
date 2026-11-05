package com.equilibra.app

import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.equilibra.app.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * The only activity (single-activity architecture). It hosts the toolbar, the bottom
 * navigation and a NavHostFragment; each section of the app is a fragment inside it.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Keep content out from under the status bar. While the keyboard is open, hide the
        // bottom bar and end the screen right above the keyboard, so the form has room.
        // When the keyboard is closed, the bottom bar handles the navigation-bar inset itself.
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val keyboard = insets.getInsets(WindowInsetsCompat.Type.ime())
            val keyboardOpen = insets.isVisible(WindowInsetsCompat.Type.ime())
            binding.bottomNav.isVisible = !keyboardOpen
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, if (keyboardOpen) keyboard.bottom else 0)
            insets
        }

        setSupportActionBar(binding.toolbar)

        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // The five bottom-bar sections are top-level: no back arrow on them.
        appBarConfiguration = AppBarConfiguration(TAB_TITLES.keys)
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNav.setupWithNavController(navController)

        // Toolbar button that switches the language. Passing `this` ties it to the activity lifecycle.
        addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(
                    menu: Menu,
                    menuInflater: MenuInflater,
                ) {
                    menuInflater.inflate(R.menu.main_toolbar_menu, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    if (menuItem.itemId != R.id.action_switch_language) return false
                    switchLanguage()
                    return true
                }
            },
            this,
        )
    }

    /**
     * Switches only this app between English and Bulgarian, whatever the phone's language is.
     * AndroidX saves the choice. Because the manifest says this activity handles "locale"
     * changes, Android doesn't rebuild the screen; it calls onConfigurationChanged instead.
     */
    private fun switchLanguage() {
        // The language chosen with this button, or the phone's language if none was chosen yet.
        val current =
            AppCompatDelegate.getApplicationLocales()[0]?.language
                ?: ConfigurationCompat.getLocales(resources.configuration)[0]?.language
        val next = if (current == BULGARIAN) ENGLISH else BULGARIAN
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(next))
    }

    /**
     * Called instead of rebuilding the activity when the language changes. The resources
     * already return the new language here, so every text on screen is set again.
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateTexts()
    }

    private fun updateTexts() {
        // Bottom bar tabs.
        TAB_TITLES.forEach { (id, title) -> binding.bottomNav.menu.findItem(id)?.title = getString(title) }

        // The toolbar shows the destination's label, which was read once from nav_graph.xml.
        DESTINATION_TITLES.forEach { (id, title) -> navController.graph.findNode(id)?.label = getString(title) }
        supportActionBar?.title = navController.currentDestination?.label

        // The БГ / EN button: Android creates the toolbar menu again.
        invalidateMenu()

        // The screen that is open: rebuild only its view, so its layout is inflated again with
        // the new texts. The fragment and its ViewModel stay, so loaded data is kept.
        // Screens further back get the new texts when the user returns to them.
        // The open screen is the fragment shown in the NavHostFragment's container (the same id).
        val fragmentManager = navHostFragment.childFragmentManager
        val current = fragmentManager.findFragmentById(R.id.nav_host_fragment) ?: return
        fragmentManager.beginTransaction().detach(current).commitNow()
        fragmentManager.beginTransaction().attach(current).commitNow()
    }

    override fun onSupportNavigateUp(): Boolean = navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()

    private companion object {
        const val ENGLISH = "en"
        const val BULGARIAN = "bg"

        /** The five bottom-bar sections and their titles. */
        val TAB_TITLES =
            mapOf(
                R.id.selfAssessmentFragment to R.string.nav_self_assessment,
                R.id.habitsFragment to R.string.nav_habits,
                R.id.prioritiesFragment to R.string.nav_priorities,
                R.id.decisionsFragment to R.string.nav_decisions,
                R.id.mySystemFragment to R.string.nav_my_system,
            )

        /** Every screen and its toolbar title, the same as android:label in nav_graph.xml. */
        val DESTINATION_TITLES =
            TAB_TITLES +
                mapOf(
                    R.id.wheelEditFragment to R.string.wheel_edit_title,
                    R.id.findingEditFragment to R.string.finding_edit_title,
                    R.id.decisionEditFragment to R.string.decision_edit_title,
                )
    }
}
