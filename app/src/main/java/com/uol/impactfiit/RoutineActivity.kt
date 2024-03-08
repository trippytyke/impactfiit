package com.uol.impactfiit

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
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
                for (document in result) {
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

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deletedRoutine: Routine = routineList[viewHolder.adapterPosition]

                val builder = AlertDialog.Builder(viewHolder.itemView.context)
                builder.setTitle("Confirm deletion")
                builder.setMessage("Are you sure you want to delete " + deletedRoutine.name + "?")

                builder.setPositiveButton("Yes") { dialog, which ->
                    routineList.removeAt(viewHolder.adapterPosition)
                    adapter.notifyItemRemoved(viewHolder.adapterPosition)

                    docRef.document(deletedRoutine.name).delete()

                    Toast.makeText(
                        viewHolder.itemView.context,
                        deletedRoutine.name +" deleted",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                builder.setNegativeButton("No") { dialog, which ->
                    adapter.notifyItemChanged(viewHolder.adapterPosition)
                }

                // Show the dialog
                builder.show()
            }

            override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
                // Increase the return value to slow down the swipe speed
                return defaultValue * 10
            }

            override fun getSwipeVelocityThreshold(defaultValue: Float): Float {
                // Decrease the return value to slow down the swipe speed
                return defaultValue / 10
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )

                val itemView = viewHolder.itemView
                val paint = Paint()
                paint.color = Color.rgb(170, 0, 0)

                val trashIcon = ContextCompat.getDrawable(itemView.context, R.drawable.ic_delete)
                val iconMargin = (itemView.height - trashIcon?.intrinsicHeight!!) / 2

                if (dX > 0) { // Swiping to the right
                    c.drawRect(
                        itemView.left.toFloat(),
                        itemView.top.toFloat(),
                        itemView.left.toFloat() + dX,
                        itemView.bottom.toFloat(),
                        paint
                    )

                    trashIcon.setBounds(itemView.left + iconMargin, itemView.top + iconMargin,
                        itemView.left + iconMargin + trashIcon.intrinsicWidth, itemView.bottom - iconMargin)
                } else if (dX < 0) { // Swiping to the left
                    c.drawRect(
                        itemView.right.toFloat() + dX,
                        itemView.top.toFloat(),
                        itemView.right.toFloat(),
                        itemView.bottom.toFloat(),
                        paint
                    )

                    trashIcon.setBounds(itemView.right - iconMargin - trashIcon.intrinsicWidth, itemView.top + iconMargin,
                        itemView.right - iconMargin, itemView.bottom - iconMargin)
                }

                trashIcon.draw(c)
            }
        }).attachToRecyclerView(recyclerView)

        //Set the top app bar navigation icon to go back to the previous activity
        topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }
}