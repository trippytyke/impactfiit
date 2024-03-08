package com.uol.impactfiit

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

data class Routine(val name: String)

class RoutineAdapter(private var routineList: List<Routine>) : RecyclerView.Adapter<RoutineAdapter.RoutineViewHolder>() {
    class RoutineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textView)
        val btnView: androidx.appcompat.widget.AppCompatButton = itemView.findViewById(R.id.btnView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.routine_row, parent, false)
        return RoutineViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoutineViewHolder, position: Int) {
        val routine = routineList[position]
        holder.textView.text = routine.name
        holder.btnView.setOnClickListener {
            val intent = Intent(holder.btnView.context, ViewRoutineActivity::class.java)
            intent.putExtra("routineName", routine.name)
            holder.btnView.context.startActivity(intent)
        }

    }

    override fun getItemCount() = routineList.size
}

class RoutineActivity : AppCompatActivity(){
    private val routineList = ArrayList<Routine>()
    private val adapter = RoutineAdapter(routineList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_routine)

        //Get the views
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        val recyclerView = findViewById<RecyclerView>(R.id.routineRecyclerView)
        val topAppBar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.topAppBar)

        // ============= FIREBASE ===========
        val currentUser = Firebase.auth.currentUser
        val uid = currentUser?.uid
        val db = Firebase.firestore
        // ==================================

        //Get the routines from the database
        val docRef = db.collection("users").document(uid!!).collection("routines")
        docRef.get()
            .addOnSuccessListener { result ->
                Log.d(TAG, "DocumentSnapshot successfully retrieved!")
                Log.d(TAG, "Size of result: ${result.size()}")
                for (document in result) {
                    Log.d(TAG, "Document ID: ${document.id}, Document Data: ${document.data}")
                    val routine = Routine(document.id)
                    routineList.add(routine)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                this,
                layoutManager.orientation
            )
        )
        Log.d(TAG, "RecyclerView set up with layoutManager and adapter")



        fab.setOnClickListener {
            val builder = AlertDialog.Builder(this, R.style.MyDialogTheme)
            builder.setTitle("Add routine")

            //Set the custom layout
            val customLayout: View = layoutInflater.inflate(R.layout.add_routine_dialog, null)
            builder.setView(customLayout)

            //Button to save the routine
            builder.setPositiveButton("Save") { dialog: DialogInterface?, which: Int ->
                //Send the user to the view routine page with the routine name
                val editText = customLayout.findViewById<EditText>(R.id.editText)
                val routineName = editText.text.toString()
                val intent = Intent(this, ViewRoutineActivity::class.java)
                db.collection("users")
                    .document(uid).collection("routines")
                    .document(routineName)
                    .set(hashMapOf("routineName" to routineName))
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "New routine created",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            this,
                            "Error adding new routine",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                intent.putExtra("routineName", routineName)
                startActivity(intent)
            }

            //Create the AlertDialog and show it
            val dialog = builder.create()
            dialog.show()
        }
        //Set the top app bar navigation icon to go back to the previous activity
        topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }
}