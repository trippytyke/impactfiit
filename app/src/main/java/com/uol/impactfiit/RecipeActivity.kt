package com.uol.impactfiit

import android.content.Context
import android.content.Intent
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
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Recipe(val id: String? = null, val name: String = "", val calorie: Int = 0, val carbohydrates: Int = 0,val protein: Int = 0,val image: String = "")
data class DailyCalories(val date: String, val totalCalories: Int)
class RecipeActivity : AppCompatActivity() {
    private val eatenFoods = mutableListOf<Recipe>()
    private val recipeList = mutableListOf<Recipe>()
    private val dailyCaloriesList = mutableListOf<DailyCalories>()

    val currentUser = Firebase.auth.currentUser
    val uid = currentUser?.uid
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe)

        val topAppBar = findViewById<MaterialToolbar>(R.id.topAppBar)
        val recipesRecyclerView: RecyclerView = findViewById(R.id.recyclerViewRecipes)
        recipesRecyclerView.layoutManager = LinearLayoutManager(this)

        val editTextQuery: EditText = findViewById(R.id.editTextQuery)
        val buttonSearch: Button = findViewById(R.id.buttonSearch)
        val recipeCall = RecipeCall(this)

        topAppBar.setNavigationOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("fragShow", "log")
            startActivity(intent)
        }


        buttonSearch.setOnClickListener {
            val query = editTextQuery.text.toString()
            if (query.isNotEmpty()) {
                recipeCall.fetchRecipes(query) { recipes ->
                    recipeList.clear()
                    Log.d("RecipeActivity", "recipeList cleared: $recipeList")
                    recipeList.addAll(recipes)
                    Log.d("RecipeActivity", "recipeList cleared: $recipeList")
                    recipesRecyclerView.adapter?.notifyDataSetChanged()
                }
            } else {
                Toast.makeText(this, "Please enter a query", Toast.LENGTH_SHORT).show()
            }
        }

        recipesRecyclerView.adapter = RecipeAdapter(recipeList) { recipe ->
            eatenFoods.add(recipe)
            addCalories(recipe)
            Toast.makeText(this, "${recipe.name} added to eaten foods", Toast.LENGTH_SHORT).show()
        }

        fetchEatenFoods()
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
                        val recipe = document.toObject(Recipe::class.java)
                        eatenFoods.add(recipe)
                        totalCalories += recipe.calorie
                    }
                    dailyCaloriesList.clear()
                    dailyCaloriesList.add(DailyCalories(today, totalCalories))
                    Log.d("LogActivity", "Total Calories $totalCalories")
                }
                .addOnFailureListener { e ->
                    Log.e("LogActivity", "Error fetching eaten foods", e)
                }
        }
    }
}

class RecipeAdapter(private val recipeList: List<Recipe>,
                    private val onRecipeClicked: (Recipe) -> Unit) :
    RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageViewRecipe)
        val nameTextView: TextView = view.findViewById(R.id.textViewRecipeName)
        val caloriesTextView: TextView = view.findViewById(R.id.textViewCalories)
        val carbohydratesTextView: TextView = view.findViewById(R.id.textViewCarbohydrate)
        val proteinTextView: TextView = view.findViewById(R.id.textViewProtein)
        val addButton: Button = view.findViewById(R.id.buttonEaten)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipeList[position]
        holder.nameTextView.text = recipe.name
        holder.caloriesTextView.text = "Calories: ${recipe.calorie}"
        holder.carbohydratesTextView.text = "Carbohydrates: ${recipe.carbohydrates}"
        holder.proteinTextView.text = "Protein: ${recipe.protein}"
        Glide.with(holder.imageView.context).load(recipe.image).into(holder.imageView)
        holder.addButton.setOnClickListener{ onRecipeClicked(recipe)}
    }

    override fun getItemCount() = recipeList.size
}
class RecipeCall(val context: Context) {

    fun fetchRecipes(query: String, onRecipesFetched: (List<Recipe>) -> Unit) {
        val url = "https://spoonacular-recipe-food-nutrition-v1.p.rapidapi.com/recipes/complexSearch?query=$query&addRecipeNutrition=true"

        val stringRequest = object : StringRequest(
            Method.GET, url,
            Response.Listener { response ->
                val jsonObject = JSONObject(response)
                val results = jsonObject.getJSONArray("results")
                val localRecipeList = ArrayList<Recipe>() // Create a new list for each call
                for (i in 0 until results.length()) {
                    val result = results.getJSONObject(i)
                    val id = result.getString("id")
                    val title = result.getString("title")
                    val image = result.getString("image")
                    val nutrition = result.optJSONObject("nutrition")
                    val calories = nutrition?.getJSONArray("nutrients")?.findNutrientAmount("Calories") ?: 0
                    val carbohydrates = nutrition?.getJSONArray("nutrients")?.findNutrientAmount("Carbohydrates") ?: 0
                    val protein = nutrition?.getJSONArray("nutrients")?.findNutrientAmount("Protein") ?: 0
                    localRecipeList.add(Recipe(id, title, calories, carbohydrates, protein, image))
                }
                onRecipesFetched(localRecipeList) // Pass the new list to the callback
            },
            Response.ErrorListener { error ->
                Log.e("RecipeCall", "Failed to fetch recipes: $error")
                Toast.makeText(context, "Error fetching recipes", Toast.LENGTH_SHORT).show()
            }) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["X-RapidAPI-Key"] = "462895ac8emshb93a880bc9aac7fp1ca9dfjsn3a8ff0c341ee"
                headers["X-RapidAPI-Host"] = "spoonacular-recipe-food-nutrition-v1.p.rapidapi.com"
                return headers
            }
        }

        Volley.newRequestQueue(context).add(stringRequest)
    }

    private fun JSONArray.findNutrientAmount(nutrientName: String): Int? {
        for (i in 0 until this.length()) {
            val nutrient = this.getJSONObject(i)
            if (nutrient.getString("name") == nutrientName) {
                return nutrient.getInt("amount")
            }
        }
        return null
    }
}