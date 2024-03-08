package com.uol.impactfiit
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils.replace
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class SettingsFragment:Fragment(R.layout.fragment_settings) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Get the buttons in the view
        val logoutButton = view.findViewById<AppCompatButton>(R.id.logoutBtn)
        val profileButton = view.findViewById<AppCompatButton>(R.id.profileBtn)

        //If the user clicks the profile button, go to the profile page
        profileButton.setOnClickListener {
            val intent = Intent(activity, ProfileActivity::class.java)
            startActivity(intent)
        }

        //If the user clicks the logout button, sign out and go to the login page
        logoutButton.setOnClickListener {
            val intent = Intent(activity, LoginActivity::class.java)
            Firebase.auth.signOut()
            startActivity(intent)

        }

    }

}