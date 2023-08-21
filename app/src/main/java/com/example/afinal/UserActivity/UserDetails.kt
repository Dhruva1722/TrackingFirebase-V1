package com.example.afinal.UserActivity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import com.example.afinal.MapActivity.MapsActivity
import com.example.afinal.R

class UserDetails : AppCompatActivity() {

    private lateinit var helpBtn: ImageView

    private lateinit var logoutbtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)

        helpBtn = findViewById(R.id.helpBtn)

        helpBtn.setOnClickListener { v ->
            showPopupMenu(v)
        }

        logoutbtn = findViewById(R.id.logoutBtn)

        logoutbtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
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
                    val intent = Intent(this, ComplainActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        // Show the popup menu
        popupMenu.show()
    }
}