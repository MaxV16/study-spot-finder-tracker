package org.setu.studyspotfinder.ui.map

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import org.setu.studyspotfinder.R

// placeholder so the drawer map item isnt dead. the real google map lands in issue 33.
class MapActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val toolbar = MaterialToolbar(this).apply {
            title = getString(R.string.map)
            setBackgroundColor(getColor(R.color.spot_primary))
            setTitleTextColor(getColor(R.color.white))
        }
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        setContentView(toolbar)
    }
}
