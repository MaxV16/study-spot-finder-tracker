package org.setu.studyspotfinder.ui.settings

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import org.setu.studyspotfinder.R

// settings screen. just a shell for now, the night mode switch comes with issue 35.
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

        val note = TextView(this).apply {
            text = getString(R.string.settings_placeholder)
            setPadding(48, 48, 48, 48)
        }

        root.addView(toolbar)
        root.addView(note)
        setContentView(root)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
