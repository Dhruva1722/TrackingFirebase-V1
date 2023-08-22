package com.example.afinal.MapActivity

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
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

    val liveAddress = MutableLiveData<String>()


    fun isLocationEnabled(): Boolean {
        val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }


    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {

            val currentLocation = result.lastLocation
            if (currentLocation != null) {
                val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)

                // Get the current address using Geocoder
                fetchCurrentAddress(latLng)

                // Print location details to console
                Log.d(latLng.toString(), "lat , lon"+ "-------------"+ currentLocation.latitude +  "-----"+ currentLocation.longitude)

                val lastLocation = locations.lastOrNull()
                if (lastLocation != null) {
//                    val distanceToCurrent = SphericalUtil.computeDistanceBetween(lastLocation, latLng)
//
//                    // Add a distance threshold (e.g., 5 meters) to consider movement
//                    if (distanceToCurrent >= 5.0) {
//                        distance += distanceToCurrent.roundToInt()
//                        liveDistance.value = distance
//                    }
                }

                locations.add(latLng)
                liveLocations.value = locations
            }

        }
        }

    // Method to fetch the current address using Geocoder
    private fun fetchCurrentAddress(latLng: LatLng) {
        val geocoder = Geocoder(activity)
        val addressList: List<Address> =
            geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1) as List<Address>

        if (addressList.isNotEmpty()) {
            val address = addressList[0]
            val addressText = address.getAddressLine(0) // Get the first address line
            // Update the LiveData for the current address
            liveAddress.value = addressText
        } else {
            liveAddress.value = null
        }
    }


    fun getUserLocation() {
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




//                if (lastLocation != null) {
//                    val distanceToCurrent =
//                        SphericalUtil.computeDistanceBetween(lastLocation, latLng)
//                    distance += distanceToCurrent.roundToInt()
//                    liveDistance.value = distance
//                }
//
//                locations.add(latLng)
//                liveLocations.value = locations
//            }

//        client.lastLocation.addOnSuccessListener { location ->
//            val latLng = LatLng(location.latitude, location.longitude)
//            locations.add(latLng)
//            liveLocation.value = latLng
//        }