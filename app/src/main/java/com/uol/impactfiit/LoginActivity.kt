package com.uol.impactfiit

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.*

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        // Initialize Firebase Auth
        auth = Firebase.auth
        val loginButton = findViewById<AppCompatButton>(R.id.loginBtn)
        val backButton = findViewById<AppCompatButton>(R.id.backBtn)
        val emailEditText = findViewById<AppCompatEditText>(R.id.emailEt)
        val passwdEditText = findViewById<AppCompatEditText>(R.id.passwordEt)



        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwdEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                val d = AlertDialog.Builder(this)
                d.setTitle("Error")
                d.setMessage("Please fill in all the fields")
                d.setPositiveButton("OK") { dialogInterface, i -> }
                val alertDialog = d.create()
                alertDialog.show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")
                        val user = auth.currentUser
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext,
                            "Authentication failed/",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
        }

        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }
}