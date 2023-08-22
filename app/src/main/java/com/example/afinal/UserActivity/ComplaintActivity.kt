package com.example.afinal.UserActivity

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.afinal.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class ComplaintActivity : AppCompatActivity() {

    private lateinit var  edtMsgInput: TextInputEditText
    private lateinit var submitButton: Button
    private  lateinit var  complaintsRef : DatabaseReference




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complaint)

        edtMsgInput = findViewById(R.id.msg_input)
        submitButton = findViewById(R.id.submit_btn)


        // Initialize the Firebase Realtime Database reference
        complaintsRef = FirebaseDatabase.getInstance().getReference("users")

        submitButton.setOnClickListener {
            submitComplaint();
        }


    }

    private fun submitComplaint() {
        val complaintMessage = edtMsgInput.text.toString()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        if (!TextUtils.isEmpty(complaintMessage)) {
            // Create a Complaint object
            val complaint = Complaint(complaintMessage)

            // Save the complaint under the user's node using their authentication ID
            userId.takeIf { it.isNotEmpty() }?.let { uid ->
                val userComplaintsRef = FirebaseDatabase.getInstance().reference
                    .child("users")
                    .child(uid)
                    .child("complaints")

                userComplaintsRef.push().setValue(complaint)
            }

            // Clear the input field after submission
            edtMsgInput.text?.clear()

            // Display a success message to the user
            Toast.makeText(this, "Complaint submitted successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Please enter a complaint", Toast.LENGTH_SHORT).show()
        }
    }

}

data class Complaint(
    val message: String = "",
) {
    @RequiresApi(Build.VERSION_CODES.O)
    fun getFormattedTimestamp(): String {
        val timestamp = System.currentTimeMillis()
        val localDateTime =
            LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return localDateTime.format(formatter)
    }

}