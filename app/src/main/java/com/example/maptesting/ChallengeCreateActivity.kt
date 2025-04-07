package com.example.maptesting

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class ChallengeCreateActivity : AppCompatActivity() {
    val firestoreClient = FirestoreClient()
    lateinit var imageView: ImageView
    private lateinit var challenge: Challenge
    private lateinit var user: User
    private lateinit var imageUrl: String
    private lateinit var imageFile: File
    val REQUEST_IMAGE_CAPTURE = 100
    val URL_PATH = "gs://challengr-1be1f.firebasestorage.app/challenges/"
    //private lateinit var imageAccessCode
    lateinit var currentPhotoPath: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_challenge_create)
        val submitButton = findViewById<Button>(R.id.submit_button)
        val title_text = findViewById<TextInputEditText>(R.id.title_input)
        val desc_text = findViewById<TextInputEditText>(R.id.textInputEditText)


        // Get latitude & longitude from intent thingy
        val latitude = intent.getDoubleExtra("LATITUDE", 0.0)
        val longitude = intent.getDoubleExtra("LONGITUDE", 0.0)

        //// ------- Extra stuff if we wanna display the coordinates
        // val locationTextView: TextView = findViewById(R.id.location_info)
        // locationTextView.text = "Challenge Location:\nLatitude: $latitude \nLongitude: $longitude"


        // Back button
        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
            finish()
        }


        val photoView = findViewById<Button>(R.id.picture_view)
        photoView.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            //dispatchTakePictureIntent()
            imageFile = createImageFile()
            try {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            }
        }


        imageView = findViewById(R.id.pic_display)
        //imageView.setImageURI(uriSave)
        val pick_photo = findViewById<Button>(R.id.btn_take_picture)
        val pickMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                // Callback is invoked after the user selects a media item or closes the
                // photo picker.
                if (uri != null) {
                    Log.d("PhotoPicker", "Selected URI: $uri")
                    imageView.setImageURI(uri)
                    //imageFile = uri.toFile()
                    imageUrl = URL_PATH+uri.toString()+".png"//+ imageAccessCode //also find a way to get a file|path from this
                    //does just adding.png work? maybe

                } else {
                    Log.d("PhotoPicker", "No media selected")
                }


            }
        pick_photo.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        submitButton.setOnClickListener {
            val title = title_text.text.toString()
            val desc = desc_text.text.toString()

            val currentUser = FirebaseAuth.getInstance().currentUser
            val email = currentUser?.email

            if (email == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = FirebaseFirestore.getInstance()

            db.collection("Users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val doc = documents.documents[0]
                        val username = doc.getString("username") ?: "unknown_user"
                        val userDocId = doc.id

                        // Build challenge object
                        challenge = Challenge(
                            title = title,
                            desc = desc,
                            photo = imageUrl,
                            lat = latitude,
                            lng = longitude,
                            creatorId = username
                        )

                        // Insert and update challenge
                        lifecycleScope.launch {
                            firestoreClient.insertChallenge(challenge).collect { id ->
                                if (id == null) {
                                    Log.e("Challenge", "Challenge insertion returned null ID.")
                                    return@collect
                                }

                                val updatedChallenge = challenge.copy(id = id)

                                firestoreClient.updateChallenge(updatedChallenge).collect { result ->
                                    Log.d("Challenge", "Challenge update result: $result")

                                    // Add challenge ID to user's completedChallenges
                                    db.collection("Users").document(userDocId)
                                        .update("completedChallenges", FieldValue.arrayUnion(id))
                                        .addOnSuccessListener {
                                            Log.d("Challenge", "Challenge marked completed for creator")

                                            // Only now navigate back
                                            val intent = Intent(this@ChallengeCreateActivity, MapActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("Challenge", "Failed to update user's completedChallenges", e)
                                            Toast.makeText(this@ChallengeCreateActivity, "Challenge created but not marked complete.", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "Username not found", Toast.LENGTH_SHORT).show()
                        Log.e("Firestore", "No user document found for email: $email")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Failed to fetch username", e)
                    Toast.makeText(this, "Failed to fetch user info", Toast.LENGTH_SHORT).show()
                }
        }



    }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                val imageBitmap = data?.extras?.get("data") as Bitmap
                val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                val fileName =  "${timeStamp}-userImage.png"
                imageFile = bitmapToFile(null, imageBitmap, fileName)!!
                imageView.setImageBitmap(imageBitmap)
                imageUrl = URL_PATH + fileName// + imageAccessCode
                //if (imageUri != null) {
                //  uriSave = imageUri
                //}
            } else {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }

        private fun createImageFile(): File {
            // Create an image file name
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            return File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
            ).apply {
                // Save a file: path for use with ACTION_VIEW intents
                currentPhotoPath = absolutePath
            }
        }

        private fun dispatchTakePictureIntent() {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                // Ensure that there's a camera activity to handle the intent
                takePictureIntent.resolveActivity(packageManager)?.also {
                    // Create the File where the photo should go
                    val photoFile: File? = try {
                        createImageFile()
                    } catch (ex: IOException) {
                        // Error occurred while creating the File
                        null
                    }
                    // Continue only if the File was successfully created
                    photoFile?.also {
                        val photoURI: Uri = FileProvider.getUriForFile(
                            this,
                            "com.example.android.fileprovider",
                            it
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                    }
                }
            }
        }

        private fun galleryAddPic() {
            Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
                val f = File(currentPhotoPath)
                mediaScanIntent.data = Uri.fromFile(f)
                sendBroadcast(mediaScanIntent)
            }
        }

        private fun setPic() {
            // Get the dimensions of the View
            val targetW: Int = imageView.width
            val targetH: Int = imageView.height

            val bmOptions = BitmapFactory.Options().apply {
                // Get the dimensions of the bitmap
                inJustDecodeBounds = true

                BitmapFactory.decodeFile(currentPhotoPath)

                val photoW: Int = outWidth
                val photoH: Int = outHeight

                // Determine how much to scale down the image
                val scaleFactor: Int = Math.max(1, Math.min(photoW / targetW, photoH / targetH))

                // Decode the image file into a Bitmap sized to fill the View
                inJustDecodeBounds = false
                inSampleSize = scaleFactor
                inPurgeable = true
            }
            BitmapFactory.decodeFile(currentPhotoPath, bmOptions)?.also { bitmap ->
                imageView.setImageBitmap(bitmap)
            }
        }

    fun bitmapToFile(
        context: Context?,
        bitmap: Bitmap,
        fileNameToSave: String?
    ): File? { // File name like "image.png"
        //create a file to write bitmap data
        var file: File? = null
        try {
            file = File(
                Environment.getExternalStorageDirectory()
                    .toString() + File.separator + fileNameToSave
            )
            file.createNewFile()

            //Convert bitmap to byte array
            val bos: ByteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos) // YOU can also save it in JPEG
            val bitmapdata: ByteArray? = bos.toByteArray()

            //write the bytes in file
            val fos: FileOutputStream = FileOutputStream(file)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return file // it will return null
        }
    }

}

