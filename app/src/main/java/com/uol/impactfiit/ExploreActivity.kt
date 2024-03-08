package com.uol.impactfiit

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.*
import com.android.volley.toolbox.*
import com.google.android.material.appbar.MaterialToolbar
import org.json.JSONArray
import java.util.Locale
import com.bumptech.glide.Glide

data class Exercise(val name: String, val id: String, val gifUrl: String, val targetMuscle: String, val bodyPart: String,
                    val equipment: String, val instructions: String)

class ExerciseAdapter(private var exerciseList: List<Exercise>) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {
    class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) { //ViewHolder class
        val textView: TextView = itemView.findViewById(R.id.textView)
        val textViewTwo: TextView = itemView.findViewById(R.id.textView2)
        val imageView: ImageView = itemView.findViewById(R.id.exerciseGif)
    }

    fun filter(filterList: List<Exercise>) {
        exerciseList = filterList
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.text_row_item, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exercise = exerciseList[position]
        holder.textView.text = exercise.name
        holder.textViewTwo.text = exercise.targetMuscle
        Glide.with(holder.imageView).load(exercise.gifUrl).into(holder.imageView);


        //Set the OnClickListener
        holder.itemView.setOnClickListener {

            Toast.makeText(holder.itemView.context, "Clicked on ${exercise.name}", Toast.LENGTH_SHORT).show()
            val intent = Intent(holder.itemView.context, ExerciseActivity::class.java)
            intent.putExtra("exerciseName", exercise.name)
            intent.putExtra("exerciseId", exercise.id)
            intent.putExtra("exerciseGifUrl", exercise.gifUrl)
            intent.putExtra("exerciseTargetMuscle", exercise.targetMuscle)
            intent.putExtra("exerciseBodyPart", exercise.bodyPart)
            intent.putExtra("exerciseEquipment", exercise.equipment)
            intent.putExtra("exerciseInstructions", exercise.instructions)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = exerciseList.size
}
class ExploreActivity : AppCompatActivity() {
    private val exerciseList = ArrayList<Exercise>()
    private val adapter = ExerciseAdapter(exerciseList)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explore)

        //Get the views of the activity
        val searchView = findViewById<androidx.appcompat.widget.SearchView>(R.id.searchView)
        val topAppBar = findViewById<MaterialToolbar>(R.id.topAppBar)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        //Set the layout manager
        recyclerView.layoutManager = LinearLayoutManager(this)
        //Set the adapter
        recyclerView.adapter = adapter

        //Calls the getExercises function to get the exercises from the API
        getExercises()

        //When user types in the search view, filter the exercises list
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(text: String?): Boolean {
                val filteredList = ArrayList<Exercise>()
                for (item in exerciseList) {
                    //Checking if the entered string matched with any item of our recycler view.
                    if (item.name.lowercase().contains(text!!.lowercase(Locale.getDefault()))) {
                        filteredList.add(item)
                    }
                }
                if (filteredList.isEmpty()) {
                    //If the filtered list is empty, display a message
                    adapter.filter(filteredList)
                    adapter.notifyDataSetChanged()
                    Toast.makeText(this@ExploreActivity, "No Exercise Found", Toast.LENGTH_SHORT).show()
                } else {
                    adapter.filter(filteredList)
                }
                return false
            }
        })

        //Set the top app bar navigation icon to go back to the previous activity
        topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun getExercises() { //Function to get the exercises from the API
        val cache = DiskBasedCache(cacheDir, 1024 * 1024) // 1MB cap
        val network = BasicNetwork(HurlStack())
        val requestQueue = RequestQueue(cache, network).apply { start() }
        //API URL
        val URL = "https://exercisedb.p.rapidapi.com/exercises?limit=10"

        //Request to get the exercises from the API
        val stringRequest = object : StringRequest(Request.Method.GET, URL,
            Response.Listener<String> { response ->
                val jsonArray = JSONArray(response)
                for (i in 0 until jsonArray.length()) {
                    //Get the exercise details from the JSON response
                    val jsonObject = jsonArray.getJSONObject(i)
                    val name = jsonObject.getString("name")
                    val id = jsonObject.getString("id")
                    val gifUrl = jsonObject.getString("gifUrl")
                    val targetMuscle = jsonObject.getString("target")
                    val bodyPart = jsonObject.getString("bodyPart")
                    val equipment = jsonObject.getString("equipment")
                    val instructions = jsonObject.getString("instructions")
                    //Add the exercise to the exerciseList
                    exerciseList.add(Exercise(name, id, gifUrl, targetMuscle, bodyPart, equipment, instructions))
                }
                //Notify the adapter that the data has changed
                adapter.notifyDataSetChanged()
            },
            Response.ErrorListener { error ->
                //Display an error message if the request fails
                Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
            }) {
            override fun getHeaders(): Map<String, String> { //Set the headers for the request
                val headers = HashMap<String, String>()
                headers["X-RapidAPI-Key"] = "9176070d3bmsh154ab0fef695c0bp19d560jsn1acc0fedad71"
                headers["X-RapidAPI-Host"] = "exercisedb.p.rapidapi.com"
                return headers
            }
        }
        //Add the request to the requestQueue
        requestQueue.add(stringRequest)
    }
}