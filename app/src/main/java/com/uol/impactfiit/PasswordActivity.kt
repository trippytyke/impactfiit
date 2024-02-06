package com.uol.impactfiit

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import nu.aaro.gustav.passwordstrengthmeter.PasswordStrengthCalculator
import nu.aaro.gustav.passwordstrengthmeter.PasswordStrengthLevel
import nu.aaro.gustav.passwordstrengthmeter.PasswordStrengthMeter


class PasswordActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password)

        auth = Firebase.auth

        val email = intent.getStringExtra("email")
        val passwordTxt = findViewById<AppCompatEditText>(R.id.passwordEt)
        val cfmPasswordTxt = findViewById<AppCompatEditText>(R.id.cfmPasswordEt)
        val continueButton = findViewById<AppCompatButton>(R.id.continueBtn)
        val meter = findViewById<PasswordStrengthMeter>(R.id.passwordInputMeter)

        meter.setEditText(passwordTxt)

        continueButton.setAlpha(0.5f)
        continueButton.setEnabled(false)

        fun toastMsg(msg: String?) {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        var textWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // this function is called before text is edited
            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // this function is called when text is edited
                if (passwordTxt.text.toString() != cfmPasswordTxt.text.toString()) {
                    cfmPasswordTxt.error = "Passwords do not match"
                }
            }
            override fun afterTextChanged(s: Editable) {
                // this function is called after text is edited
            }
        }

        meter.setStrengthLevels(
            arrayOf(
                PasswordStrengthLevel("Too short", android.R.color.white),
                PasswordStrengthLevel("Weak", android.R.color.holo_red_light),
                PasswordStrengthLevel("Good", android.R.color.holo_orange_light),
                PasswordStrengthLevel("Strong", android.R.color.holo_green_light)
            )
        )

        meter.setPasswordStrengthCalculator(object : PasswordStrengthCalculator {
            override fun calculatePasswordSecurityLevel(password: String): Int {
                return when {
                    password.length < 6 -> 0
                    password.length < 8 -> 1
                    password.length < 10 -> 2
                    else -> 3
                }
            }

            override fun getMinimumLength(): Int {
                return 6
            }

            override fun passwordAccepted(level: Int): Boolean {
                return level > 0
            }

            override fun onPasswordAccepted(password: String) {
                continueButton.setAlpha(1f);
                continueButton.setEnabled(true)
            }
        })

        cfmPasswordTxt.addTextChangedListener(textWatcher)
        continueButton.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)

            if (passwordTxt.text.toString() != cfmPasswordTxt.text.toString()) {
                toastMsg("Passwords do not match")
                return@setOnClickListener
            }
            var password = passwordTxt.text.toString()

            auth.createUserWithEmailAndPassword(email!!, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")
                        val user = auth.currentUser
                        startActivity(intent)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                            this,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
        }


    }
}