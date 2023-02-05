package com.example.photourism.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.photourism.R
import com.example.photourism.databinding.FragmentMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.File


class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private lateinit var map: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationOverlay: MyLocationNewOverlay? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root: View = inflater.inflate(R.layout.fragment_map, container, false)
        map = root.findViewById(R.id.mapV)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.requireContext())

        requestPermissionsIfNecessary(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
            )
        )
        showMap(map)
        getLastKnownLocation(map)
        return root
    }




    @SuppressLint("MissingPermission")
    private fun showMap(map : MapView) {
        Configuration.getInstance().setUserAgentValue(context!!.packageName)

        map.setTileSource(TileSourceFactory.MAPNIK)
        map.controller.zoomTo(10.0)
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.ALWAYS)
        map.setMultiTouchControls(true) // para poder fazer zoom com os dedos

        var compassOverlay = CompassOverlay(context, map)
        compassOverlay.enableCompass()
        map.overlays.add(compassOverlay)

        Toast.makeText(this.context, "A carregar...", Toast.LENGTH_SHORT).show()
        /*
        var point = GeoPoint(39.60068, -8.38967)       // 39.60199, -8.39675
        Handler(Looper.getMainLooper()).postDelayed({
            map.controller.setCenter(point)
        }, 1000) // waits one second to center map
        */
    }


    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
        Configuration.getInstance().load(this.context, PreferenceManager.getDefaultSharedPreferences(this.context));
        //add
        locationOverlay!!.enableMyLocation();
    }


    private fun requestPermissionsIfNecessary(permissions: Array<out String>){
        val permissionsToRequest = ArrayList<String>();
        permissions.forEach { permission ->
            if (ContextCompat.checkSelfPermission(
                    this.context!!,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size > 0) {
         requestPermissions(
                permissionsToRequest.toArray(arrayOf<String>()),
                REQUEST_PERMISSIONS_REQUEST_CODE
            );
        }
    }

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(map : MapView){
        println("pre location")
        /*
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location->
                println("location")
                if (location != null) {
                    var point = GeoPoint(location.latitude,location.longitude)
                    println(location.latitude)

                    map.controller.setCenter(point)

                } else {
                    println("No location found.")
                }
            }
        */

        val provider = GpsMyLocationProvider(context)
        provider.addLocationSource(LocationManager.NETWORK_PROVIDER)
        locationOverlay = MyLocationNewOverlay(provider, map)
        locationOverlay!!.enableFollowLocation()
        locationOverlay!!.runOnFirstFix(Runnable {
            Log.d(
                "MyTag",
                java.lang.String.format("First location fix: %s", locationOverlay!!.getLastFix())
            )
        })
        map.overlayManager.add(locationOverlay)
    }

    fun getOutputDirectory(): File {
        val mediaDir = activity?.externalMediaDirs?.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else activity?.filesDir!!
    }

    fun loadMarkers() {

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}