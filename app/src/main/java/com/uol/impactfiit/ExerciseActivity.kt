package com.uol.impactfiit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.StringRequest
import com.google.android.material.appbar.MaterialToolbar
import org.json.JSONArray


class ExerciseActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise)

        val topAppBar = findViewById<MaterialToolbar>(R.id.topAppBar)
        val gifUrl = intent.getStringExtra("exerciseGifUrl")
        val exerciseId = intent.getStringExtra("exerciseId")
        val exerciseInstructions = intent.getStringExtra("exerciseInstructions")
        val exerciseEquipment = intent.getStringExtra("exerciseEquipment")
        val exerciseBodyPart = intent.getStringExtra("exerciseBodyPart")
        val exerciseTargetMuscle = intent.getStringExtra("exerciseTargetMuscle")
        val exerciseName = intent.getStringExtra("exerciseName")

        val exerciseNameTxt = findViewById<androidx.appcompat.widget.AppCompatTextView>(R.id.exerciseTv)
        val webView = findViewById<WebView>(R.id.exerciseGif)

        exerciseNameTxt.text = exerciseName

        webView.getSettings().setJavaScriptEnabled(true)
        webView.setWebViewClient(WebViewClient())

        webView.loadUrl(gifUrl!!)

        topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}