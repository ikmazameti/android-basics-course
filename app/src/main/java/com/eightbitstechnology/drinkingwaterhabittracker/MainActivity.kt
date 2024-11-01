package com.eightbitstechnology.drinkingwaterhabittracker

import android.content.Context
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import com.eightbitstechnology.drinkingwaterhabittracker.databinding.ActivityMainBinding
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), MenuProvider {
    private lateinit var binding: ActivityMainBinding
    private lateinit var glassAdapter: GlassAdapter
    private val glasses = List(8) { Glass(id = it, isDrunk = false) }
    private val prefs by lazy {
        getSharedPreferences(
            "WaterTracker", Context.MODE_PRIVATE
        )
    }
    private val KEY_THEME_MODE = "theme_mode"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Apply the saved theme mode
        applySavedThemeMode()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Register the MenuProvider to the activity's lifecycle
        addMenuProvider(this, this, Lifecycle.State.RESUMED)

        // Set up RecyclerView with adapter
        glassAdapter = GlassAdapter { position, isChecked ->
            glasses[position].isDrunk = isChecked
            saveProgress()
            updateProgress()
        }


        // Check for daily reset
        checkForDailyReset()

        // Load saved state from SharedPreferences
        loadProgress()



        binding.glasses.adapter = glassAdapter
        glassAdapter.submitList(glasses)

    }

    private fun loadProgress() {
        glasses.forEachIndexed { index, glass ->
            glass.isDrunk = prefs.getBoolean("glass_$index", false)
        }
        updateProgress()
    }

    private fun saveProgress() {
        with(prefs.edit()) {
            glasses.forEachIndexed { index, glass ->
                putBoolean("glass_$index", glass.isDrunk)
            }
            apply()
        }
    }

    private fun updateProgress() {
        val count = glasses.count { it.isDrunk }
        binding.progressBar.progress = count
        binding.glassCount.text = getString(R.string.glasses_count, count, 8)
        // Trigger confetti animation if all glasses are completed
        if (count == 8) {
            showConfetti()
        }
    }

    private fun resetProgress() {
        glasses.forEach { it.isDrunk = false }
        saveProgress()
        updateProgress()
        glassAdapter.notifyDataSetChanged()
    }

    private fun checkForDailyReset() {
        val lastResetDate = prefs.getString("lastResetDate", null)
        val currentDate = getCurrentDate()

        if (lastResetDate != currentDate) {
            resetProgress() // Reset if a new day
            prefs.edit().putString("lastResetDate", currentDate).apply()
        }
    }

    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    /**
     * Called by the [MenuHost] to allow the [MenuProvider]
     * to inflate [MenuItem]s into the menu.
     *
     * @param menu         the menu to inflate the new menu items into
     * @param menuInflater the inflater to be used to inflate the updated menu
     */
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_main, menu)
    }

    /**
     * Called by the [MenuHost] when a [MenuItem] is selected from the menu.
     *
     * @param menuItem the menu item that was selected
     * @return `true` if the given menu item is handled by this menu provider,
     * `false` otherwise
     */
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.dayNight -> {
                toggleDayNightMode()
                true
            }

            R.id.resetProgress -> {
                resetProgress()
                true
            }

            else -> false
        }
    }


    override fun onPrepareMenu(menu: Menu) {
        super.onPrepareMenu(menu)
        // Update the icon each time the menu is prepared
        val dayNightItem = menu.findItem(R.id.dayNight)
        updateMenuIcon(dayNightItem)
    }

    private fun toggleDayNightMode() {
        val currentNightMode = resources.configuration.uiMode and UI_MODE_NIGHT_MASK
        val newMode = if (currentNightMode == UI_MODE_NIGHT_YES) {
            AppCompatDelegate.MODE_NIGHT_NO
        } else {
            AppCompatDelegate.MODE_NIGHT_YES
        }
        AppCompatDelegate.setDefaultNightMode(newMode)
        saveThemeMode(newMode)
    }

    private fun saveThemeMode(mode: Int) {
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply()
    }

    private fun applySavedThemeMode() {
        val savedMode = prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_NO)
        AppCompatDelegate.setDefaultNightMode(savedMode)
    }

    private fun updateMenuIcon(menuItem: MenuItem) {
        val currentNightMode = resources.configuration.uiMode and UI_MODE_NIGHT_MASK
        if (currentNightMode == UI_MODE_NIGHT_YES) {
            menuItem.setIcon(R.drawable.ic_day)
        } else {
            menuItem.setIcon(R.drawable.ic_night)
        }
    }


    private fun showConfetti() {
        // TODO: show well done msg

    }
}