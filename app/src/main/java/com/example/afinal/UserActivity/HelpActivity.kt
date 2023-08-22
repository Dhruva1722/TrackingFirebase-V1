package com.example.afinal.UserActivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.afinal.R
import com.example.afinal.UserActivity.Adapter.Manager
import com.example.afinal.UserActivity.Adapter.ManagerAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HelpActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var managerAdapter: ManagerAdapter
    private lateinit var databaseReference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        managerAdapter = ManagerAdapter(this, mutableListOf())
        recyclerView.adapter = managerAdapter

        databaseReference = FirebaseDatabase.getInstance().getReference("managers")

        // Fetch data from Firebase and update the adapter
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val managers = mutableListOf<Manager>()
                for (dataSnapshot in snapshot.children) {
                    val manager = dataSnapshot.getValue(Manager::class.java)
                    manager?.let { managers.add(it) }
                }
                managerAdapter.managers = managers
                managerAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HelpActivity", "Error fetching data--------------------", error.toException())
            }
        })

    }
}