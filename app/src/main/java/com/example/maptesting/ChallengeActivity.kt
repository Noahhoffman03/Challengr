package com.example.maptesting

<<<<<<< Updated upstream
class ChallengeActivity {
}
=======
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.File
import java.io.FileOutputStream

class ChallengeActivity : AppCompatActivity() {
    lateinit var capReq: CaptureRequest.Builder
    lateinit var handler: Handler
    lateinit var handlerThread: HandlerThread
    lateinit var cameraManager: CameraManager
    lateinit var textureView: TextureView
    lateinit var cameraCaptureSession: CameraCaptureSession
    lateinit var cameraDevice: CameraDevice
    lateinit var captureRequest: CaptureRequest
    lateinit var imageReader: ImageReader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_challenge)

        getPermissions()

        textureView = findViewById(R.id.textureView)
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler((handlerThread).looper)

        imageReader = ImageReader.newInstance(1080,1980,ImageFormat.JPEG, 1)
        imageReader.setOnImageAvailableListener(object: ImageReader.OnImageAvailableListener {
            override fun onImageAvailable(p0: ImageReader?) {
                val image = p0?.acquireLatestImage()
                val buffer = image!!.planes[0].buffer
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)

                val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "img.jpeg")
                val opStream = FileOutputStream(file)

                opStream.write(bytes)

                opStream.close()

                image.close()
                Toast.makeText(this@ChallengeActivity, "Image Captured", Toast.LENGTH_SHORT).show()
            }
        },handler)
        textureView.surfaceTextureListener = object: TextureView.SurfaceTextureListener{
            override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
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
        findViewById<Button>(R.id.btn_take_picture).apply {
            setOnClickListener {
                capReq = cameraDevice.createCaptureRequest((CameraDevice.TEMPLATE_STILL_CAPTURE))
                capReq.addTarget(imageReader.surface)
                cameraCaptureSession.capture(capReq.build(),null,null)

            }
        }


        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun openCamera() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            getPermissions()
        }
        cameraManager.openCamera(cameraManager.cameraIdList[0], object: CameraDevice.StateCallback(){
            override fun onOpened(p0: CameraDevice) {
                cameraDevice = p0

                capReq = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                var surface = Surface(textureView.surfaceTexture)
                capReq.addTarget(surface)

                cameraDevice.createCaptureSession(listOf(surface,imageReader.surface), object:
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

    override fun onDestroy() {
        super.onDestroy()
        cameraDevice.close()
        handler.removeCallbacksAndMessages(null)
        handlerThread.quitSafely()
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
}
>>>>>>> Stashed changes
