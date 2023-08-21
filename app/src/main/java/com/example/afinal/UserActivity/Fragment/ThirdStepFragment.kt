package com.example.afinal.UserActivity.Fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.afinal.MapActivity.MapsActivity
import com.example.afinal.R
import com.example.afinal.UserActivity.RegistrationActivity
import com.example.afinal.UserActivity.UserDetails


class ThirdStepFragment : Fragment() {

    private lateinit var registerButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_third_step, container, false)



        registerButton = view.findViewById(R.id.registerBtn)
        registerButton.setOnClickListener{
            val intent = Intent (requireContext(), MapsActivity::class.java)
            startActivity(intent)
        }



        return view
    }


}