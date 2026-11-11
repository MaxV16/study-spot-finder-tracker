package org.setu.studyspotfinder.ui.settings

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.materialswitch.MaterialSwitch
import org.setu.studyspotfinder.R
import org.setu.studyspotfinder.data.ThemePrefs

// settings screen. right now its just the dark mode switch.
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val toolbar = MaterialToolbar(this).apply {
            title = getString(R.string.settings)
            setBackgroundColor(getColor(R.color.spot_primary))
            setTitleTextColor(getColor(R.color.white))
        }

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // the switch rebuilds the activity with the other theme, so the screen will blink once
        val darkSwitch = MaterialSwitch(this).apply {
            text = getString(R.string.dark_mode)
            isChecked = ThemePrefs.isDark(this@SettingsActivity)
            setPadding(48, 32, 48, 32)
            setOnCheckedChangeListener { _, checked ->
                ThemePrefs.setDark(this@SettingsActivity, checked)
            }
        }

        root.addView(toolbar)
        root.addView(darkSwitch)
        setContentView(root)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
