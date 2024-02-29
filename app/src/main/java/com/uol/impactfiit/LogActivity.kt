package com.uol.impactfiit

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogActivity : AppCompatActivity() {
    private val eatenFoods = mutableListOf<Recipe>()
    private val dailyCaloriesList = mutableListOf<DailyCalories>()

    val currentUser = Firebase.auth.currentUser
    val uid = currentUser?.uid
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log)

        val eatenFoodRecyclerView: RecyclerView = findViewById(R.id.recyclerViewEatenFoods)
        eatenFoodRecyclerView.layoutManager = LinearLayoutManager(this)

        val foodName: EditText = findViewById(R.id.foodNameEt)
        val calories: EditText = findViewById(R.id.caloriesEt)
        val addButton: Button = findViewById((R.id.addBtn))
        val addImage: ImageView = findViewById(R.id.selectedImage)

        val galleryImage = registerForActivityResult(ActivityResultContracts.GetContent(),
            ActivityResultCallback {
                addImage.setImageURI(it)
            })
        addImage.setOnClickListener{
            galleryImage.launch("image/*")
        }


        eatenFoodRecyclerView.adapter = RecipeAdapter(eatenFoods) { recipe ->
            eatenFoods.add(recipe)
            addCalories(recipe.calorie)
            Toast.makeText(this, "${recipe.name} added to eaten foods", Toast.LENGTH_SHORT).show()
        }

        addButton.setOnClickListener {
            val foodNameText = foodName.text.toString().trim()
            val caloriesText = calories.text.toString().trim()

            if (foodNameText.isNotEmpty() && caloriesText.isNotEmpty()) {
                val calorieValue = caloriesText.toIntOrNull() ?: 0
                val newFoodItem = Recipe(foodNameText, calorieValue, addImage.toString())
                eatenFoods.add(newFoodItem)
                addCalories(calorieValue)
                eatenFoodRecyclerView.adapter?.notifyDataSetChanged()
                Toast.makeText(this, "$foodNameText added to eaten foods", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter food name and calories", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun updateTotalCalories() {
        val today = getCurrentDate()
        val todayCalories = dailyCaloriesList.find { it.date == today }?.totalCalories ?: 0
        val dailyCaloriesEntry = DailyCalories(today, todayCalories)
        uid?.let {
            db.collection("calorieLog").document(it).collection("dailyIntake").document(today).set(dailyCaloriesEntry)
        }
    }

    private fun addCalories(calories: Int) {
        val today = getCurrentDate()
        val existingEntry = dailyCaloriesList.find { it.date == today }
        if (existingEntry != null) {
            dailyCaloriesList[dailyCaloriesList.indexOf(existingEntry)] =
                existingEntry.copy(totalCalories = existingEntry.totalCalories + calories)
        } else {
            dailyCaloriesList.add(DailyCalories(today, calories))
        }
        updateTotalCalories()
    }
}