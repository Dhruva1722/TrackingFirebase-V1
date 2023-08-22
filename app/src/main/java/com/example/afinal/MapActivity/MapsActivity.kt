    package com.example.afinal.MapActivity

    import android.annotation.SuppressLint
    import android.content.Intent
    import android.graphics.Color
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
    import androidx.appcompat.app.AppCompatActivity
    import com.example.afinal.R
    import com.example.afinal.UserActivity.ComplaintActivity
    import com.example.afinal.UserActivity.HelpActivity
    import com.example.afinal.UserActivity.UserDetails
    import com.example.afinal.databinding.ActivityMapsBinding
    import com.google.android.gms.maps.CameraUpdateFactory
    import com.google.android.gms.maps.GoogleMap
    import com.google.android.gms.maps.OnMapReadyCallback
    import com.google.android.gms.maps.SupportMapFragment
    import com.google.android.gms.maps.model.LatLng
    import com.google.android.gms.maps.model.MarkerOptions
    import com.google.android.gms.maps.model.PolylineOptions
    import java.io.IOException


    class MapsActivity : AppCompatActivity(), OnMapReadyCallback  {

        private lateinit var map: GoogleMap
        private lateinit var binding: ActivityMapsBinding

        private lateinit var exitbtn: Button
        private lateinit var helpBtn: ImageView
        private lateinit var userCurrentAddress: TextView

        private val presenter = MapPresenter(this)

        private val locationProvider by lazy { LocationProvider(this) }


        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            binding = ActivityMapsBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Find and initialize the TextView widgets

            exitbtn = findViewById(R.id.ExitBtn)

            // Find and initialize the TextView widgets
            userCurrentAddress = findViewById(R.id.userCurrentAddress)

            exitbtn.setOnClickListener {
                val intent = Intent(this, UserDetails::class.java)
                startActivity(intent)
            }





            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)




            binding.btnStartStop.setOnClickListener {
                if (binding.btnStartStop.text == getString(com.example.afinal.R.string.start_label)) {
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
            val locationSearch = findViewById<EditText>(com.example.afinal.R.id.edt_search)
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
                    Toast.makeText(applicationContext, "Location not found", Toast.LENGTH_SHORT).show()
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
                val addresses = geocoder.getFromLocation(ui.currentLocation.latitude, ui.currentLocation.longitude, 1)
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
            drawRoute(ui.userPath,color)

        }

        private fun drawRoute(locations: List<LatLng>, color: Int) {
            val polylineOptions = PolylineOptions()
                .addAll(locations)
                .color(color) // Set the desired color for the polyline

    //        map.clear()

            // Add markers for start and end points
            if (locations.isNotEmpty()) {
                val endMarker = MarkerOptions()
                    .position(locations.last())
                    .title("End")
                map.addMarker(endMarker)
            }

            map.addPolyline(polylineOptions)

        }

    }


