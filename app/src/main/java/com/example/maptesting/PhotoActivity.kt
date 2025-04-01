package com.example.maptesting

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Camera
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.MultiResolutionImageReader
import android.media.ImageReader
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.view.TextureView
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import com.google.android.gms.maps.model.LatLng
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class PhotoActivity : AppCompatActivity() {

    lateinit var capReq: CaptureRequest.Builder
    lateinit var handler: Handler
    lateinit var handlerThread: HandlerThread
    lateinit var cameraManager: CameraManager
    lateinit var textureView: TextureView
    lateinit var cameraCaptureSession: CameraCaptureSession
    lateinit var cameraDevice: CameraDevice
    lateinit var captureRequest: CaptureRequest
    lateinit var imageReader: ImageReader
    //lateinit var uri_save: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.photo_viewer)

        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            val intent = Intent(this, ChallengeActivity::class.java)
            startActivity(intent)
            //if(uri_save != null) { //if the uri save exists, bring it with back to the challenge page
             //   intent.putExtra("uri_save", uri_save.toString())
            //}
            finish()
        }
        getPermissions()

        textureView = findViewById(R.id.textureView)
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler((handlerThread).looper)

        //set take picture button to take the picture
        findViewById<Button>(R.id.btn_take_picture).apply {
            setOnClickListener {
                //create capture request on current camera device
                capReq = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                //add the imagereader surface as a target
                capReq.addTarget(imageReader.surface)
                //capture the image
                cameraCaptureSession.capture(capReq.build(), null, null)

            }
        }

        imageReader = ImageReader.newInstance(1080, 1920, ImageFormat.JPEG, 1)

        //when the image is available to be read
        imageReader.setOnImageAvailableListener(object : ImageReader.OnImageAvailableListener {
            override fun onImageAvailable(p0: ImageReader?) {
                //get the image
                val image = p0?.acquireLatestImage()
                //create a by
                val buffer = image!!.planes[0].buffer
                //image to bytes
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)
                //date format for saving files
                val format = SimpleDateFormat("dd/M/yyyy hh:mm:ss", Locale("en", "US"))
                //get the current date in desired format
                val currentDate = format.format(Date())
                //turn it into a file
                val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "$currentDate-img.jpeg")
                //create output stream
                val opStream = FileOutputStream(file)
                //val uri = file.toUri()
                //write the output
                opStream.write(bytes)

                opStream.close()

                image.close()


                startChallengeActivity(file)
                //send bytearray to challenge activity
                //Toast.makeText(this@PhotoActivity, "Image Captured", Toast.LENGTH_SHORT).show()
            }
        }, handler)
        //initiate texture view as a listener
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            //when texture view surface is available
            @RequiresPermission(Manifest.permission.CAMERA)
            override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
                //open the camera
                openCamera()
            }

            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {
            }

            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {
            }


        }

    }
    override fun onDestroy() {
        super.onDestroy()
        cameraDevice.close()
        handler.removeCallbacksAndMessages(null)
        handlerThread.quitSafely()
    }
    //fun to open camera
    @RequiresPermission(Manifest.permission.CAMERA)
    private fun openCamera() {
        //check permissions
        //use camera manager to open camera
        cameraManager.openCamera(cameraManager.cameraIdList[0], object: CameraDevice.StateCallback(){
            override fun onOpened(p0: CameraDevice) {
                cameraDevice = p0
                //create a capture request
                capReq = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                var surface = Surface(textureView.surfaceTexture)
                capReq.addTarget(surface)

                cameraDevice.createCaptureSession(listOf(surface, imageReader.surface), object:
                    CameraCaptureSession.StateCallback() {
                    override fun onConfigured(p0: CameraCaptureSession) {
                        cameraCaptureSession = p0
                        cameraCaptureSession.setRepeatingRequest(capReq.build(), null, null)
                    }

                    override fun onConfigureFailed(p0: CameraCaptureSession) {

                    }
                }, handler)


            }

            override fun onDisconnected(p0: CameraDevice) {

            }

            override fun onError(p0: CameraDevice, p1: Int) {

            }
        }, handler)
    }
    fun getPermissions(){
        var permissionsList = mutableListOf<String>()

        if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {permissionsList.add(Manifest.permission.CAMERA)}
        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){ permissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE)}
        if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){ permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)}
        if (permissionsList.size >0){
            requestPermissions(permissionsList.toTypedArray(), 101)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        grantResults.forEach{
            if(it != PackageManager.PERMISSION_GRANTED){
                getPermissions()
            }
        }
    }
    private fun startChallengeActivity(file: File) {
        val intent = Intent(this, ChallengeActivity::class.java)
        intent.putExtra("Photo", file.toUri().toString())
        startActivity(intent)
    }
}
