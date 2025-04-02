package com.example.maptesting

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class ChallengeActivity : AppCompatActivity() {
    private val firestoreClient = FirestoreClient()
    private lateinit var imageView: ImageView
    private lateinit var challenge: Challenge
    private lateinit var user: User
    private var photoPath: String? = null  // Stores the local file path

    private val REQUEST_IMAGE_CAPTURE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_challenge)

        val submitButton = findViewById<Button>(R.id.submit_button)
        val title_text = findViewById<TextInputEditText>(R.id.title_input)
        val desc_text = findViewById<TextInputEditText>(R.id.textInputEditText)

        val latitude = intent.getDoubleExtra("LATITUDE", 0.0)
        val longitude = intent.getDoubleExtra("LONGITUDE", 0.0)

        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        val photoView = findViewById<Button>(R.id.picture_view)
        photoView.setOnClickListener {
            val intent = Intent(this, PhotoActivity::class.java)
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        }

        imageView = findViewById(R.id.pic_display)

        // Handle selecting a photo from gallery
        val pickPhotoButton = findViewById<Button>(R.id.btn_take_picture)
        val pickMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (uri != null) {
                    Log.d("PhotoPicker", "Selected URI: $uri")
                    imageView.setImageURI(uri)
                    photoPath = uri.toString()
                } else {
                    Log.d("PhotoPicker", "No media selected")
                }
            }
        pickPhotoButton.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        submitButton.setOnClickListener {
            val title = title_text.text.toString()
            val description = desc_text.text.toString()

            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (photoPath != null) {
                uploadImageToFirebase(photoPath!!) { imageUrl ->
                    saveChallengeToFirestore(title, description, imageUrl, latitude, longitude)
                }
            } else {
                saveChallengeToFirestore(title, description, null, latitude, longitude)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            photoPath = data?.getStringExtra("photo_path")

            if (photoPath != null) {
                val file = File(photoPath!!)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(photoPath)
                    imageView.setImageBitmap(bitmap)
                }
            }
        }
    }

    private fun uploadImageToFirebase(filePath: String, callback: (String?) -> Unit) {
        val fileUri = Uri.fromFile(File(filePath))
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("challenge_images/${UUID.randomUUID()}.jpg")

        imageRef.putFile(fileUri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    callback(uri.toString()) // Pass the download URL back
                }
            }
            .addOnFailureListener {
                Log.e("FirebaseUpload", "Image upload failed: ${it.message}")
                Toast.makeText(this, "Image upload failed!", Toast.LENGTH_SHORT).show()
                callback(null)
            }
    }

    private fun saveChallengeToFirestore(
        title: String,
        description: String,
        imageUrl: String?,
        lat: Double,
        lng: Double
    ) {
        challenge = Challenge(
            creatorId = user.id,
            title = title,
            desc = description,
            photo = imageUrl,
            lat = lat,
            lng = lng
        )

        lifecycleScope.launch {
            firestoreClient.insertChallenge(challenge).collect { id ->
                challenge = challenge.copy(id = id ?: "")
            }
            firestoreClient.updateChallenge(challenge).collect { result ->
                println(result)
            }
        }

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
