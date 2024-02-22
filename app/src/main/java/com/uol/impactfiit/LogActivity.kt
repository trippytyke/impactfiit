package com.uol.impactfiit

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import org.json.JSONObject

data class Recipe(val id: Int, val name: String, val calorie: Int, val image: String)

class LogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewRecipes)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val recipeCall = RecipeCall(this)
        recipeCall.fetchRecipes(recyclerView)

        recyclerView.adapter = RecipeAdapter(recipeCall.recipeList)
    }
}
class RecipeAdapter(private val recipeList: List<Recipe>) :
    RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageViewRecipe)
        val nameTextView: TextView = view.findViewById(R.id.textViewRecipeName)
        val caloriesTextView: TextView = view.findViewById(R.id.textViewCalories)
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
        Glide.with(holder.imageView.context).load(recipe.image).into(holder.imageView)
    }

    override fun getItemCount() = recipeList.size
}
class RecipeCall(val context: Context) {
    private val url = "https://spoonacular-recipe-food-nutrition-v1.p.rapidapi.com/recipes/complexSearch?query=pasta&maxCalories=10000"
    val recipeList = ArrayList<Recipe>()

    fun fetchRecipes(recyclerView : RecyclerView) {
        val stringRequest = object : StringRequest(
            Method.GET, url,
            Response.Listener { response ->
                val jsonObject = JSONObject(response)
                val results = jsonObject.getJSONArray("results")
                for (i in 0 until results.length()) {
                    val result = results.getJSONObject(i)
                    val id = result.getInt("id")
                    val title = result.getString("title")
                    val image = result.getString("image")
                    // for getting the calories
                    // Use optJSONObject to avoid JSONException if "nutrition" key is missing
                    val nutrition = result.optJSONObject("nutrition")
                    val calories = if (nutrition != null) {
                        val nutrients = nutrition.getJSONArray("nutrients")
                        nutrients.getJSONObject(0).getInt("amount")
                    } else {
                        // Set a default value or handle the missing nutrition information
                        0
                    }
                    Log.d("RecipeCall", "Name: $title, Cal: $calories")
                    recipeList.add(Recipe(id, title, calories, image))
                }
                recyclerView.adapter = RecipeAdapter(recipeList)
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
}