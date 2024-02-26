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

        val logoutButton = view.findViewById<AppCompatButton>(R.id.logoutBtn)
        val profileButton = view.findViewById<AppCompatButton>(R.id.profileBtn)

        profileButton.setOnClickListener {
            val intent = Intent(activity, ProfileActivity::class.java)
            startActivity(intent)
        }


        logoutButton.setOnClickListener {
            val intent = Intent(activity, LoginActivity::class.java)
            Firebase.auth.signOut()
            startActivity(intent)

        }

    }

}