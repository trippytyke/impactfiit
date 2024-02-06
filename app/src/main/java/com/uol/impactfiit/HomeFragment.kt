package com.uol.impactfiit
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class HomeFragment:Fragment(R.layout.fragment_home) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = Firebase.auth.currentUser
        val uid = currentUser?.uid
        val db = Firebase.firestore

        val docRef = db.collection("users").document(uid!!)
        val nameTxt = view.findViewById<AppCompatTextView>(R.id.userName)

        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val name = document.getString("first")
                    nameTxt.setText(name)
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }
}