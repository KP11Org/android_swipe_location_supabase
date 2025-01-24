package com.example.swipeloacationcamera

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.example.swipeloacationcamera.data.SupabaseConn
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.exceptions.HttpRequestException
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val address = MutableLiveData<String>()
    private var locationManager : LocationManager? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        lifecycleScope.launch {
            try {
                SupabaseConn.supabase.auth.clearSession()
                SupabaseConn.supabase.auth.signInWith(Email){
                    email = "testuse@gmail.com"
                    password = "User12345678!"
                }
            }catch (ex : AuthRestException){
                Log.e("AuthFailed", ex.message.toString())
                Log.e("AuthFailed", ex.statusCode.toString())
                Log.e("AuthFailed", ex.description.toString())
            }catch (ex : HttpRequestException){
                Log.e("InternetConnection", ex.message.toString())
            }
            catch (ex : Exception){
                Log.e("auth", ex.message.toString())
                Log.e("auth", ex.cause.toString())
                Log.e("auth", ex.toString())
            }

        }.invokeOnCompletion {
            Log.e("supabaseResult", SupabaseConn.supabase.auth.currentUserOrNull().toString())
        }

        val mainCardView = findViewById<LinearLayout>(R.id.mainCont)
        val leftCard = findViewById<CardView>(R.id.blueCard)
        val rightCard = findViewById<CardView>(R.id.redCard)
        val btnGetLocation = findViewById<Button>(R.id.btnGetLocation)
        val addressTextView = findViewById<TextView>(R.id.addressTextView)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?
        address.observe(this){
            addressTextView.text = it
        }
        locationCallback = object : LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult) {
                for(location in locationResult.locations){
                    addressTextView.text = location.toString()
                }
            }
        }

        val gestureDetector = GestureDetector(this, object : GestureDetector.OnGestureListener{
            private val swipeThreshold = 100
            override fun onDown(p0: MotionEvent): Boolean {
                return true
            }

            override fun onShowPress(p0: MotionEvent) {

            }

            override fun onSingleTapUp(p0: MotionEvent): Boolean {
                return true
            }

            override fun onScroll(
                p0: MotionEvent?,
                p1: MotionEvent,
                p2: Float,
                p3: Float
            ): Boolean {
                return true
            }

            override fun onLongPress(p0: MotionEvent) {

            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                    val distX = e2?.x!! - e1?.x!!
                    if (distX > swipeThreshold) {
                        leftCard.visibility = View.VISIBLE
                        rightCard.visibility = View.GONE
                    }
                    else if (distX < -swipeThreshold) {
                        rightCard.visibility = View.VISIBLE
                        leftCard.visibility = View.GONE
                    }
                    return true

            }
        })



        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    getCurrentLocation()
                }
                permissions.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    getCurrentLocation()
                } else -> {
                    Toast.makeText(this, "Access denide", Toast.LENGTH_LONG).show()
                }
            }
        }
        mainCardView.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
        btnGetLocation.setOnClickListener {
            try {
                locationPermissionRequest.launch(arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION))
            }catch (ex : Exception){

            }

        }


    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f
        ) {
            getAddress(it)
        }
//        fusedLocationClient.lastLocation
//            .addOnSuccessListener { location ->
//                getAddress(location)
//            }
    }

    private fun getAddress(location: Location) {
        Log.e("Location", location.toString())
        val geo = Geocoder(this, Locale("RU-ru"))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geo.getFromLocation(location.latitude, location.longitude, 1) {
                Log.e("loc", it.toString())
                address.postValue(it.first().getAddressLine(0))
            }
        } else {
            address.postValue("VERSION CODE less than TIRAMISU")
        }
    }

}