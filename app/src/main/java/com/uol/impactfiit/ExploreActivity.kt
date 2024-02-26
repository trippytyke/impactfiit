package com.uol.impactfiit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.*
import com.android.volley.toolbox.*
import com.google.android.material.appbar.MaterialToolbar
import org.json.JSONArray
import java.util.Locale

data class Exercise(val name: String, val id: String, val gifUrl: String, val targetMuscle: String, val bodyPart: String, val equipment: String, val instructions: String)

class ExerciseAdapter(private var exerciseList: List<Exercise>) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {
    class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textView)
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

        // Set the OnClickListener
        holder.itemView.setOnClickListener {
            // This code will be executed when the item is clicked
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explore)
        val searchView = findViewById<androidx.appcompat.widget.SearchView>(R.id.searchView)
        val topAppBar = findViewById<MaterialToolbar>(R.id.topAppBar)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val exerciseList = ArrayList<Exercise>()
        val adapter = ExerciseAdapter(exerciseList)
        recyclerView.adapter = adapter


        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(text: String?): Boolean {
                val filteredList = ArrayList<Exercise>()
                // running a for loop to compare elements.
                for (item in exerciseList) {
                    // checking if the entered string matched with any item of our recycler view.
                    if (item.name.lowercase().contains(text!!.lowercase(Locale.getDefault()))) {
                        // if the item is matched we are
                        // adding it to our filtered list.
                        filteredList.add(item)
                    }
                }
                if (filteredList.isEmpty()) {
                    Toast.makeText(this@ExploreActivity, "No Exercise Found", Toast.LENGTH_SHORT).show()
                } else {
                    adapter.filter(filteredList)
                }
                return false
            }
        })


        val cache = DiskBasedCache(cacheDir, 1024 * 1024) // 1MB cap
        val network = BasicNetwork(HurlStack())
        val requestQueue = RequestQueue(cache, network).apply { start() }
        val URL = "https://exercisedb.p.rapidapi.com/exercises?limit=10"

        val stringRequest = object : StringRequest(Request.Method.GET, URL,
            Response.Listener<String> { response ->
                val jsonArray = JSONArray(response)
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val name = jsonObject.getString("name")
                    val id = jsonObject.getString("id")
                    val gifUrl = jsonObject.getString("gifUrl")
                    val targetMuscle = jsonObject.getString("target")
                    val bodyPart = jsonObject.getString("bodyPart")
                    val equipment = jsonObject.getString("equipment")
                    val instructions = jsonObject.getString("instructions")
                    Log.d("ExploreActivity", "Name: $name, ID: $id")
                    exerciseList.add(Exercise(name, id, gifUrl, targetMuscle, bodyPart, equipment, instructions))
                }
                adapter.notifyDataSetChanged()
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
            }) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["X-RapidAPI-Key"] = "9176070d3bmsh154ab0fef695c0bp19d560jsn1acc0fedad71"
                headers["X-RapidAPI-Host"] = "exercisedb.p.rapidapi.com"
                return headers
            }
        }
        requestQueue.add(stringRequest)

        topAppBar.setNavigationOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("fragShow", "workout")
            startActivity(intent)
        }
    }



}