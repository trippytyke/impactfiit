package com.uol.impactfiit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class ViewRoutineFragment:Fragment(R.layout.fragment_viewroutine) {
    //Declare the view model and the list of view routines
    private val viewModel: ListViewModel by activityViewModels()
    private val viewRoutineList = ArrayList<ViewRoutine>()
    private var routineName: String? = null
    private lateinit var adapter: ViewRoutineAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        routineName = requireActivity().intent.getStringExtra("routineName")
        adapter = ViewRoutineAdapter(viewRoutineList, routineName!!)

        //Set the layout manager
        val layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        //Get the recycler view
        val recyclerView = view.findViewById<RecyclerView>(R.id.routineRecyclerView)

        // ============= FIREBASE ===========
        val currentUser = Firebase.auth.currentUser
        val uid = currentUser?.uid
        val db = Firebase.firestore
        // ==================================

        val docRef = db.collection("users").document(uid!!).collection("routines").document(routineName!!).collection("exercises")
        docRef.get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val exercise = ViewRoutine(document.data["name"].toString(), document.id, document.data["set"].toString(), document.data["rep"].toString(), document.data["weight"].toString(), document.data["gifUrl"].toString())
                    viewRoutineList.add(exercise)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
        //Set the layout manager and the adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                this.context,
                layoutManager.orientation
            )
        )

    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun refreshData() { //Get the data from the view model and add it to the recycler view
        val exercise = viewModel.viewRoutineList
        for (i in 0 until exercise.size) {
            viewRoutineList.add(ViewRoutine(exercise[i].name, exercise[i].id, exercise[i].set, exercise[i].rep, exercise[i].weight, exercise[i].gifUrl))
        }
        adapter.notifyDataSetChanged()
        viewModel.clearData()
    }
    companion object {
        private const val ARG_COUNT = "param1"
        fun newInstance(counter: Int?): ViewRoutineFragment {
            val fragment = ViewRoutineFragment()
            val args = Bundle()
            args.putInt(ARG_COUNT, counter!!)
            fragment.arguments = args
            return fragment
        }
}

data class ViewRoutine(val name: String, val id: String, val set: String, val rep: String, val weight: String, val gifUrl: String)
class ViewRoutineAdapter(private var viewRoutineList: MutableList<ViewRoutine>, private val routineName: String) : RecyclerView.Adapter<ViewRoutineAdapter.RoutineViewHolder>() {
    class RoutineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //Get the views
        val textView: TextView = itemView.findViewById(R.id.textView)
        val reps: TextView = itemView.findViewById(R.id.repsEt)
        val sets: TextView = itemView.findViewById(R.id.setsEt)
        val weight: TextView = itemView.findViewById(R.id.weightEt)
        val imageView : ImageView = itemView.findViewById(R.id.exerciseGif)
        val editBtn = itemView.findViewById<Button>(R.id.btnEdit)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_routine_row, parent, false)
        return RoutineViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoutineViewHolder, position: Int) {
        var routine = viewRoutineList[position]
        holder.textView.text = routine.name
        holder.reps.text = routine.rep
        holder.sets.text = routine.set
        holder.weight.text = routine.weight
        Glide.with(holder.imageView).load(routine.gifUrl).into(holder.imageView)


        holder.editBtn.setOnClickListener() {
            val builder = AlertDialog.Builder(holder.itemView.context, R.style.MyDialogTheme)
            builder.setTitle("Edit exercise")
            val inflater = LayoutInflater.from(holder.itemView.context)
            val view = inflater.inflate(R.layout.routine_dialog, null)

            val repsEt = view.findViewById<EditText>(R.id.repsEt)
            val setsEt = view.findViewById<EditText>(R.id.setsEt)
            val weightEt = view.findViewById<EditText>(R.id.weightEt)

            repsEt.setText(routine.rep)
            setsEt.setText(routine.set)
            weightEt.setText(routine.weight)

            builder.setView(view)
            builder.setPositiveButton("Save") { dialog, which ->
                val reps = repsEt.text.toString()
                val sets = setsEt.text.toString()
                val weight = weightEt.text.toString()

                // ============= FIREBASE ===========
                val currentUser = Firebase.auth.currentUser
                val uid = currentUser?.uid
                val db = Firebase.firestore
                // ==================================
                val exerciseMap = hashMapOf(
                    "name" to routine.name,
                    "id" to routine.id,
                    "set" to sets,
                    "rep" to reps,
                    "weight" to weight,
                    "gifUrl" to routine.gifUrl
                )
                val routineNameSafe = routine.name.replace("/", "-")
                db.collection("users").document(uid!!)
                    .collection("routines")
                    .document(routineName!!)
                    .collection("exercises")
                    .document(routineNameSafe)
                    .set(exerciseMap)
                    .addOnSuccessListener {
                        val updatedRoutine = ViewRoutine(routine.name, routine.id, sets, reps, weight, routine.gifUrl)
                        viewRoutineList[position] = updatedRoutine
                        notifyItemChanged(position)
                    }
                    .addOnFailureListener { e -> println("Error writing document: $e") }

            }
            builder.setNegativeButton("Cancel") { dialog, which -> }
            builder.show()
        }
    }
    override fun getItemCount() = viewRoutineList.size
    }
}

