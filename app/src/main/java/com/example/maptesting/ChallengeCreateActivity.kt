package com.example.maptesting

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ChallengeCreateActivity : AppCompatActivity() {
    val firestoreClient = FirestoreClient()
    private lateinit var imageView: ImageView
    private lateinit var imageUrl: String
    private lateinit var imageFile: File
    private lateinit var currentPhotoPath: String
    private val REQUEST_IMAGE_CAPTURE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_challenge_create)

        val submitButton = findViewById<Button>(R.id.submit_button)
        val titleText = findViewById<TextInputEditText>(R.id.title_input)
        val descText = findViewById<TextInputEditText>(R.id.textInputEditText)
        imageView = findViewById(R.id.pic_display)

        val latitude = intent.getDoubleExtra("LATITUDE", 0.0)
        val longitude = intent.getDoubleExtra("LONGITUDE", 0.0)

        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
            finish()
        }

        // Take Photo Button
        val takePhotoButton = findViewById<Button>(R.id.picture_view)
        takePhotoButton.setOnClickListener {
            imageFile = createImageFile()
            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                imageFile
            )

            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

            try {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            } catch (e: Exception) {
                Toast.makeText(this, "Camera error", Toast.LENGTH_SHORT).show()
                Log.e("CameraError", "Failed to launch camera", e)
            }
        }

        // Pick Image Button
        val pickPhotoButton = findViewById<Button>(R.id.ChoosePhoto)
        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                imageView.setImageURI(uri)
                uploadImageToFirebase(uri) { downloadUrl ->
                    imageUrl = downloadUrl
                }
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }
        pickPhotoButton.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        submitButton.setOnClickListener {
            val title = titleText.text.toString()
            val desc = descText.text.toString()
            val currentUser = FirebaseAuth.getInstance().currentUser
            val email = currentUser?.email
            val db = FirebaseFirestore.getInstance()

            if (!::imageUrl.isInitialized) {
                Toast.makeText(this, "Please upload a photo first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.collection("Users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val doc = documents.documents[0]
                        val username = doc.getString("username") ?: "unknown_user"
                        val userDocId = doc.id

                        val challenge = Challenge(
                            title = title,
                            desc = desc,
                            photo = imageUrl,
                            lat = latitude,
                            lng = longitude,
                            creatorId = username
                        )

                        lifecycleScope.launch {
                            firestoreClient.insertChallenge(challenge).collect { id ->
                                if (id == null) return@collect

                                val updatedChallenge = challenge.copy(id = id)
                                firestoreClient.updateChallenge(updatedChallenge).collect { _ ->

                                    db.collection("Users").document(userDocId)
                                        .update("completedChallenge", FieldValue.arrayUnion(id))
                                        .addOnSuccessListener {
                                            startActivity(Intent(this@ChallengeCreateActivity, MapActivity::class.java))
                                            finish()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(this@ChallengeCreateActivity, "Challenge created but not marked complete.", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "Username not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to fetch user info", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageUri = Uri.fromFile(File(currentPhotoPath))
            imageView.setImageURI(imageUri)

            uploadImageToFirebase(imageUri) { downloadUrl ->
                imageUrl = downloadUrl
            }
        }
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri?, onSuccess: (String) -> Unit) {
        if (imageUri == null) return

        val storageRef = FirebaseStorage.getInstance().reference
        val fileName = "challenges/${UUID.randomUUID()}.jpg"
        val imageRef = storageRef.child(fileName)

        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri.toString())
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseUpload", "Upload failed", e)
                Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
            }
    }
}
