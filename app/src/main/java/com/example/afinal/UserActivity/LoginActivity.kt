package com.example.afinal.UserActivity

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.afinal.MapActivity.MapsActivity
import com.example.afinal.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : AppCompatActivity() {


    private lateinit var newUserTextView: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        auth = FirebaseAuth.getInstance()


        val emailOrIdField = findViewById<TextInputEditText>(R.id.loginEmailID)
        val passwordField = findViewById<TextInputEditText>(R.id.loginPasswordID)
        val loginButton = findViewById<Button>(R.id.loginBtnID)


        newUserTextView = findViewById(R.id.newUserID)
        newUserTextView.setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        }

        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE) // Initialize it here

        loginButton.setOnClickListener {
            val userInput = emailOrIdField.text.toString().trim()
            val password = passwordField.text.toString()

            if (userInput.isNotEmpty() && password.isNotEmpty()) {
                loginUser(userInput, password)
            } else {
                Toast.makeText(this, "Please enter both user input and password.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser(userInput: String, password: String) {
        if (isValidEmail(userInput)) {
            // Authenticate using email and password
            auth.signInWithEmailAndPassword(userInput, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Successful login, redirect to MapsActivity
                        val intent = Intent(this, MapsActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // Failed login, show an error message
                        Toast.makeText(this, "Invalid email or password.", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            // Authenticate using employee ID and password
            employeeIdExistsInDatabase(userInput) { exists ->
                if (exists) {
                    // Check if employee ID matches password
                    if (employeePasswordMatches(userInput, password)) {
                        // Successful login, redirect to MapsActivity
                        val intent = Intent(this, MapsActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // Failed login, show an error message
                        Toast.makeText(this, "Invalid employee ID or password.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Employee ID doesn't exist in the database, show an error message
                    Toast.makeText(this, "User not authenticated. Please register or check your credentials.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Implement your own logic for these functions
    private fun isValidEmail(email: String): Boolean {
        // Your email validation logic here
        return true
    }

    private fun employeeIdExistsInDatabase(employeeId: String, callback: (Boolean) -> Unit) {
        // Your logic to check if the employee ID exists in the database here
        // Call callback(true) if exists, callback(false) otherwise
    }

    private fun employeePasswordMatches(employeeId: String, password: String): Boolean {
        // Your logic to check if the employee ID matches the provided password here
        return true
    }
}