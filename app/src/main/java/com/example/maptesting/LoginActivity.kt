package com.example.maptesting

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var signUpRedirectButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()


        if (auth.currentUser != null) {
            Log.d("AuthCheck", "User already logged in: ${auth.currentUser?.email}")
            startActivity(Intent(this, MapActivity::class.java))
            finish()
        } else {
            Log.d("AuthCheck", "No user logged in, showing login screen")
            setContentView(R.layout.activity_login)

            emailEditText = findViewById(R.id.emailEditText)
            passwordEditText = findViewById(R.id.passwordEditText)
            loginButton = findViewById(R.id.login_button)
            signUpRedirectButton = findViewById(R.id.signupRedirect_button)

            loginButton.setOnClickListener {
                val email = emailEditText.text.toString().trim()
                val password = passwordEditText.text.toString().trim()

                if (email.isNotEmpty() && password.isNotEmpty()) {
                    loginUser(email, password)
                } else {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            }

            signUpRedirectButton.setOnClickListener {
                startActivity(Intent(this, SignupActivity::class.java))
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, DisclaimerActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    Log.w("Login", "Login failed", task.exception)
                }
            }
    }
}
