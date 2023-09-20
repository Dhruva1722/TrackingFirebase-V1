package com.example.afinal.UserActivity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.afinal.MainActivity
import com.example.afinal.MapActivity.MapsActivity
import com.example.afinal.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegistrationActivity : AppCompatActivity() {

//    private lateinit var viewPager: ViewPager
//    private lateinit var progressBar: ProgressBar
//    private lateinit var nextButton: ImageView
//    private lateinit var previousButton: ImageView

    private lateinit var haveAnAccount: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_refistration)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("users")
        haveAnAccount = findViewById(R.id.haveAnAccountTxt)


        val registerButton = findViewById<Button>(R.id.registerBtn)
        registerButton.setOnClickListener {
            val employeeId = findViewById<TextInputEditText>(R.id.registerEmployeID).text.toString()
            val name = findViewById<TextInputEditText>(R.id.registerNameID).text.toString()
            val email = findViewById<TextInputEditText>(R.id.registerEmailID).text.toString()
            val password = findViewById<TextInputEditText>(R.id.registerPasswordID).text.toString()

            // Register user with Firebase Authentication
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        user?.let {
                            // Store additional user details in Firebase Realtime Database
                            val userId = user.uid
                            val userDetails = User(employeeId, name, email)
                            database.child(userId).setValue(userDetails)

                            // Navigate to the next screen or perform any other action
                            // For example, navigate to MapsActivity
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            overridePendingTransition(R.anim.slide_down,
                                R.anim.slide_left_out);
                            finish()
                        }
                    } else {
                        // Log the error
                        task.exception?.printStackTrace()

                        // Display a toast message with the error details
                        Toast.makeText(
                            this,
                            "Registration failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            haveAnAccount.setOnClickListener {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }

        }

    }

}
data class User(
    val employeeId: String,
    val name: String,
    val email: String
)




//        viewPager = findViewById(R.id.viewPager)
//        progressBar = findViewById(R.id.progressBar)
//        nextButton = findViewById(R.id.nextButton)
//        previousButton = findViewById(R.id.previousButton)

// Create an instance of the adapter
//        val pagerAdapter = RegistrationPagerAdapter(supportFragmentManager)
//
//        // Set the adapter for the ViewPager
//        viewPager.adapter = pagerAdapter
//
//        // Set up navigation buttons
//        nextButton.setOnClickListener {
//            viewPager.currentItem = viewPager.currentItem + 1
//        }
//
//        previousButton.setOnClickListener {
//            viewPager.currentItem = viewPager.currentItem - 1
//        }
//
//        // Set up ViewPager listener to update navigation buttons and progress bar
//        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
//            override fun onPageScrollStateChanged(state: Int) {}
//
//            override fun onPageScrolled(
//                position: Int,
//                positionOffset: Float,
//                positionOffsetPixels: Int
//            ) {}
//
//            override fun onPageSelected(position: Int) {
//                // Update navigation buttons and progress bar based on current page
//                val isLastPage = position == pagerAdapter.count - 1
//                nextButton.visibility = if (isLastPage) View.GONE else View.VISIBLE
//                previousButton.visibility = if (position == 0) View.GONE else View.VISIBLE
//                progressBar.progress = (position + 1) * 100 / pagerAdapter.count
//            }
//        })