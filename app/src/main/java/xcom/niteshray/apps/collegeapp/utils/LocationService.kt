package xcom.niteshray.apps.collegeapp.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationServices
import xcom.niteshray.apps.collegeapp.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocationService : Service() {

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var locationRequest: LocationRequest? = null

    private var location: Location? = null

    override fun onCreate() {
        super.onCreate()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                onNewLocation(locationResult)
            }
        }

        Log.d("LocationService", "onCreate")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "location_channel",
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        startForegroundService()
    }

    private fun startForegroundService() {
        val notification = NotificationCompat.Builder(this, "location_channel")
            .setContentTitle("Tracking Location")
            .setContentText("Your location is being tracked in the background")
            .setSmallIcon(R.drawable.vice)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)
    }

    @Suppress("MissingPermission")
    fun createLocationRequest() {
        try {
            fusedLocationProviderClient?.requestLocationUpdates(
                locationRequest!!, locationCallback!!, null
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onNewLocation(locationResult: LocationResult) {
        location = locationResult.lastLocation

        val sharedPrefs = getSharedPreferences("LocationPrefs", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()

        val distance = FloatArray(1)
        Location.distanceBetween(
            location!!.latitude,
            location!!.longitude,
            Constants.COLLEGE_LATITUDE,
            Constants.COLLEGE_LONGITUDE,
            distance
        )

        val isInsideCampus = distance[0] <= Constants.GEOFENCE_RADIUS
        val enteredCampusToday = sharedPrefs.getBoolean("enteredCampusToday", false)
        val exitedCampusToday = sharedPrefs.getBoolean("exitedCampusToday", false)
        val lastUpdatedDate = sharedPrefs.getString("savedDate", "")

        val currentDate = getCurrentDate()

//        if (currentDate != lastUpdatedDate) {
//            editor.putBoolean("enteredCampusToday", false)
//            editor.putBoolean("exitedCampusToday", false)
//            editor.putString("savedDate", currentDate)
//            editor.apply()
//        }

        if (isInsideCampus) {
            if (!enteredCampusToday) {
                EmailService.sendEmail(this,"Entered")
                editor.putBoolean("enteredCampusToday", true)
                editor.apply()
            }
        } else {
            if (!exitedCampusToday && enteredCampusToday) {
                EmailService.sendEmail(this,"Leaved")
                editor.putBoolean("exitedCampusToday", true)
                editor.apply()
            }
        }

        Log.d("LocationService", "Location: ${location?.latitude}, ${location?.longitude}")
    }

    private fun getCurrentDate(): String {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return format.format(Date())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        createLocationRequest()
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationProviderClient?.removeLocationUpdates(locationCallback!!)
    }
}