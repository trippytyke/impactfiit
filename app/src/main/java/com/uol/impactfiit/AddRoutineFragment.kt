package com.uol.impactfiit

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.activityViewModels
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.StringRequest
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import org.json.JSONArray
import java.util.Locale

class AddRoutineFragment: Fragment(R.layout.fragment_addroutine), AddRoutineAdapter.OnAddButtonClickListener {
    private val viewModel: ListViewModel by activityViewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.routineRecyclerView)
        val currentUser = Firebase.auth.currentUser //Get current logged in user
        val uid = currentUser?.uid //Get the user id
        val db = Firebase.firestore //Get the firestore database
        val searchView = view.findViewById<androidx.appcompat.widget.SearchView>(R.id.searchView)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        val addRoutineList = ArrayList<AddRoutine>()
        val adapter = AddRoutineAdapter(addRoutineList, this)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                this.context,
                layoutManager.orientation
            )
        )

        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(text: String?): Boolean {
                val filteredList = ArrayList<AddRoutine>()
                // running a for loop to compare elements.
                for (item in addRoutineList) {
                    // checking if the entered string matched with any item of our recycler view.
                    if (item.name.lowercase().contains(text!!.lowercase(Locale.getDefault()))) {
                        // if the item is matched we are
                        // adding it to our filtered list.
                        filteredList.add(item)
                    }
                }
                if (filteredList.isEmpty()) {
                    adapter.filter(filteredList)
                    adapter.notifyDataSetChanged()
                    Toast.makeText(requireActivity(), "No Exercise Found", Toast.LENGTH_SHORT).show()
                } else {
                    adapter.filter(filteredList)
                }
                return false
            }
        })


        val cache = DiskBasedCache(requireActivity().cacheDir, 1024 * 1024) // 1MB cap
        val network = BasicNetwork(HurlStack())
        val requestQueue = RequestQueue(cache, network).apply { start() }
        val URL = "https://exercisedb.p.rapidapi.com/exercises?limit=10"

        val stringRequest = object : StringRequest(
            Request.Method.GET, URL,
            Response.Listener<String> { response ->
                val jsonArray = JSONArray(response)
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val name = jsonObject.getString("name")
                    val id = jsonObject.getString("id")
                    val gifUrl = jsonObject.getString("gifUrl")
                    val targetMuscle = jsonObject.getString("target")
                    addRoutineList.add(AddRoutine(name, id, gifUrl, targetMuscle))
                }
                adapter.notifyDataSetChanged()
            },
            Response.ErrorListener { error ->
                Toast.makeText(requireActivity(), error.toString(), Toast.LENGTH_LONG).show()
            }) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["X-RapidAPI-Key"] = "9176070d3bmsh154ab0fef695c0bp19d560jsn1acc0fedad71"
                headers["X-RapidAPI-Host"] = "exercisedb.p.rapidapi.com"
                return headers
            }
        }
        requestQueue.add(stringRequest)
    }
    companion object {
        private const val ARG_COUNT = "param1"
        fun newInstance(counter: Int?): AddRoutineFragment {
            val fragment = AddRoutineFragment()
            val args = Bundle()
            args.putInt(ARG_COUNT, counter!!)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onAddButtonClick(routine: List<ViewRoutineFragment.ViewRoutine>) {
        viewModel.viewRoutineList.addAll(routine)
    }
}

data class AddRoutine(
    val name: String,
    val id: String,
    val gifUrl: String,
    val targetMuscle: String
)

class AddRoutineAdapter(private var addRoutineList: List<AddRoutine>, private val listener: OnAddButtonClickListener) :
    RecyclerView.Adapter<AddRoutineAdapter.RoutineViewHolder>() {

    interface OnAddButtonClickListener {
        fun onAddButtonClick(routine: List<ViewRoutineFragment.ViewRoutine>)
    }
    class RoutineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textView)
        val textViewTwo: TextView = itemView.findViewById(R.id.textViewTwo)
        val imageView: ImageView = itemView.findViewById(R.id.exerciseGif)
        val addButton: Button = itemView.findViewById(R.id.btnAdd)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.add_routine_row, parent, false)
        return RoutineViewHolder(view)
    }
    fun filter(filterList: List<AddRoutine>) {

        addRoutineList = filterList

        notifyDataSetChanged()
    }
    override fun onBindViewHolder(holder: RoutineViewHolder, position: Int) {
        val routine = addRoutineList[position]
        holder.textView.text = routine.name
        holder.textViewTwo.text = routine.targetMuscle
        Glide.with(holder.imageView).load(routine.gifUrl).into(holder.imageView)
        holder.addButton.setOnClickListener {
            val builder = AlertDialog.Builder(holder.itemView.context, R.style.MyDialogTheme)
            builder.setTitle("Add exercise")

            // Inflate the custom layout
            val customLayout: View =
                LayoutInflater.from(holder.itemView.context).inflate(R.layout.routine_dialog, null)
            builder.setView(customLayout)

            // add a button
            builder.setPositiveButton("Add", DialogInterface.OnClickListener { dialog, which ->
                val sets = customLayout.findViewById<EditText>(R.id.setsEt)
                val reps = customLayout.findViewById<EditText>(R.id.repsEt)
                val weights = customLayout.findViewById<EditText>(R.id.weightEt)
                val rep = reps.text.toString()
                val weight = weights.text.toString()
                val set = sets.text.toString()
                val db = Firebase.firestore
                val currentUser = Firebase.auth.currentUser
                val uid = currentUser?.uid
                val exercise = listOf(ViewRoutineFragment.ViewRoutine(routine.name, routine.id, set, rep, weight, routine.gifUrl))
                listener.onAddButtonClick(exercise)
                 /*   if (uid != null) {
                        db.collection("users").document(uid).collection("routines").document(routine.id)
                            .set(exercise)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    holder.itemView.context,
                                    "Exercise added to routine",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    holder.itemView.context,
                                    "Error adding exercise to routine",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }*/
            })
            builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
                dialog.cancel()
            })
            val dialog = builder.create()
            dialog.show()
        }
    }
    override fun getItemCount() = addRoutineList.size
}