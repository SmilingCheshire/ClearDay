package com.example.clearday.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.location.*

/**
 * Helper class to manage real-time location tracking using FusedLocationProviderClient.
 */
class LocationTracker(private val context: Context) {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private var callback: LocationCallback? = null

    /**
     * Starts requesting periodic location updates.
     * @param onLocationUpdate Callback function triggered when a new [Location] is available.
     */
    @SuppressLint("MissingPermission")
    fun startTracking(onLocationUpdate: (Location) -> Unit) {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10_000L
        )
            .setMinUpdateIntervalMillis(5_000L)
            .build()

        callback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val location = locationResult.lastLocation
                if (location != null) {
                    Log.d("LocationTracker", "Lat: ${location.latitude}, Lng: ${location.longitude}")
                    onLocationUpdate(location)
                }
            }
        }

        fusedClient.requestLocationUpdates(
            request,
            callback!!,
            context.mainLooper
        )
    }

    /**
     * Stops location updates and releases the callback to prevent memory leaks.
     */
    fun stopTracking() {
        callback?.let {
            fusedClient.removeLocationUpdates(it)
        }
        callback = null
    }
}