package com.example.afinal

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.afinal.UserActivity.Fragment.AccountFragment
import com.example.afinal.UserActivity.Fragment.AttendanceFragment
import com.example.afinal.UserActivity.Fragment.HomeFragment
import com.example.afinal.UserActivity.LoginActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() ,BottomNavigationView.OnNavigationItemSelectedListener{

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE)

        var toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        actionBarDrawerToggle =
            ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)


        actionBarDrawerToggle.syncState()

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)

        toolbar = findViewById(R.id.toolbar)
        actionBarDrawerToggle.isDrawerIndicatorEnabled = true
        toolbar.navigationIcon!!
            .setColorFilter(resources.getColor(R.color.white), PorterDuff.Mode.SRC_ATOP)


        toolbar.setOnClickListener {
            if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
        val navigationView = findViewById<NavigationView>(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener {item -> // Handle navigation item selection here
            //drawerLayout.closeDrawer(GravityCompat.START)
            var fragment: Fragment? = null
            if (item.itemId == R.id.nav_attendance) {
                fragment = AttendanceFragment()
                drawerLayout.closeDrawer(GravityCompat.START)
            } else if (item.itemId == R.id.nav_help) {
                fragment = AccountFragment()
                drawerLayout.closeDrawer(GravityCompat.START)
            } else if (item.itemId == R.id.nav_logout) {
                mAuth.signOut()
                sharedPreferences.edit().remove("isLoggedIn").apply()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                true
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            fragment?.let { loadFragment(it) }
            true
        }

        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        bottomNavigationView.setOnNavigationItemSelectedListener(this)
        loadFragment(HomeFragment())
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var fragment: Fragment? = null
        if (item.itemId == R.id.bottomnav_home) {
            fragment = HomeFragment()
        } else if (item.itemId == R.id.bottomnav_attendence) {
            fragment = AttendanceFragment()
        } else if (item.itemId == R.id.bottomnav_account) {
            fragment = AccountFragment()
        }
        fragment?.let { loadFragment(it) }
        return true
    }

    fun loadFragment(fragment: Fragment?) {
        //to attach fragment
        supportFragmentManager.beginTransaction().replace(R.id.relativelayout, fragment!!).commit()
    }
}




