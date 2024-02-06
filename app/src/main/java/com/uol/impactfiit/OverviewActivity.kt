package com.uol.impactfiit
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import java.math.BigDecimal
import java.math.RoundingMode

class OverviewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overview)
        val currentUser = Firebase.auth.currentUser
        val uid = currentUser?.uid
        val db = Firebase.firestore

        // Get the data from the intent
        val fname = intent.getStringExtra("fName")
        val lname = intent.getStringExtra("lName")
        val height = intent.getStringExtra("height")
        val weight = intent.getStringExtra("weight")
        val dateBirth = intent.getStringExtra("dateBirth")
        val fitGoals = intent.getStringExtra("daysExercise")
        val goals = intent.getStringExtra("goals")
        val targetWeight = intent.getStringExtra("targetWeight")

        // Get the views
        val continueButton = findViewById<AppCompatButton>(R.id.continueBtn)
        val nameTxt = findViewById<AppCompatTextView>(R.id.nameTv)
        val heightTxt = findViewById<AppCompatTextView>(R.id.heightTv)
        val weightTxt = findViewById<AppCompatTextView>(R.id.weightTv)
        val dateBirthTxt = findViewById<AppCompatTextView>(R.id.dateOfBirthTv)
        val targetWeightTxt = findViewById<AppCompatTextView>(R.id.targetWeightTv)
        val fitGoalsTxt = findViewById<AppCompatTextView>(R.id.fitGoalTv)
        val endGoalsTxt = findViewById<AppCompatTextView>(R.id.endGoalTv)
        val bmiTxt = findViewById<AppCompatTextView>(R.id.bmiTv)

        fun setName() { //Function to set the textView of the name
            val name = SpannableStringBuilder("$fname $lname")
            if (fname != null) {
                name.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorName)), 0, fname.length, 0)
            }
            nameTxt.setText(name)
        }

        fun setDOB() { //Function to set the textView of the date of birth
            val dateBirthText = SpannableStringBuilder(dateBirth)
            val firstSlashPos = dateBirthText.indexOf('/')
            val secondSlashPos = dateBirthText.lastIndexOf('/')

            if (firstSlashPos != -1) {
                dateBirthText.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorName)), firstSlashPos, firstSlashPos + 1, 0)
            }
            if (secondSlashPos != -1) {
                dateBirthText.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorName)), secondSlashPos, secondSlashPos + 1, 0)
            }
            dateBirthTxt.setText(dateBirthText)
        }

        fun setHeight() { //Function to set the textView of the height
            val heightText = SpannableStringBuilder(height + " CM")
            val cmPos = heightText.indexOf("CM")
            if (cmPos != -1) {
                heightText.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorName)), cmPos, cmPos + 2, 0)
            }
            heightTxt.setText(heightText)
        }

        fun setWeight() { //Function to set the textView of the weight
            val weightText = SpannableStringBuilder(weight + " KG")
            val kgPos = weightText.indexOf("KG")
            if (kgPos != -1) {
                weightText.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorName)), kgPos, kgPos + 2, 0)
            }
            weightTxt.setText(weightText)
        }

        fun setTargetWeight() { //Function to set the textView of the target weight
            val targetWeightText = SpannableStringBuilder(targetWeight + " KG")
            val targetKgPos = targetWeightText.indexOf("KG")
            if (targetKgPos != -1) {
                targetWeightText.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorName)), targetKgPos, targetKgPos + 2, 0)
            }
            targetWeightTxt.setText(targetWeightText)
        }

        fun setFitGoals(){ //Function to set the textView of the fit goals
            val fitGoalsText = SpannableStringBuilder(fitGoals + " times a week")
            fitGoalsText.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorName)), 0, 1, 0)
            fitGoalsTxt.setText(fitGoalsText)
        }

        fun setBMI() { //Function to set the textView of the BMI
            val heightInMeters = height!!.toDouble() / 100
            val bmi = weight!!.toDouble() / (heightInMeters * heightInMeters)
            bmiTxt.setText(BigDecimal(bmi).setScale(1, RoundingMode.HALF_EVEN).toString())
        }

        fun setEndGoals() { //Function to set the textView of the end goals
            endGoalsTxt.setText(goals)
        }

        // Call the functions
        setName()
        setDOB()
        setHeight()
        setWeight()
        setTargetWeight()
        setFitGoals()
        setBMI()
        setEndGoals()


        continueButton.setOnClickListener {//When the continue button is clicked, the user is taken to the HomeActivity
            val intent = Intent(this, HomeActivity::class.java)
            val dob = dateBirth!!.replace(" ", "")
            val user = hashMapOf(
                "first" to fname,
                "last" to lname,
                "dob" to dob,
                "height" to height,
                "weight" to weight,
                "targetWeight" to targetWeight,
                "fitGoals" to fitGoals,
                "goals" to goals
            )
            db.collection("users").document(uid!!)
                .set(user)
                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                .addOnFailureListener { Log.w(TAG, "Error writing document") }

            startActivity(intent)
        }
    }
}