package com.example.clearday.location


import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.location.*

class LocationTracker(private val context: Context) {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private var callback: LocationCallback? = null

    @SuppressLint("MissingPermission") // we will check permission before calling
    fun startTracking(onLocationUpdate: (Location) -> Unit) {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10_000L // 10 seconds; adjust as needed
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

    fun stopTracking() {
        callback?.let {
            fusedClient.removeLocationUpdates(it)
        }
        callback = null
    }
}