package com.example.afinal.UserActivity.Fragment

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.afinal.R
import com.example.afinal.UserActivity.User
import com.example.afinal.services.BackgroundService
import com.example.afinal.services.isServiceRunning
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class AttendanceFragment : Fragment() {


    private lateinit var dateTimeTextView: TextView
    private lateinit var daymonthTextView: TextView
    private lateinit var username: TextView
    private lateinit var userStatusTime: TextView
    private lateinit var onlineOfflineBtn: ImageView
    private lateinit var presentBtn: LinearLayout
    private lateinit var absentBtn: LinearLayout

    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase


    private lateinit var sharedPreferences: SharedPreferences

    private var isPresent = false
    private var attendanceTime: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_attendance, container, false)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())

        if (!isServiceRunning(requireContext(), BackgroundService::class.java)) {
            val serviceIntent = Intent(requireContext(), BackgroundService::class.java)
            requireContext().startService(serviceIntent)
        }


        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Initialize your views
        dateTimeTextView = view.findViewById(R.id.dateTime)
        daymonthTextView = view.findViewById(R.id.dayMonth)
        userStatusTime = view.findViewById(R.id.userTimeOfAttendence)
        onlineOfflineBtn = view.findViewById(R.id.onlineOfflineBtn)
        presentBtn = view.findViewById(R.id.presentBtn)
        absentBtn = view.findViewById(R.id.absentBtn)
        username = view.findViewById(R.id.Username)


        fetchAndDisplayUsername()


        val currentDateTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        dateTimeTextView.text = currentDateTime


        val currentDayMonth = SimpleDateFormat("EEEE d,MMM", Locale.getDefault()).format(Date())
        daymonthTextView.text = currentDayMonth

        presentBtn.setOnClickListener {
            setAttendance(true)
        }

        absentBtn.setOnClickListener {
            setAttendance(false)
        }

        if (savedInstanceState != null) {
            isPresent = savedInstanceState.getBoolean("isPresent")
            attendanceTime = savedInstanceState.getString("attendanceTime")
            updateUI()
        }

        return view
    }
    private fun fetchAndDisplayUsername() {
        val userId = mAuth.currentUser?.uid
        if (userId != null) {
            val userRef = database.getReference("users").child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = snapshot.child("name").getValue(String::class.java)
                    if (username != null) {
                        val usernameTextView = view?.findViewById<TextView>(R.id.Username)
                        usernameTextView?.text = "Hello $username!"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle the error if needed
                    Toast.makeText(context, "Getting error fetching username", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Save the state
        outState.putBoolean("isPresent", isPresent)
        outState.putString("attendanceTime", attendanceTime)
    }

    override fun onResume() {
        super.onResume()

        // Check attendance state from SharedPreferences
        isPresent = sharedPreferences.getBoolean("isPresent", false)
        attendanceTime = sharedPreferences.getString("attendanceTime", null)

        if (isPresent) {
            onlineOfflineBtn.setImageResource(R.drawable.onlinebtn)
        } else {
            onlineOfflineBtn.setImageResource(R.drawable.offlinebtn)
        }

        updateUI()
    }

    private fun setAttendance(present: Boolean) {
        val currentTime = SimpleDateFormat("HH:mm:ss").format(Date())
        val currentHour = SimpleDateFormat("HH").format(Date()).toInt()
        val userId = mAuth.currentUser?.uid

        if (userId != null) {
            val database = FirebaseDatabase.getInstance()
            val userRef = database.getReference("users").child(userId)

            if (currentHour >= 10 && currentHour < 19) {
                val currentDate = SimpleDateFormat("yyyy-MM-dd").format(Date())
                val attendanceRecord = if (present) {
                    "Present at $currentTime"
                } else {
                    "Absent at $currentTime"
                }

                userRef.child("attendance").child(currentDate)
                    .runTransaction(object : Transaction.Handler {
                        override fun doTransaction(currentData: MutableData): Transaction.Result {
                            val attendanceData = currentData.getValue(String::class.java)
                            if (attendanceData == null) {
                                // No previous attendance data, set the new value
                                currentData.value = attendanceRecord
                            } else {
                                // Attendance data exists, append the new record
                                currentData.value = "$attendanceData\n$attendanceRecord"
                            }
                            return Transaction.success(currentData)
                        }

                        override fun onComplete(
                            databaseError: DatabaseError?,
                            committed: Boolean,
                            dataSnapshot: DataSnapshot?
                        ) {
                            if (committed) {
                                onlineOfflineBtn.setImageResource(if (present) R.drawable.onlinebtn else R.drawable.offlinebtn)
                                isPresent = present
                                attendanceTime = currentTime
                                updateUI()
                                // Save attendance state to SharedPreferences
                                val editor = sharedPreferences.edit()
                                editor.putBoolean("isPresent", isPresent)
                                editor.putString("attendanceTime", attendanceTime)
                                editor.apply()
                            } else {
                                // Handle the error if the transaction was not committed
                                // For example, when there's concurrent modification of data
                                userStatusTime.text = "Attendance update failed."
                            }
                        }
                    })
            } else {
                // Outside working hours
                userStatusTime.text = "You are outside working hours."
            }


            // Check for overtime
            if (currentHour >= 19 && isPresent) {
                val overTime = currentHour - 19
                userStatusTime.text = "You have $overTime hours of overtime."
            }
        }

        val editor = sharedPreferences.edit()
        editor.putBoolean("isPresent", isPresent)
        editor.putString("attendanceTime", attendanceTime)
        editor.apply()
    }
    private fun updateUI() {
        val statusText = if (isPresent) "Present" else "Absent"
        val message = if (attendanceTime != null) {
            "You marked $statusText at $attendanceTime."
        } else {
            "You are $statusText."
        }

        userStatusTime.text = message
    }


}


//private fun setAttendance(present: Boolean) {
//    val currentTime = SimpleDateFormat("HH:mm:ss").format(Date())
//    val currentHour = SimpleDateFormat("HH").format(Date()).toInt()
//    val userId = mAuth.currentUser?.uid
//    if (userId != null) {
//        val database = FirebaseDatabase.getInstance()
//        val userRef = database.getReference("users").child(userId)
//
//        if (currentHour >= 10 && currentHour < 19) {
//            val attendanceRecord = if (present) {
//                "Present at $currentTime"
//            } else {
//                "Absent at $currentTime"
//            }
//
//            val currentDate = SimpleDateFormat("yyyy-MM-dd").format(Date())
//
//            val newAttendanceRef = userRef.child("attendance").child(currentDate).push()
//            newAttendanceRef.setValue(attendanceRecord)
//
//            onlineOfflineBtn.setImageResource(if (present) R.drawable.onlinebtn else R.drawable.offlinebtn)
//            isPresent = present
//            attendanceTime = currentTime
//            updateUI()
//        } else {
//            // Outside working hours
//            userStatusTime.text = "You are outside working hours."
//        }