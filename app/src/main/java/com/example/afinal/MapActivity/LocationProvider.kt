package com.example.afinal.MapActivity

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.location.LocationManagerCompat
import androidx.core.location.LocationManagerCompat.requestLocationUpdates
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.maps.android.SphericalUtil
import kotlin.math.roundToInt


@SuppressLint("MissingPermission")
class LocationProvider(private val activity: AppCompatActivity) {

    private val client by lazy { LocationServices.getFusedLocationProviderClient(activity) }

    private val locations = mutableListOf<LatLng>()
    private var distance = 0

    val liveLocations = MutableLiveData<List<LatLng>>()
    val liveDistance = MutableLiveData<Int>()
    val liveLocation = MutableLiveData<LatLng>()

    private val database = FirebaseDatabase.getInstance()
    internal val locationsRef = database.reference.child("locations")


    fun isLocationEnabled(): Boolean {
        val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {

            val currentLocation = result.lastLocation
            if (currentLocation != null) {
                val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
                val timestampMillis = currentLocation.time
                // Push the location to Firebase
                val locationData = mapOf(
                    "latitude" to currentLocation.latitude,
                    "longitude" to currentLocation.longitude,
                    "timestamp" to currentLocation.time
                )
                val timestamp = currentLocation.time.toString()
                locationsRef.child(timestamp).setValue(locationData)


                // Print location details to console
                Log.d(latLng.toString(), "lat , lon"+ "---"+ currentLocation.latitude +  "---"+ currentLocation.longitude)

                val lastLocation = locations.lastOrNull()

                if (lastLocation != null) {
                    val distanceToCurrent =
                        SphericalUtil.computeDistanceBetween(lastLocation, latLng)
                    distance += distanceToCurrent.roundToInt()
                    liveDistance.value = distance
                }

                locations.add(latLng)
                liveLocations.value = locations
            }
        }
        }
//            val currentLocation = result.lastLocation
//            val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
//
//            val lastLocation = locations.lastOrNull()
//
//            if (lastLocation != null) {
//                val distanceToCurrent = SphericalUtil.computeDistanceBetween(lastLocation, latLng)
//                distance += distanceToCurrent.roundToInt()
//                liveDistance.value = distance
//            }
//
//            locations.add(latLng)
//            liveLocations.value = locations
//
//        }


    fun getUserLocation() {
//        client.lastLocation.addOnSuccessListener { location ->
//            val latLng = LatLng(location.latitude, location.longitude)
//            locations.add(latLng)
//            liveLocation.value = latLng
//        }
        client.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val latLng = LatLng(location.latitude, location.longitude)
                locations.add(latLng)
                liveLocation.value = latLng
            } else {
                // Location is null, request location updates explicitly
                trackUser()
            }
        }
    }


    fun trackUser() {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        client.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    fun stopTracking() {
        client.removeLocationUpdates(locationCallback)
        locations.clear()
        distance = 0
    }
}

data class LocationData(val latitude: Double, val longitude: Double, val timestamp: Long)