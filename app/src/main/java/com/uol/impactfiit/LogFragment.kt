package com.uol.impactfiit

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.Fragment

class LogFragment:Fragment(R.layout.fragment_log) {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_log, container, false)

        val buttonExplore: ImageButton = view.findViewById(R.id.btnExplore)
        val buttonLog: ImageButton = view.findViewById(R.id.btnLog)
        buttonExplore.setOnClickListener {
            val intent = Intent(activity, RecipeActivity::class.java)
            startActivity(intent)
        }
        buttonLog.setOnClickListener {
            val intent = Intent(activity, LogActivity::class.java)
            startActivity(intent)
        }
        return view
    }
}

