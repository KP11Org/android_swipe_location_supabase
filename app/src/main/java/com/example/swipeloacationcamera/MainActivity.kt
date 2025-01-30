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
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.example.swipeloacationcamera.data.SupabaseConn
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.common.util.concurrent.ListenableFuture
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.exceptions.HttpRequestException
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration.getInstance
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var cameraProviderFeature: ListenableFuture<ProcessCameraProvider>
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val address = MutableLiveData<String>()
    private var locationManager : LocationManager? = null
    private lateinit var previewView:PreviewView
    private lateinit var map: MapView
    private lateinit var mLocationOverlay: MyLocationNewOverlay

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Конфигурация карты
        getInstance().load(this, androidx.preference.PreferenceManager.getDefaultSharedPreferences(this))
        //---
        setContentView(R.layout.activity_main)
        //Затемнение экрана
        window.attributes = window.attributes.apply {
            screenBrightness = 0f
        }
        //Установка карты
        map = findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        //Запрос к БД на авторизацию
        lifecycleScope.launch {
            try {
                SupabaseConn.supabase.auth.clearSession()
                SupabaseConn.supabase.auth.signInWith(Email){
                    email = "testuser@gmail.com"
                    password = "User12345678!"
                }
            }catch (ex : AuthRestException){
                //Сообщение когда неверные данные
                Log.e("AuthFailed", ex.message.toString())
                Log.e("AuthFailed", ex.statusCode.toString())
                Log.e("AuthFailed", ex.description.toString())
            }catch (ex : HttpRequestException){
                //Разраыв соединения
                Log.e("InternetConnection", ex.message.toString())
            }
            catch (ex : Exception){
                //Другие ошибки
                Log.e("auth", ex.message.toString())
                Log.e("auth", ex.cause.toString())
                Log.e("auth", ex.toString())
            }

        }.invokeOnCompletion {
            //Вывод результата когда запрос выполнен без ошибок
            Log.e("supabaseResult", SupabaseConn.supabase.auth.currentUserOrNull().toString())
        }

        val mainCardView = findViewById<LinearLayout>(R.id.mainCont)
        val leftCard = findViewById<CardView>(R.id.blueCard)
        val rightCard = findViewById<CardView>(R.id.redCard)
        val btnGetLocation = findViewById<Button>(R.id.btnGetLocation)
        val addressTextView = findViewById<TextView>(R.id.addressTextView)
        //Получение геопозиции
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
        //Обработка свайпа по карточке товара
        val gestureDetector = GestureDetector(this, object : GestureDetector.OnGestureListener{
            private val swipeThreshold = 100
            override fun onDown(p0: MotionEvent): Boolean = true
            override fun onShowPress(p0: MotionEvent) {}
            override fun onSingleTapUp(p0: MotionEvent): Boolean = true
            override fun onScroll(
                p0: MotionEvent?,
                p1: MotionEvent,
                p2: Float,
                p3: Float
            ): Boolean = true
            override fun onLongPress(p0: MotionEvent) {}
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
        mainCardView.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }


        //Запрос на доступ к геолокации
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    getCurrentLocation()
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    getCurrentLocation()
                } else -> {
                    Toast.makeText(this, "Access denied", Toast.LENGTH_LONG).show()
                }
            }
        }


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
        //Проверка что разрешения выданы
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
        //Получаем геопозицию пользователя
        locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f
        ) {
            getAddress(it)
        }
    }


    private fun getAddress(location: Location) {
        //Устанавливаем курсор к пользователю
        mLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), map)
        mLocationOverlay.enableMyLocation()
        map.overlays.add(mLocationOverlay)
        map.invalidate()
        try {
            val latitude = location.latitude
            val longitude = location.longitude
            //Перемещение к точке
            map.controller.setCenter(GeoPoint(latitude, longitude))
            map.setZoomLevel(13.0)
            //Получение текущего адресса по координатам
            //Log.e("Location", location.toString())
            val geo = Geocoder(this, Locale("RU-ru"))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geo.getFromLocation(latitude, longitude, 1) {
                    Log.e("loc", it.toString())
                    address.postValue(it.first().getAddressLine(0))
                }
            } else {
                address.postValue("VERSION CODE less than TIRAMISU")
            }
        }catch (ex : Exception){

        }

    }



}