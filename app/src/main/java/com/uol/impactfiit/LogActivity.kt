package com.uol.impactfiit

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class LogActivity : AppCompatActivity() {
    private val eatenFoods = mutableListOf<Recipe>()
    private val dailyCaloriesList = mutableListOf<DailyCalories>()
    private lateinit var eatenFoodRecyclerView: RecyclerView
    private lateinit var totalCaloriesText: TextView

    val currentUser = Firebase.auth.currentUser
    val uid = currentUser?.uid
    val db = Firebase.firestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log)

        eatenFoodRecyclerView = findViewById(R.id.recyclerViewEatenFoods)
        eatenFoodRecyclerView.layoutManager = LinearLayoutManager(this)

        val foodName: EditText = findViewById(R.id.foodNameEt)
        val calories: EditText = findViewById(R.id.caloriesEt)
        val addButton: Button = findViewById((R.id.addBtn))
        val addImage: ImageView = findViewById(R.id.selectedImage)
        val topAppBar = findViewById<MaterialToolbar>(R.id.topAppBar)
        totalCaloriesText = findViewById(R.id.totalCaloriesText)

        topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val galleryImage = registerForActivityResult(ActivityResultContracts.GetContent(),
            ActivityResultCallback {
                addImage.setImageURI(it)
            })
        addImage.setOnClickListener{
            galleryImage.launch("image/*")
        }

        eatenFoodRecyclerView.adapter = RecipeAdapter(eatenFoods) { recipe ->
            removeEatenFood(recipe)
        }

        addButton.setOnClickListener {
            val foodNameText = foodName.text.toString().trim()
            val caloriesText = calories.text.toString().trim()
            val carbohydrateText = calories.text.toString().trim()
            val proteinText = calories.text.toString().trim()

            if (foodNameText.isNotEmpty() && caloriesText.isNotEmpty()) {
                val calorieValue = caloriesText.toIntOrNull() ?: 0
                val carbohydrateValue = carbohydrateText.toIntOrNull() ?: 0
                val proteinValue = proteinText.toIntOrNull() ?: 0

                val newFoodItem = Recipe(id = UUID.randomUUID().toString(), foodNameText, calorieValue, carbohydrateValue, proteinValue, addImage.toString())
                eatenFoods.add(newFoodItem)
                addCalories(newFoodItem)
                eatenFoodRecyclerView.adapter?.notifyDataSetChanged()
                Toast.makeText(this, "$foodNameText added to eaten foods", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter food name and calories", Toast.LENGTH_SHORT).show()
            }
        }

        fetchEatenFoods()

    }

    class RecipeAdapter(private val recipeList: MutableList<Recipe>,
                        private val onRecipeRemoved: (Recipe) -> Unit) :
        RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

        class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imageView: ImageView = view.findViewById(R.id.imageViewRecipe)
            val nameTextView: TextView = view.findViewById(R.id.textViewRecipeName)
            val caloriesTextView: TextView = view.findViewById(R.id.textViewCalories)
            val carbohydratesTextView: TextView = view.findViewById(R.id.textViewCarbohydrate)
            val proteinTextView: TextView = view.findViewById(R.id.textViewProtein)
            val removeButton: Button = view.findViewById(R.id.buttonRemoveEaten)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_log, parent, false)
            return RecipeViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
            val recipe = recipeList[position]
            holder.nameTextView.text = recipe.name
            holder.caloriesTextView.text = "Calories: ${recipe.calorie}"
            holder.carbohydratesTextView.text = "Carbohydrates: ${recipe.carbohydrates}"
            holder.proteinTextView.text = "Protein: ${recipe.protein}"
            Glide.with(holder.imageView.context).load(recipe.image).into(holder.imageView)
            holder.removeButton.setOnClickListener {
                onRecipeRemoved(recipe)
                recipeList.removeAt(position)
                notifyItemRemoved(position)
            }
        }
        override fun getItemCount() = recipeList.size
    }


    private fun fetchEatenFoods() {
        val today = getCurrentDate()
        uid?.let { userId ->
            db.collection("calorieLog").document(userId).collection("dailyIntake").document(today)
                .collection("recipes")
                .get()
                .addOnSuccessListener { result ->
                    eatenFoods.clear()
                    var totalCalories = 0
                    for (document in result) {
                        val recipe = document.toObject(Recipe::class.java).copy(id = document.id)
                        recipe?.let {
                            eatenFoods.add(it)
                            totalCalories += it.calorie
                        }
                    }
                    dailyCaloriesList.clear()
                    dailyCaloriesList.add(DailyCalories(today, totalCalories))
                    eatenFoodRecyclerView.adapter?.notifyDataSetChanged()
                    totalCaloriesText.text = "Total Calories: $totalCalories"
                    Log.d("LogActivity", "Total Calories $totalCalories")
                }
                .addOnFailureListener { e ->
                    Log.e("LogActivity", "Error fetching eaten foods", e)
                }
        }
    }
    private fun removeEatenFood(recipe: Recipe) {
        val today = getCurrentDate()
        uid?.let { userId ->
            recipe.id?.let { documentId ->
                db.collection("calorieLog").document(userId).collection("dailyIntake").document(today)
                    .collection("recipes").document(documentId).delete()
                    .addOnSuccessListener {
                        Log.d("LogActivity", "Recipe removed from eaten foods in Firestore")
                        // Refresh the list and total calories
                        fetchEatenFoods()
                    }
                    .addOnFailureListener { e ->
                        Log.e("LogActivity", "Error removing recipe from Firestore", e)
                    }
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

    private fun addCalories(recipe: Recipe) {
        val today = getCurrentDate()
        val existingEntry = dailyCaloriesList.find { it.date == today }
        val newTotalCalories = (existingEntry?.totalCalories ?: 0) + recipe.calorie
        if (existingEntry != null) {
            dailyCaloriesList[dailyCaloriesList.indexOf(existingEntry)] =
                existingEntry.copy(totalCalories = newTotalCalories)
        } else {
            dailyCaloriesList.add(DailyCalories(today, newTotalCalories))
        }
        updateTotalCalories()
        totalCaloriesText.text = "Total Calories: $newTotalCalories"

        uid?.let { userId ->
            db.collection("calorieLog").document(userId)
                .collection("dailyIntake").document(today)
                .collection("recipes").add(recipe)
                .addOnSuccessListener {
                    Log.d("LogActivity", "Recipe added to eaten foods in Firestore")
                }
                .addOnFailureListener { e ->
                    Log.e("LogActivity", "Error adding recipe to Firestore", e)
                }
        }
    }
}