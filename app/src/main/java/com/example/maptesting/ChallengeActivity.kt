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
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.net.URI
import java.text.SimpleDateFormat
import java.util.Date

class ChallengeActivity : AppCompatActivity() {
    val firestoreClient = FirestoreClient()
    lateinit var currentPhotoPath: String
    lateinit var imageView: ImageView
    private lateinit var challenge: Challenge
    private lateinit var user: User
    private lateinit var uriSave: Uri
    val REQUEST_IMAGE_CAPTURE = 100
    /*
        username = "Test",
        bio = "test Bio",
        mainLocation = "St. Peter, MN",
        password = "TestPassword"
    )*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_challenge)
        val submitButton = findViewById<Button>(R.id.submit_button)
        val title_text = findViewById<TextInputEditText>(R.id.title_input)
        val desc_text = findViewById<TextInputEditText>(R.id.textInputEditText)


        // Get latitude & longitude from intent thingy
        val latitude = intent.getDoubleExtra("LATITUDE", 0.0)
        val longitude = intent.getDoubleExtra("LONGITUDE", 0.0)

        //get pic uri from intent

        //this doesnt do anything
        uriSave = R.drawable.back_arrow.toString().toUri()
        //this should work
        if(intent.getStringExtra("Photo") != null){
            uriSave = intent.getStringExtra("Photo")!!.toUri()
        }

        //// ------- Extra stuff if we wanna display the coordinates
        // val locationTextView: TextView = findViewById(R.id.location_info)
        // locationTextView.text = "Challenge Location:\nLatitude: $latitude \nLongitude: $longitude"


        // Back button
        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    

        val photoView = findViewById<Button>(R.id.picture_view)
        photoView.setOnClickListener {

            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            dispatchTakePictureIntent()
            try {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }catch(e: ActivityNotFoundException){
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            }
        }


        imageView = findViewById(R.id.pic_display)
        val pick_photo = findViewById<Button>(R.id.btn_take_picture)
        imageView.setImageURI(uriSave)
        val pickMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                // Callback is invoked after the user selects a media item or closes the
                // photo picker.
                if (uri != null) {
                    Log.d("PhotoPicker", "Selected URI: $uri")
                    imageView.setImageURI(uri)
                    uri.toFile().readBytes()
                    uriSave = uri

                } else {
                    Log.d("PhotoPicker", "No media selected")
                }


            }
        pick_photo.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        submitButton.setOnClickListener {
            challenge = Challenge(
                creatorId = user.id,
                title = title_text.text.toString(),
                desc = desc_text.text.toString(),
                photo = uriSave.toFile(),
                lat = latitude,
                lng = longitude
            )


            lifecycleScope.launch {
                firestoreClient.insertChallenge(challenge).collect { id ->
                    challenge = challenge.copy(id = id ?: "")
                }
                firestoreClient.updateChallenge(challenge).collect { result ->
                    println(result)
                }

                //get code
                /*
                user = firestoreClient.getUser(user.username).collect{ result ->
                    if (result!= null){
                        printLn("user got")
                        //id = user.id
                        //username = user.username
                        //etc
                    }
                    else{
                        println("no user")
                    }
                }
                */
            }

            //var newChal: Challenge
            //newChal = Challenge(title_text.text.toString(), uriSave, desc_text.text.toString())


            //I think there was an issue with how this was setup so I changed it a little
            //submitButton.setOnClickListener {
            //  val newChal = Challenge(
            //    title_text.text.toString(), uriSave, desc_text.text.toString(), latitude, longitude
            //)
            //challenges.add(newChal)
            //}


        }
        /*
fun setTitle(context: Context, title: String) {
    val prefs = context.getSharedPreferences("myAppPackage", 0)
    val editor = prefs.edit();
    editor.putString("Title", title);
    editor.apply();
}
fun getTitle(context: Context): String? {
    val prefs = context.getSharedPreferences("myAppPackage", 0)
    return prefs.getString("username", "")
}*/


    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(imageBitmap)
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
}
