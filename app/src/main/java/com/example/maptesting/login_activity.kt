package com.example.maptesting

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class login_activity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_page)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

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

            // Check Firestore for the username and retrieve the associated email
            firestore.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val userDoc = documents.documents[0]
                        val email = userDoc.getString("email")

                        if (email != null) {
                            // Authenticate with FirebaseAuth
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this, MainActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            Toast.makeText(this, "Username not found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Username not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error connecting to database", Toast.LENGTH_SHORT).show()
                }
        }

        signupButton.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }
}
