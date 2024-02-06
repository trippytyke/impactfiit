package com.uol.impactfiit

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.text.bold
import java.util.*
import com.github.stephenvinouze.materialnumberpickercore.MaterialNumberPicker


class AboutActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        var weight = ""
        var height = ""
        val continueButton = findViewById<AppCompatButton>(R.id.continueBtn)
        val fNameTxt = findViewById<AppCompatEditText>(R.id.firstNameEt)
        val lNameTxt = findViewById<AppCompatEditText>(R.id.lastNameEt)
        val dateBirthTxt = findViewById<AppCompatEditText>(R.id.dateOfBirthEt)
        val heightTxt = findViewById<AppCompatEditText>(R.id.heightEt)
        val weightTxt = findViewById<AppCompatEditText>(R.id.weightEt)


        dateBirthTxt.setOnClickListener {

            val c = Calendar.getInstance()
            c.add(Calendar.YEAR, -12)
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)


            val datePickerDialog = DatePickerDialog(
                this,
                R.style.MyDatePickerStyle,
                { view, year, monthOfYear, dayOfMonth ->
                    val dat = (dayOfMonth.toString() + " / " + (monthOfYear + 1) + " / " + year)
                    dateBirthTxt.setText(dat)
                },
                year,
                month,
                day
            )
            datePickerDialog.datePicker.maxDate = c.timeInMillis
            datePickerDialog.show()
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
            numberPicker.value = 150
            numberPicker.setOnValueChangedListener { numberPicker, i, i1 -> println("onValueChange: ") }
            d.setPositiveButton("Done") { dialogInterface, i ->
                println("onClick: " + numberPicker.value)
                height = numberPicker.value.toString()
                val text = SpannableStringBuilder()
                    .append(numberPicker.value.toString())
                    .bold { append("\tCM") }
                heightTxt.setText(text)
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
            numberPicker.value = 60
            numberPicker.setOnValueChangedListener { numberPicker, i, i1 -> println("onValueChange: ") }
            d.setPositiveButton("Done") { dialogInterface, i ->
                println("onClick: " + numberPicker.value)
                weight = numberPicker.value.toString()
                val text = SpannableStringBuilder()
                    .append(numberPicker.value.toString())
                    .bold { append("\tKG") }
                weightTxt.setText(text)
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

        continueButton.setOnClickListener {
            val intent = Intent(this, GoalsActivity::class.java)
            val fName = fNameTxt.text.toString()
            val lName = lNameTxt.text.toString()
            val dateBirth = dateBirthTxt.text.toString()

            //Check if the fields are empty
            if (fName.isEmpty() || lName.isEmpty() || dateBirth.isEmpty() || height.isEmpty() || weight.isEmpty()) {
                Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //Pass the data to the next activity
            intent.putExtra("fName", fName)
            intent.putExtra("lName", lName)
            intent.putExtra("dateBirth", dateBirth)
            intent.putExtra("height", height)
            intent.putExtra("weight", weight)
            startActivity(intent)
        }


    }
}