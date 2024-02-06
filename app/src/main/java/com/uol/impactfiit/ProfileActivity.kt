package com.uol.impactfiit

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.stephenvinouze.materialnumberpickercore.MaterialNumberPicker
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.Period

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        var targetWeight = ""
        var weight = ""
        var height = ""
        val saveButton = findViewById<AppCompatButton>(R.id.saveBtn)
        val heightTxt = findViewById<AppCompatEditText>(R.id.heightEt)
        val weightTxt = findViewById<AppCompatEditText>(R.id.weightEt)
        val targetWeightTxt = findViewById<AppCompatEditText>(R.id.targetWeightEt)
        val nameTv = findViewById<AppCompatTextView>(R.id.nameTv)
        val ageTv = findViewById<AppCompatTextView>(R.id.ageTv)
        val bmiTv = findViewById<AppCompatTextView>(R.id.bmiTv)

        val currentUser = Firebase.auth.currentUser
        val uid = currentUser?.uid
        val db = Firebase.firestore
        val docRef = db.collection("users").document(uid!!)

        fun setName(fname: String, lname: String) { //Function to set the textView of the name
            val name = SpannableStringBuilder("$fname $lname")
            if (fname != null) {
                name.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorName)), 0, fname.length, 0)
            }
            nameTv.setText(name)
        }

        fun setHeight(height: String) { //Function to set the textView of the height
            val heightText = SpannableStringBuilder(height + " CM")
            val cmPos = heightText.indexOf("CM")
            if (cmPos != -1) {
                heightText.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorName)), cmPos, cmPos + 2, 0)
            }
            heightTxt.setText(heightText)
        }

        fun setWeight(weight: String) { //Function to set the textView of the weight
            val weightText = SpannableStringBuilder(weight + " KG")
            val kgPos = weightText.indexOf("KG")
            if (kgPos != -1) {
                weightText.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorName)), kgPos, kgPos + 2, 0)
            }
            weightTxt.setText(weightText)
        }

        fun setTargetWeight(targetWeight: String) { //Function to set the textView of the target weight
            val targetWeightText = SpannableStringBuilder(targetWeight + " KG")
            val targetKgPos = targetWeightText.indexOf("KG")
            if (targetKgPos != -1) {
                targetWeightText.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorName)), targetKgPos, targetKgPos + 2, 0)
            }
            targetWeightTxt.setText(targetWeightText)
        }

        fun setBMI(height: String, weight: String) { //Function to set the textView of the BMI
            val heightInMeters = height!!.toDouble() / 100
            val bmi = weight!!.toDouble() / (heightInMeters * heightInMeters)
            bmiTv.setText(BigDecimal(bmi).setScale(1, RoundingMode.HALF_EVEN).toString())
        }

        fun getAge(year: Int, month: Int, dayOfMonth: Int): Int {
            return Period.between(
                LocalDate.of(year, month, dayOfMonth),
                LocalDate.now()
            ).years
        }

        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val fname = document.getString("first")
                    val lname = document.getString("last")
                    val dob = document.getString("dob")
                    height = document.getString("height")!!
                    weight = document.getString("weight")!!
                    targetWeight = document.getString("targetWeight")!!

                    val items1: List<String> = dob!!.split("/")
                    val day = items1[0]
                    val month = items1[1]
                    val year = items1[2]
                    val age = getAge(year.toInt(), month.toInt(), day.toInt())
                    ageTv.setText(age.toString())
                    setName(fname!!, lname!!)
                    setWeight(weight!!)
                    setHeight(height!!)
                    setTargetWeight(targetWeight!!)
                    setBMI(height, weight)
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }


        fun heightPicker() { //Function to initialise the height picker
            val d = AlertDialog.Builder(this)
            val inflater = this.layoutInflater
            val dialogView = inflater.inflate(R.layout.number_picker_dialog, null)
            d.setTitle("Height")
            d.setMessage("Please select your current height in centimeters (CM)")
            d.setView(dialogView)
            val numberPicker = dialogView.findViewById<NumberPicker>(R.id.dialog_number_picker)
            numberPicker.maxValue = 250
            numberPicker.minValue = 50
            numberPicker.value = height.toInt()
            numberPicker.setOnValueChangedListener { numberPicker, i, i1 -> println("onValueChange: ") }
            d.setPositiveButton("Done") { dialogInterface, i ->
                println("onClick: " + numberPicker.value)
                height = numberPicker.value.toString()
                setHeight(height)
            }
            d.setNegativeButton("Cancel") { dialogInterface, i -> }
            val alertDialog = d.create()
            alertDialog.show()
        }


        fun weightPicker() { //Function to initialise the weight picker
            val d = AlertDialog.Builder(this)
            val inflater = this.layoutInflater
            val dialogView = inflater.inflate(R.layout.number_picker_dialog, null)
            d.setTitle("Weight")
            d.setMessage("Please select your current weight in kilograms (KG)")
            d.setView(dialogView)
            val numberPicker = dialogView.findViewById<MaterialNumberPicker>(R.id.dialog_number_picker)
            numberPicker.maxValue = 250
            numberPicker.minValue = 30
            numberPicker.value = weight.toInt()
            numberPicker.setOnValueChangedListener { numberPicker, i, i1 -> println("onValueChange: ") }
            d.setPositiveButton("Done") { dialogInterface, i ->
                println("onClick: " + numberPicker.value)
                weight = numberPicker.value.toString()
                setWeight(weight)
            }
            d.setNegativeButton("Cancel") { dialogInterface, i -> }
            val alertDialog = d.create()
            alertDialog.show()
        }

        fun targetWeightPicker() {
            val d = AlertDialog.Builder(this)
            val inflater = this.layoutInflater
            val dialogView = inflater.inflate(R.layout.number_picker_dialog, null)
            d.setTitle("Weight")
            d.setMessage("Please select your target weight in kilograms (KG)")
            d.setView(dialogView)
            val numberPicker = dialogView.findViewById<MaterialNumberPicker>(R.id.dialog_number_picker)
            numberPicker.maxValue = 250
            numberPicker.minValue = 30
            numberPicker.value = targetWeight.toInt()
            numberPicker.setOnValueChangedListener { numberPicker, i, i1 -> println("onValueChange: ") }
            d.setPositiveButton("Done") { dialogInterface, i ->
                println("onClick: " + numberPicker.value)
                targetWeight = numberPicker.value.toString()
                setTargetWeight(targetWeight)
            }
            d.setNegativeButton("Cancel") { dialogInterface, i -> }
            val alertDialog = d.create()
            alertDialog.show()
        }

        heightTxt.setOnClickListener {//When the height text is clicked, the height picker is initialised
            heightPicker()
        }

        weightTxt.setOnClickListener {//When the weight text is clicked, the weight picker is initialised
            weightPicker()
        }

        targetWeightTxt.setOnClickListener {
            targetWeightPicker()
        }

        saveButton.setOnClickListener {
            docRef
                .update(
                    "height", height,
                    "weight", weight,
                    "targetWeight", targetWeight
                )
                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
                .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
        }
    }
}