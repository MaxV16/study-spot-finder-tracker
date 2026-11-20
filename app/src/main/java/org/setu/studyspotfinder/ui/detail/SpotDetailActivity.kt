package org.setu.studyspotfinder.ui.detail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import coil.load
import org.setu.studyspotfinder.R
import org.setu.studyspotfinder.data.AppData
import org.setu.studyspotfinder.databinding.ActivitySpotDetailBinding
import org.setu.studyspotfinder.models.StudySpot
import org.setu.studyspotfinder.ui.AddEditActivity

// one spot, read only. we only pass the id in and reload it here, so a stale parcelable can never lie to us.
class SpotDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySpotDetailBinding
    private var spotId: Long = -1L
    private var currentSpot: StudySpot? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpotDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        spotId = intent.getLongExtra("id", -1L)
        if (spotId == -1L) {
            // no id at all, nothing to show
            finish()
            return
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.openMapButton.setOnClickListener { openInMaps() }
        binding.editButton.setOnClickListener { openEditor() }
    }

    // comes back here after the edit form closes, so just load again
    override fun onResume() {
        super.onResume()
        loadSpot()
    }

    private fun loadSpot() {
        val spot = AppData.studySpots.findOne(spotId)

        // spot got deleted somewhere else, dont crash, just leave
        if (spot == null) {
            Toast.makeText(this, R.string.spot_not_found, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        currentSpot = spot
        binding.spotTitle.text = spot.title
        binding.spotCategory.text = spot.category
        binding.spotCategory.visibility =
            if (spot.category.isBlank()) View.GONE else View.VISIBLE
        binding.spotDesc.text = spot.desc
        binding.spotCoords.text = getString(
            R.string.coords_format, spot.x.toString(), spot.y.toString()
        )

        if (spot.imageUri.isNullOrBlank()) {
            // no photo on this spot, so hide the frame. a giant placeholder icon looked broken.
            binding.spotImage.visibility = View.GONE
        } else {
            binding.spotImage.visibility = View.VISIBLE
            binding.spotImage.load(spot.imageUri) {
                crossfade(true)
                placeholder(android.R.drawable.ic_menu_gallery)
                error(android.R.drawable.ic_menu_gallery)
            }
        }

        binding.toolbar.title = spot.title
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_spot_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_edit -> {
                openEditor()
                true
            }
            R.id.action_share -> {
                shareSpot()
                true
            }
            R.id.action_delete -> {
                confirmDelete()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openEditor() {
        startActivity(
            Intent(this, AddEditActivity::class.java).putExtra("id", spotId)
        )
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out)
    }

    private fun shareSpot() {
        val spot = currentSpot ?: return
        val text = buildString {
            appendLine(spot.title)
            if (spot.desc.isNotBlank()) appendLine(spot.desc)
            appendLine("geo:${spot.x},${spot.y}")
        }

        startActivity(
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, spot.title)
                putExtra(Intent.EXTRA_TEXT, text)
            }
        )
    }

    private fun openInMaps() {
        val spot = currentSpot ?: return
        // spaces and & in a title would break the uri if we didnt encode it
        val label = Uri.encode(spot.title)
        val uri = Uri.parse("geo:${spot.x},${spot.y}?q=${spot.x},${spot.y}($label)")

        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, R.string.no_maps_app, Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_spot_title)
            .setMessage(R.string.delete_spot_message)
            .setPositiveButton(R.string.delete) { _, _ ->
                AppData.studySpots.delete(spotId)
                finish()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    // however this screen closes (back, delete, missing spot) it fades back out
    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_right)
    }
}
