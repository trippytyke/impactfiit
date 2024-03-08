package com.uol.impactfiit

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        //Get the buttons and edit text in the view
        val registerButton = findViewById<AppCompatButton>(R.id.registerBtn)
        val backButton = findViewById<AppCompatButton>(R.id.backBtn)
        val emailEditText = findViewById<AppCompatEditText>(R.id.emailEt)

        //Regex pattern for email validation
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex()

        //If the user clicks the register button, go to the password page
        registerButton.setOnClickListener {
            val intent = Intent(this, PasswordActivity::class.java)
            val email = emailEditText.text.toString()

            if (email.isEmpty()) { //If the email is empty, show an error message
                emailEditText.error = "Please enter your email address"
                Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!email.matches(emailPattern)) //If the email is invalid, show an error message
            {
                emailEditText.error = "Invalid email address"
                Toast.makeText(this,"Invalid email address", Toast.LENGTH_SHORT).show();
                return@setOnClickListener
            }

            intent.putExtra("email", email)
            startActivity(intent)
        }

        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }



    }
}