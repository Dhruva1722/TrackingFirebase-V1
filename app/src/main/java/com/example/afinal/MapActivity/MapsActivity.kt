    package com.example.afinal.MapActivity

    import android.annotation.SuppressLint
    import android.content.Context
    import android.content.Intent
    import android.graphics.Color
    import android.graphics.PorterDuff
    import android.hardware.Sensor
    import android.hardware.SensorEvent
    import android.hardware.SensorEventListener
    import android.hardware.SensorManager
    import android.location.Address
    import android.location.Geocoder
    import android.os.Bundle
    import android.os.SystemClock
    import android.view.MenuItem
    import android.view.View
    import android.widget.Button
    import android.widget.EditText
    import android.widget.ImageView
    import android.widget.PopupMenu
    import android.widget.TextView
    import android.widget.Toast
    import androidx.appcompat.app.ActionBarDrawerToggle
    import androidx.appcompat.app.AppCompatActivity
    import androidx.appcompat.widget.Toolbar
    import androidx.core.view.GravityCompat
    import androidx.drawerlayout.widget.DrawerLayout
    import com.example.afinal.R
    import com.example.afinal.UserActivity.ComplaintActivity
    import com.example.afinal.UserActivity.Fragment.AttendanceFragment
    import com.example.afinal.UserActivity.Fragment.HomeFragment
    import com.example.afinal.UserActivity.Fragment.AccountFragment
    import com.example.afinal.UserActivity.HelpActivity
    import com.example.afinal.UserActivity.UserDetails
    import com.example.afinal.databinding.ActivityMapsBinding
    import com.example.afinal.services.BackgroundService
    import com.example.afinal.services.isServiceRunning
    import com.google.android.gms.maps.CameraUpdateFactory
    import com.google.android.gms.maps.GoogleMap
    import com.google.android.gms.maps.OnMapReadyCallback
    import com.google.android.gms.maps.SupportMapFragment
    import com.google.android.gms.maps.model.LatLng
    import com.google.android.gms.maps.model.MarkerOptions
    import com.google.android.gms.maps.model.PolylineOptions
    import com.google.android.material.navigation.NavigationView
    import java.io.IOException
    import kotlin.math.log2
    import kotlin.math.sqrt


    class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

        private lateinit var map: GoogleMap
        private lateinit var binding: ActivityMapsBinding

        private lateinit var exitbtn: Button
        private lateinit var helpBtn: ImageView
        private lateinit var userCurrentAddress: TextView
        private val presenter = MapPresenter(this)
        private val locationProvider by lazy { LocationProvider(this) }


        private lateinit var sensorManager: SensorManager
        private var accelerometer: Sensor? = null
        private val stableThreshold = 0.10 // Adjust this threshold as needed
        private val stableDurationThresholdMillis = 60000L // Adjust this duration as needed (1 minute in milliseconds)
        private var lastUpdateTimeMillis = 0L
        private var isDeviceStable = false

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            if (!isServiceRunning(this, BackgroundService::class.java)) {
                val serviceIntent = Intent(this, BackgroundService::class.java)
                startService(serviceIntent)
            }

            binding = ActivityMapsBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Find and initialize the TextView widgets

            exitbtn = findViewById(R.id.ExitBtn)

            // Find and initialize the TextView widgets
            userCurrentAddress = findViewById(R.id.userCurrentAddress)

            exitbtn.setOnClickListener {
                val intent = Intent(this, UserDetails::class.java)
                startActivity(intent)
                overridePendingTransition(
                    R.anim.slide_down,
                    R.anim.slide_down
                );
            }

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)


            binding.btnStartStop.setOnClickListener {
                if (binding.btnStartStop.text == getString(R.string.start_label)) {
                    startTracking()
                    binding.btnStartStop.setText(com.example.afinal.R.string.stop_label)
                } else {
                    stopTracking()
                    binding.btnStartStop.setText(com.example.afinal.R.string.start_label)
                }
            }

            presenter.onViewCreated()

            // Start listening for location updates
            // locationProvider.startTracking()
            // Start listening for Firebase location updates

            helpBtn = findViewById(R.id.helpBtn)

            helpBtn.setOnClickListener { v ->
                showPopupMenu(v)
            }

            registerAccelerometerSensor()

            unregisterAccelerometerSensor()

        }

        private fun showPopupMenu(view: View) {
            val popupMenu = PopupMenu(this, view)
            popupMenu.inflate(R.menu.help_menu) // Inflate the menu resource

            // Set a listener for menu item clicks
            popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
                when (menuItem.itemId) {
                    R.id.action_help -> {
                        // Handle Help action
                        val intent = Intent(this, HelpActivity::class.java)
                        startActivity(intent)
                        true
                    }

                    R.id.action_complain -> {
                        // Handle Feedback action
                        val intent = Intent(this, ComplaintActivity::class.java)
                        startActivity(intent)
                        true
                    }

                    else -> false
                }
            }
            // Show the popup menu
            popupMenu.show()
        }

        fun searchLocation(view: View) {
            val locationSearch = findViewById<EditText>(R.id.edt_search)
            val location = locationSearch.text.toString()
            var addressList: List<Address>? = null

            if (location.isNotEmpty()) {
                val geocoder = Geocoder(this)
                try {
                    addressList = geocoder.getFromLocationName(location, 1)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                if (addressList != null && addressList.isNotEmpty()) {
                    val address = addressList[0]
                    val latLng = LatLng(address.latitude, address.longitude)
                    map.addMarker(MarkerOptions().position(latLng).title(location))
                    map.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                    Toast.makeText(
                        applicationContext,
                        "${address.latitude} ${address.longitude}",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(applicationContext, "Location not found", Toast.LENGTH_SHORT)
                        .show()
                }
            }
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

            // Use your LocationProvider's getUserLocation function
            locationProvider.getUserLocation()

            // Now, you can observe liveLocation to get user's location
            locationProvider.liveLocation.observe(this, { userLocation ->
                val accuracy = 0.0f
                val zoomLevel = calculateZoomLevel(accuracy) // Calculate your zoom level
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, zoomLevel))
            })

        }

        private fun calculateZoomLevel(accuracy: Float): Float {
            // Example calculation - you can adjust this based on your requirements
            val zoomLevel = 15.0f - log2(accuracy) // You may need to import kotlin.math.log2

            return if (zoomLevel < 1) 1.0f else zoomLevel
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

                // Get the address for the current location
                val geocoder = Geocoder(this)
                val addresses = geocoder.getFromLocation(
                    ui.currentLocation.latitude,
                    ui.currentLocation.longitude,
                    1
                )
                val address = addresses?.firstOrNull()

                // Set the address in the TextView
                userCurrentAddress.text = address?.getAddressLine(0) ?: "Unknown"
                val markerSnippet = "Address: ${address?.getAddressLine(0) ?: "Unknown"}"
                // Add marker at the current location
                map.clear()


                map.addMarker(
                    MarkerOptions()
                        .position(ui.currentLocation)
                        .title(markerSnippet)
                        .snippet("Lat: ${ui.currentLocation.latitude}, Lng: ${ui.currentLocation.longitude}")
                )
            }

            binding.container.txtDistance.text = ui.formattedDistance
            //        binding.container.txtFuelConsumption.text = getString(
            //            R.string.fuel_consumption_label, ui.fuelConsumption)
            binding.container.txtPace.text = ui.formattedPace
            val color = Color.BLUE
            drawRoute(ui.userPath, color)

        }

        private fun drawRoute(locations: List<LatLng>, color: Int) {
            val polylineOptions = PolylineOptions()
                .addAll(locations)
                .color(color)

            // map.clear()

            // Add markers for start and end points
            if (locations.isNotEmpty()) {
                val endMarker = MarkerOptions()
                    .position(locations.last())
                    .title("End")
                map.addMarker(endMarker)
            }

            map.addPolyline(polylineOptions)

        }

        private fun registerAccelerometerSensor() {

            val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            sensorManager.registerListener(accelerometerListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        }
        private fun unregisterAccelerometerSensor() {

            val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            sensorManager.unregisterListener(accelerometerListener)
        }

        override fun onResume() {
            super.onResume()
            // Register the accelerometer sensor when the activity is in the foreground
            accelerometer?.let { sensor ->
                sensorManager.registerListener(
                    accelerometerListener,
                    sensor,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            }
        }

        override fun onPause() {
            super.onPause()
            // Unregister the accelerometer sensor when the activity is in the background
            accelerometer?.let { sensorManager.unregisterListener(accelerometerListener, it) }
        }

        private val accelerometerListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val currentTimeMillis = System.currentTimeMillis()

                if (currentTimeMillis - lastUpdateTimeMillis > stableDurationThresholdMillis) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]

                    // Calculate the acceleration magnitude
                    val acceleration = sqrt(x * x + y * y + z * z)

                    if (acceleration < stableThreshold) {
                        if (!isDeviceStable) {
                            // Device has become stable
                            isDeviceStable = true
                            showStabilityMessage()
                        }
                    } else {
                        // Device is not stable
                        isDeviceStable = false
                    }

                    lastUpdateTimeMillis = currentTimeMillis
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Handle accuracy change if needed
            }
        }
        private fun showStabilityMessage() {
            // Display a message when the device is stable for the defined duration
            Toast.makeText(this, "Device is stable", Toast.LENGTH_SHORT).show()
        }


//        override fun onStepUpdated(steps: Int) {
//            val currentLocation = presenter.ui.value?.currentLocation
//            if (currentLocation != null) {
//                val latitudeFactor = 0.00001 // Adjust this factor as needed
//                val longitudeFactor = 0.00001 // Adjust this factor as needed
//
//                // Calculate the new latitude and longitude based on the factor and steps
//                val newLatitude = currentLocation.latitude + latitudeFactor * steps
//                val newLongitude = currentLocation.longitude + longitudeFactor * steps
//                val newLocation = LatLng(newLatitude, newLongitude)
//
//                // Update the user's path with the new location
//                val updatedPath: MutableList<LatLng> =
//                    presenter.ui.value?.userPath?.toMutableList()?.apply {
//                        add(newLocation)
//                    } ?: mutableListOf(newLocation)
//
//                // Update the UI with the new location and path
//                presenter.ui = presenter.ui.value?.copy(
//                    currentLocation = newLocation,
//                    userPath = updatedPath
//                )
//
//
//            }
//
//        }

    }

