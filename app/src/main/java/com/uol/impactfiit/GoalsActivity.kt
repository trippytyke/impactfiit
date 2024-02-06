package com.uol.impactfiit
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatToggleButton
import androidx.core.text.bold
import com.github.stephenvinouze.materialnumberpickercore.MaterialNumberPicker


class GoalsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goals)

        var daysExercise = ""
        var targetWeight = ""
        val continueButton = findViewById<AppCompatButton>(R.id.continueBtn)
        val targetWeightTxt = findViewById<AppCompatEditText>(R.id.targetWeightEt)
        val daysExerciseTxt = findViewById<AppCompatEditText>(R.id.daysExerciseEt)
        val buildMuscleButton = findViewById<AppCompatToggleButton>(R.id.buildMuscleBtn)
        val loseWeightButton = findViewById<AppCompatToggleButton>(R.id.weightLossBtn)
        val incStaminaButton = findViewById<AppCompatToggleButton>(R.id.incStaminaBtn)

        fun weightPicker() {
            val d = AlertDialog.Builder(this)
            val inflater = this.layoutInflater
            val dialogView = inflater.inflate(R.layout.number_picker_dialog, null)
            d.setTitle("Weight")
            d.setMessage("Please select your target weight in kilograms (KG)")
            d.setView(dialogView)
            val numberPicker = dialogView.findViewById<MaterialNumberPicker>(R.id.dialog_number_picker)
            numberPicker.maxValue = 250
            numberPicker.minValue = 30
            numberPicker.value = 60
            numberPicker.setOnValueChangedListener { numberPicker, i, i1 -> println("onValueChange: ") }
            d.setPositiveButton("Done") { dialogInterface, i ->
                println("onClick: " + numberPicker.value)
                targetWeight += numberPicker.value.toString()
                val text = SpannableStringBuilder()
                    .append(numberPicker.value.toString())
                    .bold { append("\tKG") }
                targetWeightTxt.setText(text)
            }
            d.setNegativeButton("Cancel") { dialogInterface, i -> }
            val alertDialog = d.create()
            alertDialog.show()
        }

        fun daysPicker() {
            val d = AlertDialog.Builder(this)
            val inflater = this.layoutInflater
            val dialogView = inflater.inflate(R.layout.number_picker_dialog, null)
            d.setTitle("Days")
            d.setMessage("Please select the number of days you want to exercise")
            d.setView(dialogView)
            val numberPicker = dialogView.findViewById<MaterialNumberPicker>(R.id.dialog_number_picker)
            numberPicker.maxValue = 7
            numberPicker.minValue = 1
            numberPicker.value = 3
            numberPicker.setOnValueChangedListener { numberPicker, i, i1 -> println("onValueChange: ") }
            d.setPositiveButton("Done") { dialogInterface, i ->
                println("onClick: " + numberPicker.value)
                daysExercise += numberPicker.value.toString()
                val text = SpannableStringBuilder()
                    .append(numberPicker.value.toString())
                    .bold { append("\tDays") }
                daysExerciseTxt.setText(text)
            }
            d.setNegativeButton("Cancel") { dialogInterface, i -> }
            val alertDialog = d.create()
            alertDialog.show()
        }

        targetWeightTxt.setOnClickListener {
            weightPicker()
        }

        daysExerciseTxt.setOnClickListener {
            daysPicker()
        }

        continueButton.setOnClickListener {
            var goals = ""
            val intent = Intent(this, OverviewActivity::class.java)

            if (targetWeight.isEmpty() || daysExercise.isEmpty()) {
                Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!buildMuscleButton.isChecked && !loseWeightButton.isChecked && !incStaminaButton.isChecked) {
                Toast.makeText(this, "Please select at least one goal", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (buildMuscleButton.isChecked) {
                goals += "Build muscle mass"
            }
            if (loseWeightButton.isChecked) {
                goals += "\nWeight loss"
            }
            if (incStaminaButton.isChecked) {
                goals += "\nIncrease stamina"
            }

            val bundle = getIntent().extras
            if (bundle != null) {
                intent.putExtras(bundle)
            }
            intent.putExtra("targetWeight", targetWeight)
            intent.putExtra("daysExercise", daysExercise)
            intent.putExtra("goals", goals)
            startActivity(intent)
        }
    }
}