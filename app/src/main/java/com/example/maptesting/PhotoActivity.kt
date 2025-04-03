package com.example.maptesting

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import java.io.File



class PhotoActivity : AppCompatActivity() {

    lateinit var imageView: ImageView
    lateinit var picButton: Button
    val REQUEST_IMAGE_CAPTURE = 100
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)
        val backButton = findViewById<ImageButton>(R.id.back_button)
        imageView = findViewById(R.id.imageView)
        picButton = findViewById(R.id.btn_take_picture)


        picButton.setOnClickListener { {

            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            try {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }catch(e: ActivityNotFoundException){
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            }

        } }
        backButton.setOnClickListener {
            val intent = Intent(this, ChallengeCreateActivity::class.java)
            startActivity(intent)
            //if(uri_save != null) { //if the uri save exists, bring it with back to the challenge page
            //   intent.putExtra("uri_save", uri_save.toString())
            //}
            finish()
        }
        getPermissions()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(imageBitmap)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }


        fun getPermissions() {
            var permissionsList = mutableListOf<String>()

            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(Manifest.permission.CAMERA)
            }
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            if (permissionsList.size > 0) {
                requestPermissions(permissionsList.toTypedArray(), 101)
            }
        }

        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            grantResults.forEach {
                if (it != PackageManager.PERMISSION_GRANTED) {
                    getPermissions()
                }
            }
        }

        private fun startChallengeActivity(file: File) {
            val intent = Intent(this, ChallengeCreateActivity::class.java)
            intent.putExtra("Photo", file.toUri().toString())
            startActivity(intent)
        }


    }


