    package com.example.afinal.MapActivity

    import android.Manifest
    import android.annotation.SuppressLint
    import android.content.Context
    import android.content.pm.PackageManager
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
    import android.location.LocationManager
    import android.os.Handler
    import androidx.core.app.ActivityCompat
    import com.google.firebase.database.DataSnapshot
    import com.google.firebase.database.DatabaseError
    import com.google.firebase.database.DatabaseReference
    import com.google.firebase.database.FirebaseDatabase
    import com.google.firebase.database.ValueEventListener


    class MapsActivity : AppCompatActivity(), OnMapReadyCallback  {

        private lateinit var map: GoogleMap
        private lateinit var binding: ActivityMapsBinding

        private lateinit var tv_lat: TextView
        private lateinit var tv_lon: TextView

        private val presenter = MapPresenter(this)

        private val locationProvider by lazy { LocationProvider(this) }

        private val myLocationListener = MylocationListener()
        private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("locations")

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
            requestLocationUpdates()
            // Start listening for location updates
//            locationProvider.startTracking()
            // Start listening for Firebase location updates
            locationProvider.locationsRef.addValueEventListener(valueEventListener)
        }

        private val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Fetch the latest location data from the snapshot
                    val latestLocation = snapshot.children.last()
                    val latitude = latestLocation.child("latitude").getValue(Double::class.java)
                    val longitude = latestLocation.child("longitude").getValue(Double::class.java)

                    // Update the TextViews with the latest location data
                    tv_lat.text = latitude?.toString() ?: "00.00000"
                    tv_lon.text = longitude?.toString() ?: "00.0000000"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if needed
            }
        }
        override fun onDestroy() {
            super.onDestroy()
            // Stop listening for location updates
            locationProvider.stopTracking()
            // Stop listening for Firebase location updates
            locationProvider.locationsRef.removeEventListener(valueEventListener)
        }


        private fun requestLocationUpdates() {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,

                5,
                10.0f,
                myLocationListener
            )
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

//                map.addMarker(
//                    MarkerOptions()
//                        .position(ui.currentLocation)
//                        .title("Current Location")
//                        .snippet("Lat: ${ui.currentLocation.latitude}, Lng: ${ui.currentLocation.longitude}")
//                )



            }

            binding.container.txtDistance.text = ui.formattedDistance
    //        binding.container.txtFuelConsumption.text = getString(
    //            R.string.fuel_consumption_label, ui.fuelConsumption)
            binding.container.txtPace.text = ui.formattedPace
            val color = Color.BLUE
            drawRoute(ui.userPath,color)
        }

       

        private fun drawRoute(locations: List<LatLng>, color: Int) {
            val polylineOptions = PolylineOptions()
                .addAll(locations)
                .color(color) // Set the desired color for the polyline

    //        map.clear()

            // Add markers for start and end points
            if (locations.isNotEmpty()) {
                val startMarker = MarkerOptions()
                    .position(locations.first())
                    .title("Start")
                map.addMarker(startMarker)

                val endMarker = MarkerOptions()
                    .position(locations.last())
                    .title("End")
                map.addMarker(endMarker)
            }

            map.addPolyline(polylineOptions)

        }

//        override fun onStepUpdated(steps: Int) {
//            // Update the user's location based on steps (approximation)
//            val currentLocation = presenter.ui.value?.currentLocation
//            if (currentLocation != null) {
//                // Calculate the new latitude and longitude based on the step count
//                // (This is just an example, you may need to adjust the calculation)
//                val latitudeIncrement =1000 * steps // Adjust this value as needed
//                val longitudeIncrement = 1000 * steps // Adjust this value as needed
//
//                // Update the current location with the new latitude and longitude
//                val newLatitude = currentLocation.latitude + latitudeIncrement
//                val newLongitude = currentLocation.longitude + longitudeIncrement
//                val newLocation = LatLng(newLatitude, newLongitude)
//
//                // Update the user's path with the new location
//                val updatedPath = presenter.ui.value?.userPath?.toMutableList()?.apply {
//                    add(newLocation)
//                } ?: listOf(newLocation)
//
//                // Update the UI with the new location and path
//                presenter.ui.value = presenter.ui.value?.copy(
//                    currentLocation = newLocation,
//                    userPath = updatedPath
//                )
//
//
//
//            }
        }

    private fun LocationManager.requestLocationUpdates(gpsProvider: String, i: Int, fl: Float, myLocationListener: MylocationListener) {

    }

    class MylocationListener {

    }
