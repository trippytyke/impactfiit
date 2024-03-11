package com.uol.impactfiit


import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar



class ExerciseActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise)

        //Get the top app bar view
        val topAppBar = findViewById<MaterialToolbar>(R.id.topAppBar)

        //Get the intent data
        val gifUrl = intent.getStringExtra("exerciseGifUrl")
        var exerciseInstructions = intent.getStringExtra("exerciseInstructions")
        val exerciseEquipment = intent.getStringExtra("exerciseEquipment")
        val exerciseBodyPart = intent.getStringExtra("exerciseBodyPart")
        val exerciseTargetMuscle = intent.getStringExtra("exerciseTargetMuscle")
        val exerciseName = intent.getStringExtra("exerciseName")

        //Get the text and image views
        val exerciseNameTxt = findViewById<androidx.appcompat.widget.AppCompatTextView>(R.id.exerciseTv)
        val exerciseInsTxt = findViewById<androidx.appcompat.widget.AppCompatTextView>(R.id.instructionsTv)
        val exerciseEquipTxt = findViewById<androidx.appcompat.widget.AppCompatTextView>(R.id.equipmentTv)
        val exerciseBodyPartTxt = findViewById<androidx.appcompat.widget.AppCompatTextView>(R.id.bodyPartTv)
        val exerciseTMuscleTxt = findViewById<androidx.appcompat.widget.AppCompatTextView>(R.id.targetMuscleTv)
        val webView = findViewById<WebView>(R.id.exerciseGif)

        //Get the screen width
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val squareSize = (screenWidth * 0.8).toInt() // 80% of the screen width

        val layoutParams = webView.layoutParams
        layoutParams.width = squareSize
        layoutParams.height = squareSize
        webView.layoutParams = layoutParams

        Log.d(TAG, exerciseInstructions.toString())

        exerciseInstructions = exerciseInstructions!!.replace("[", "").replace("]", "")
        val splitInstructions = exerciseInstructions.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
        val numberedInstructions = splitInstructions.withIndex().joinToString("\n\n") { (index, line) ->
            "${index + 1}. $line"
        }
        exerciseInstructions = numberedInstructions
        exerciseInstructions = exerciseInstructions.replace("\"", "")

        //Set the text views with data from the intent
        exerciseNameTxt.text = exerciseName
        exerciseInsTxt.text = exerciseInstructions
        exerciseEquipTxt.text = exerciseEquipment
        exerciseBodyPartTxt.text = exerciseBodyPart
        exerciseTMuscleTxt.text = exerciseTargetMuscle

        //Set the gif in the webview
        webView.getSettings().setJavaScriptEnabled(true)
        webView.setWebViewClient(WebViewClient())
        webView.loadUrl(gifUrl!!)

        //Set the top app bar navigation icon to go back to the previous activity
        topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}