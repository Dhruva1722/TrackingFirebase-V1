package com.example.afinal.MapActivity

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.example.afinal.R

class MapPresenter(private val activity: AppCompatActivity) {

    private val locationProvider = LocationProvider(activity)

    private val stepCounter = StepCounter(activity)


    private val permissionsManager = PermissionsManager(activity, locationProvider, stepCounter)

    // Other methods and logic in the MapPresenter class

    val ui = MutableLiveData(Ui.EMPTY)

    fun onViewCreated() {
//        TODO("Not yet implemented")
        locationProvider.liveLocations.observe(activity) { locations ->
            val current = ui.value
            ui.postValue(current?.copy(userPath = locations))
        }

        locationProvider.liveLocation.observe(activity) { currentLocation ->
            val current = ui.value
            ui.postValue(current?.copy(currentLocation = currentLocation))
        }

        locationProvider.liveDistance.observe(activity) { distance ->
            val current = ui.value
            val formattedDistance = activity.getString(R.string.distance_value, distance)
            ui.postValue(current?.copy(formattedDistance = formattedDistance))
        }
        permissionsManager.requestActivityRecognition()
    }

    fun onMapLoaded() {
//        TODO("Not yet implemented")
        permissionsManager.requestUserLocation()
    }

    fun startTracking() {
//        TODO("Not yet implemented")
        locationProvider.trackUser()
    }

    fun stopTracking() {
        locationProvider.stopTracking()
        stepCounter.unloadStepCounter()
    }

}
