package com.uol.impactfiit

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class WorkoutFragment: Fragment(R.layout.fragment_workout) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val routineButton = view.findViewById<AppCompatImageButton>(R.id.routineBtn)
        val exploreButton = view.findViewById<AppCompatImageButton>(R.id.exploreBtn)

        exploreButton.setOnClickListener {
            val intent = Intent(activity, ExploreActivity::class.java)
            startActivity(intent)
        }

        routineButton.setOnClickListener {
            val intent = Intent(activity, RoutineActivity::class.java)
            startActivity(intent)
        }
    }
}