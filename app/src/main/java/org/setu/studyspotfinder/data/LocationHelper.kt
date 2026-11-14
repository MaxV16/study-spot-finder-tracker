package org.setu.studyspotfinder.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

// small helper so the form and the map ask for a location the same way.
// we only want one fix when the user taps a button, so no listening for updates.
class LocationHelper(private val context: Context) {

    private val client = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(onResult: (Location?) -> Unit) {
        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location -> onResult(location) }
            .addOnFailureListener { onResult(null) }
    }
}
