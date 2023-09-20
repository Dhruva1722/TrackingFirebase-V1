package com.example.afinal.MapActivity

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.example.afinal.R

class MapPresenter(private val activity: AppCompatActivity) {

    private val locationProvider = LocationProvider(activity )

    private val stepCounter = StepCounter(activity)

    val fuelEfficiencyKmpl = 55

    private val permissionsManager = PermissionsManager(activity, locationProvider, stepCounter)

    // Other methods and logic in the MapPresenter class

    val ui = MutableLiveData(Ui.EMPTY)

    fun onViewCreated() {
//        TODO("Not yet implemented")

        stepCounter.liveSteps.observe(activity) { steps ->
            val current = ui.value
            ui.value = current?.copy(formattedPace = "$steps")
        }

        locationProvider.liveLocations.observe(activity) { locations ->
            val current = ui.value
            ui.postValue(current?.copy(userPath = locations))
        }

        locationProvider.liveLocation.observe(activity) { currentLocation ->
            val current = ui.value
            ui.postValue(current?.copy(currentLocation = currentLocation))
        }

//        locationProvider.liveDistance.observe(activity) { distance ->
//            val current = ui.value
//            val formattedDistance = activity.getString(R.string.distance_value, distance / 1000.0)
//            ui.value = current?.copy(formattedDistance = formattedDistance)
//
////            val formattedDistance = activity.getString(R.string.distance_value, distance)
////            ui.value = current?.copy(formattedDistance = formattedDistance)
//        }

        locationProvider.liveDistance.observe(activity) { distance ->
            val current = ui.value
            val formattedDistance = activity.getString(R.string.distance_value, distance / 1000.0)

            val fuelConsumptionLiters = (distance.toDouble() / 1000.0) / fuelEfficiencyKmpl
            val formattedFuelConsumption = activity.getString(
                R.string.fuel_consumption_value,
                fuelConsumptionLiters
            )

            ui.value = current?.copy(
                formattedDistance = formattedDistance,
                formattedFuelConsumption = formattedFuelConsumption
            )
        }
        permissionsManager.requestActivityRecognition()

    }

    fun onMapLoaded() {
//        TODO("Not yet implemented")
        permissionsManager.requestUserLocation()
    }

    fun startTracking() {
//        TODO("Not yet implemented")
//        locationProvider.trackUser()
        if (locationProvider.isLocationEnabled()) {
            permissionsManager.requestActivityRecognition()
            locationProvider.trackUser()

            val currentUi = ui.value

            ui.value = currentUi?.copy(
                formattedPace = Ui.EMPTY.formattedPace,
                formattedDistance = Ui.EMPTY.formattedDistance,

            )


        } else {
            // Show a dialog or toast message to prompt the user to turn on location services
            // For example, you can use a Toast message to inform the user
            Toast.makeText(activity, "Please turn on location services", Toast.LENGTH_SHORT).show()
        }
    }



    fun stopTracking() {
        locationProvider.stopTracking()
        stepCounter.unloadStepCounter()
    }

}



