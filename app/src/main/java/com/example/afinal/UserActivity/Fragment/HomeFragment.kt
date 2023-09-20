package com.example.afinal.UserActivity.Fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.afinal.MapActivity.MapsActivity
import com.example.afinal.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class HomeFragment : Fragment() {

        private lateinit var continuebtn : Button
    private lateinit var yourLocation : TextInputEditText
    private lateinit var destinationLocation : TextInputEditText


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)
        continuebtn = view.findViewById(R.id.continueBtn)

        yourLocation = view.findViewById(R.id.yourLocationID)
        destinationLocation = view.findViewById(R.id.destinationID)


        continuebtn.setOnClickListener {

            val yourLocation = yourLocation.text.toString()
            val destinationLocation = destinationLocation.text.toString()

            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val database = FirebaseDatabase.getInstance()

            if (userId != null) {
                val userRef = database.getReference("users").child(userId)

                // Create a unique key for the location data
                val locationKey = userRef.child("location_points").push().key

                if (locationKey != null) {
                    val locationData = hashMapOf(
                        "yourLocation" to yourLocation,
                        "destinationLocation" to destinationLocation
                    )
                    // Set the location data under the unique key
                    userRef.child("location_points").child(locationKey).setValue(locationData)
                        .addOnSuccessListener {
                            val intent = Intent(context, MapsActivity::class.java)
                            startActivity(intent)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error adding data: $e", Toast.LENGTH_SHORT)
                                .show()
                        }
                }
            }
        }
            return view
        }

}