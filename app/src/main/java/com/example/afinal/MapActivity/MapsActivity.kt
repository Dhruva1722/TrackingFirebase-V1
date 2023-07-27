package com.example.afinal.MapActivity

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.afinal.R
import com.example.afinal.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions


class MapsActivity : AppCompatActivity(), OnMapReadyCallback , StepCounter.StepUpdater {

    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var tv_lat: TextView
    private lateinit var tv_lon: TextView

    private val presenter = MapPresenter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Find and initialize the TextView widgets
        tv_lat = findViewById<TextView>(R.id.tv_lat)
        tv_lon = findViewById<TextView>(R.id.tv_lon)


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnStartStop.setOnClickListener {
            if (binding.btnStartStop.text == getString(R.string.start_label)) {
                startTracking()
                binding.btnStartStop.setText(R.string.stop_label)
            } else {
                stopTracking()
                binding.btnStartStop.setText(R.string.start_label)
            }
        }

        presenter.onViewCreated()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        presenter.ui.observe(this) { ui ->
            updateUi(ui)
        }

        presenter.onMapLoaded()
        map.uiSettings.isZoomControlsEnabled = true
    }

    private fun startTracking() {
        binding.container.txtPace.text = ""
        binding.container.txtDistance.text = ""
        binding.container.txtTime.base = SystemClock.elapsedRealtime()
        binding.container.txtTime.start()
        map.clear()

        presenter.startTracking()
    }

    private fun stopTracking() {
        presenter.stopTracking()
        binding.container.txtTime.stop()
    }

    @SuppressLint("MissingPermission")
    private fun updateUi(ui: Ui) {
        if (ui.currentLocation != null && ui.currentLocation != map.cameraPosition.target) {
            map.isMyLocationEnabled = true
//            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(ui.currentLocation, 17f))

            // Add marker at the current location
            map.clear()

            map.addMarker(
                MarkerOptions()
                    .position(ui.currentLocation)
                    .title("Current Location")
                    .snippet("Lat: ${ui.currentLocation.latitude}, Lng: ${ui.currentLocation.longitude}")
            )

            // Show the latitude and longitude in the TextViews
            tv_lat.text = " ${ui.currentLocation.latitude}"
            tv_lon.text = "${ui.currentLocation.longitude}"
        }

        binding.container.txtDistance.text = ui.formattedDistance
        binding.container.txtPace.text = ui.formattedPace
        val color = Color.BLUE
        drawRoute(ui.userPath,color)
    }

//    private fun drawRoute(locations: List<LatLng>) {
//        val polylineOptions = PolylineOptions()
//
////        map.clear()
//
//        val points = polylineOptions.points
//        points.addAll(locations)
//
//        map.addPolyline(polylineOptions)
//    }

    private fun drawRoute(locations: List<LatLng>, color: Int) {
        val polylineOptions = PolylineOptions()
            .addAll(locations)
            .color(color) // Set the desired color for the polyline

//        map.clear()

        map.addPolyline(polylineOptions)

    }

    override fun onStepUpdated(steps: Int) {
        // Update the user's location based on steps (approximation)
        val currentLocation = presenter.ui.value?.currentLocation
        if (currentLocation != null) {
            // Calculate the new latitude and longitude based on the step count
            // (This is just an example, you may need to adjust the calculation)
            val latitudeIncrement =1000 * steps // Adjust this value as needed
            val longitudeIncrement = 1000 * steps // Adjust this value as needed

            // Update the current location with the new latitude and longitude
            val newLatitude = currentLocation.latitude + latitudeIncrement
            val newLongitude = currentLocation.longitude + longitudeIncrement
            val newLocation = LatLng(newLatitude, newLongitude)

            // Update the user's path with the new location
            val updatedPath = presenter.ui.value?.userPath?.toMutableList()?.apply {
                add(newLocation)
            } ?: listOf(newLocation)

            // Update the UI with the new location and path
            presenter.ui.value = presenter.ui.value?.copy(
                currentLocation = newLocation,
                userPath = updatedPath
            )



        }
    }
}