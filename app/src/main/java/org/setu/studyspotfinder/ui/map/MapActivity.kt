package org.setu.studyspotfinder.ui.map

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import org.setu.studyspotfinder.R
import org.setu.studyspotfinder.data.AppData
import org.setu.studyspotfinder.databinding.ActivityMapBinding
import org.setu.studyspotfinder.ui.detail.SpotDetailActivity
import timber.log.Timber

// shows every saved spot as a marker. tapping the little info bubble opens the detail screen.
class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapBinding
    private lateinit var map: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isCompassEnabled = true

        // the marker tag holds the spot id so we know where to go on tap
        map.setOnInfoWindowClickListener { marker ->
            val id = marker.tag as? Long ?: return@setOnInfoWindowClickListener
            startActivity(
                Intent(this, SpotDetailActivity::class.java).putExtra("id", id)
            )
        }

        plotSpots()
    }

    private fun plotSpots() {
        map.clear()

        val spots = AppData.studySpots.findAll()
        binding.emptyText.visibility = if (spots.isEmpty()) View.VISIBLE else View.GONE

        if (spots.isEmpty()) {
            // no spots, just park the camera over dublin
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(LatLng(53.3498, -6.2603), 11f)
            )
            return
        }

        val bounds = LatLngBounds.Builder()

        spots.forEach { spot ->
            val position = LatLng(spot.x, spot.y)
            val marker = map.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(spot.title)
                    .snippet(
                        if (spot.category.isBlank()) spot.desc
                        else "${spot.category} \u00b7 ${spot.desc}"
                    )
            )
            marker?.tag = spot.id
            bounds.include(position)
        }

        // zoom out enough that every marker is on screen
        try {
            map.animateCamera(
                CameraUpdateFactory.newLatLngBounds(bounds.build(), 140)
            )
        } catch (e: IllegalStateException) {
            // only happens if the list was empty which we already handled, but just in case
            Timber.w(e, "Could not fit map bounds")
        }
    }

    override fun onResume() {
        super.onResume()
        if (::map.isInitialized) plotSpots()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
