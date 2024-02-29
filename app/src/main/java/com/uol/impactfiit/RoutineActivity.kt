package com.uol.impactfiit

import android.content.ContentValues
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

data class Routine(val name: String, val id: String)

class RoutineAdapter(private var routineList: List<Routine>) : RecyclerView.Adapter<RoutineAdapter.RoutineViewHolder>() {
    class RoutineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.routine_row, parent, false)
        return RoutineViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoutineViewHolder, position: Int) {
        val routine = routineList[position]
        holder.textView.text = routine.name

    }

    override fun getItemCount() = routineList.size
}

class RoutineActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_routine)

        val currentUser = Firebase.auth.currentUser //Get current logged in user
        val uid = currentUser?.uid //Get the user id
        val db = Firebase.firestore //Get the firestore database

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        val recyclerView = findViewById<RecyclerView>(R.id.routineRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val routineList = ArrayList<Routine>()
        val adapter = RoutineAdapter(routineList)
        recyclerView.adapter = adapter

        fab.setOnClickListener {
            val builder = AlertDialog.Builder(this, R.style.MyDialogTheme)
            builder.setTitle("Add routine")

            // set the custom layout
            val customLayout: View = layoutInflater.inflate(R.layout.add_routine_dialog, null)
            builder.setView(customLayout)

            // add a button
            builder.setPositiveButton("Save") { dialog: DialogInterface?, which: Int ->
                // send data from the AlertDialog to the Activity
                val editText = customLayout.findViewById<EditText>(R.id.editText)
                val routineName = editText.text.toString()
                val intent = Intent(this, ViewRoutineActivity::class.java)
                intent.putExtra("routineName", routineName)
                startActivity(intent)
            }
            // create and show the alert dialog
            val dialog = builder.create()
            dialog.show()
        }
    }
}