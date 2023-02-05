package com.example.photourism.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.location.Location
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.photourism.GPSLocator
import com.example.photourism.R
import com.example.photourism.databinding.FragmentCameraBinding
import java.io.File
import java.util.*
import java.util.concurrent.ExecutorService


public class CameraFragment : Fragment() {

    private var viewBinding: FragmentCameraBinding? = null
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var outputDirectory: File
    private lateinit var gps: GPSLocator

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentCameraBinding.inflate(inflater)
        val root: View = viewBinding!!.root

        val cameraViewModel =
            ViewModelProvider(this)[CameraViewModel::class.java]
        outputDirectory = getOutputDirectory()
        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
            gps = GPSLocator(requireContext())
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
        viewBinding!!.takePhotoBt.setOnClickListener {
            if (allPermissionsGranted())
                takePhoto()
        }

        return root
    }

    private fun getOutputDirectory(): File {
        val mediaDir = activity?.externalMediaDirs?.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else activity?.filesDir!!
    }

    override fun onDestroyView() {
        if (::cameraExecutor.isInitialized)
            cameraExecutor.shutdown()
        super.onDestroyView()
        viewBinding = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
                gps = GPSLocator(requireContext())
            } else {
                Toast.makeText(viewBinding!!.root.context,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                onDestroyView()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            viewBinding!!.root.context, it) == PackageManager.PERMISSION_GRANTED
    }
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(viewBinding!!.root.context)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding!!.viewFinder.surfaceProvider)
                }
            imageCapture=ImageCapture.Builder().build()
            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(viewBinding!!.root.context))
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture
        // Create timestamped output file to hold the image
        Toast.makeText(context, outputDirectory.toString(), Toast.LENGTH_SHORT).show()
        val photoName = SimpleDateFormat(FILENAME_FORMAT, Locale.UK).format(System.currentTimeMillis()) + ".jpg"
        val photoFile = File(outputDirectory, photoName)
        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Setup image capture listener which is triggered after photo has
        // been taken
        imageCapture!!.takePicture(outputOptions, ContextCompat.getMainExecutor(context!!), object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
            }

            @RequiresApi(Build.VERSION_CODES.Q)
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)

                val location: Location = gps.GetLocation()

                var exif: ExifInterface = ExifInterface(photoFile)

                exif!!.setAttribute(ExifInterface.TAG_GPS_LATITUDE, dec2DMS(location.latitude))
                exif!!.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, dec2DMS(location.longitude))
                exif!!.saveAttributes()

                println("Meta -> " + exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE))
                println("Meta -> " + exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE))

                val msg = "Photo capture succeeded: $savedUri"
                Toast.makeText(context!!, msg, Toast.LENGTH_SHORT).show()
                Log.d(TAG, msg)
            }
        })
    }

    fun dec2DMS(coord: Double): String? {
        var coord = coord
        coord = if (coord > 0) coord else -coord // -105.9876543 -> 105.9876543
        var sOut = Integer.toString(coord.toInt()) + "/1," // 105/1,
        coord = coord % 1 * 60 // .987654321 * 60 = 59.259258
        sOut = sOut + Integer.toString(coord.toInt()) + "/1," // 105/1,59/1,
        coord = coord % 1 * 60000 // .259258 * 60000 = 15555
        sOut = sOut + Integer.toString(coord.toInt()) + "/1000" // 105/1,59/1,15555/1000
        return sOut
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}