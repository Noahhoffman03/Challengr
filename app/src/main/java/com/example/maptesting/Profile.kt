package com.example.maptesting

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile) // Ensure this matches your XML filename

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val usernameInput = findViewById<EditText>(R.id.title_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)
        val loginButton = findViewById<Button>(R.id.login_button)
        val signupButton = findViewById<Button>(R.id.signup_button)

        loginButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(username, password)
        }

        signupButton.setOnClickListener {
            val intent = Intent(this, SignupPage::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser(username: String, password: String) {
        db.collection("users").whereEqualTo("username", username).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "Username not found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val userId = documents.documents[0].id
                auth.signInWithEmailAndPassword("$username@dummy.com", password) // Firebase requires an email format
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error logging in. Try again!", Toast.LENGTH_SHORT).show()
            }
    }
}
