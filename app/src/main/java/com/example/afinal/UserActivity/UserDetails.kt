package com.example.afinal.UserActivity

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.afinal.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserDetails : AppCompatActivity() {

    private lateinit var helpBtn: ImageView

    private lateinit var logoutbtn: Button
    private lateinit var savebtn: Button

    private lateinit var mAuth: FirebaseAuth
    private lateinit var radioGroup: RadioGroup
    private lateinit var busRadio: RadioButton
    private lateinit var bikeRadio: RadioButton
    private lateinit var trainRadio: RadioButton
    private lateinit var flightRadio: RadioButton
    private lateinit var uploadButton: ImageView
    private lateinit var imgContainer:RelativeLayout

    private lateinit var database: FirebaseDatabase

    private lateinit var sharedPreferences: SharedPreferences

    private val selectedImages = mutableListOf<Uri>()

    private lateinit var storage: FirebaseStorage



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)


        radioGroup = findViewById(R.id.idRadioGroup)
        uploadButton = findViewById(R.id.uploadButton)
        savebtn = findViewById(R.id.saveBtn)

        busRadio = findViewById(R.id.idBtnBusRadio)
        bikeRadio = findViewById(R.id.idBtnBikeRadio)
        trainRadio = findViewById(R.id.idBtnTrainRadio)
        flightRadio = findViewById(R.id.idBtnFlightRadio)
        imgContainer = findViewById(R.id.imageContainer)

        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()


        // Initialize sharedPreferences
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE)


        uploadButton.setOnClickListener {
            openImagePicker()
        }

        // Set a listener for the save button
        savebtn.setOnClickListener {
            saveUserData()
        }
        helpBtn = findViewById(R.id.helpBtn)

        helpBtn.setOnClickListener { v ->
            showPopupMenu(v)
        }

        logoutbtn = findViewById(R.id.logoutBtn)

        logoutbtn.setOnClickListener {
            mAuth.signOut()
            sharedPreferences.edit().remove("isLoggedIn").apply()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_up,
                R.anim.slide_down);
            finish()
        }


    }
    private fun showSuccessMessage() {
        val thankYouTextView = findViewById<TextView>(R.id.thankYouTextView)
        val successIconImageView = findViewById<ImageView>(R.id.successIconImageView)

        thankYouTextView.visibility = View.VISIBLE
        successIconImageView.visibility = View.VISIBLE
    }


    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(Intent.createChooser(intent, "Select Images"),
            Companion.IMAGE_PICKER_REQUEST_CODE
        )
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Companion.IMAGE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.clipData?.let { clipData ->
                for (i in 0 until clipData.itemCount) {
                    val imageUri = clipData.getItemAt(i).uri
                    selectedImages.add(imageUri)
                }
            }
            data?.data?.let { imageUri ->
                selectedImages.add(imageUri)
            }

            // Process and upload images
            uploadImages(selectedImages)
        }
    }

    private fun uploadImages(images: List<Uri>) {
        val userId = mAuth.currentUser?.uid
        if (userId != null) {
            val storageRef = storage.reference.child("images").child(userId)
            for (imageUri in images) {
                val imageRef = storageRef.child(imageUri.lastPathSegment!!)
                imageRef.putFile(imageUri)
                    .addOnSuccessListener {
                        // Image uploaded successfully
                        // Now load the image into the corresponding ImageView
                        showSuccessMessage()
                        Toast.makeText(this, "Image upload successful", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        // Image upload failed
                        Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }


    private fun saveUserData() {
        val userId = mAuth.currentUser?.uid
        if (userId != null) {
            val selectedTransportation = when {
                busRadio.isChecked -> "Bus"
                bikeRadio.isChecked -> "Bike"
                trainRadio.isChecked -> "Train"
                flightRadio.isChecked -> "Flight"
                else -> ""
            }

            val timestamp = System.currentTimeMillis() // Get current timestamp in milliseconds

            val dateTimeFormat = SimpleDateFormat("dd/MM/yy hh:mm:ss", Locale.getDefault())
            val formattedTimestamp = dateTimeFormat.format(Date(timestamp))

            val saveUserData = saveUserData(
                selectedTransportation,
                formattedTimestamp
            )

            // Store visitor data in Realtime Database under user's unique ID
            val userVisitorRef = database.reference.child("users").child(userId).push()
            userVisitorRef.setValue(saveUserData)
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

    companion object {
        private const val IMAGE_PICKER_REQUEST_CODE = 123
    }
}

data class saveUserData(
    val selectedTransportation: String,
    val timestamp: String
)