package com.uol.impactfiit

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class LogFragment:Fragment(R.layout.fragment_log) {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_log, container, false)

        val buttonNavigate: Button = view.findViewById(R.id.btnNavigate)
        buttonNavigate.setOnClickListener {
            val intent = Intent(activity, LogActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}

