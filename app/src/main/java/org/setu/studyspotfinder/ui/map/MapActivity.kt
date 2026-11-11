package org.setu.studyspotfinder.ui.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
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
import org.setu.studyspotfinder.ui.AddEditActivity
import org.setu.studyspotfinder.ui.detail.SpotDetailActivity
import timber.log.Timber

// shows every saved spot as a marker. tapping the little info bubble opens the detail screen.
class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapBinding
    private lateinit var map: GoogleMap

    // only asked for when the user taps the my location button
    private val locationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                enableMyLocation()
            } else {
                Toast.makeText(this, R.string.location_denied, Toast.LENGTH_LONG).show()
            }
        }

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

        binding.myLocationButton.setOnClickListener { askForLocation() }

        // long press drops a new spot at that point, the form opens already filled in
        map.setOnMapLongClickListener { latLng ->
            startActivity(
                Intent(this, AddEditActivity::class.java)
                    .putExtra("lat", latLng.latitude)
                    .putExtra("lng", latLng.longitude)
            )
        }

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

    private fun askForLocation() {
        val granted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            enableMyLocation()
        } else {
            locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // shows the blue dot and jumps the camera to where the phone is
    private fun enableMyLocation() {
        // check again right before turning it on, the property throws without permission
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        try {
            map.isMyLocationEnabled = true
        } catch (_: SecurityException) {
            return
        }

        LocationServices.getFusedLocationProviderClient(this)
            .lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(location.latitude, location.longitude), 15f
                        )
                    )
                }
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
