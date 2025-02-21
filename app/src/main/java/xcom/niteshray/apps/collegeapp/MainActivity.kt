package xcom.niteshray.apps.collegeapp

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract.Colors
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import xcom.niteshray.apps.collegeapp.UiScreens.HomeFragment
import xcom.niteshray.apps.collegeapp.model.User
import xcom.niteshray.apps.collegeapp.utils.LocationService
import xcom.niteshray.apps.collegeapp.utils.MidnightReceiver
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var navController: NavController
    private var auth = FirebaseAuth.getInstance().currentUser?.uid

    private val backgroundLocation =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val service = Intent(this, LocationService::class.java)
            startForegroundService(service)
            Toast.makeText(this, "Notification Permission Granted 🎉", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Please Allow Notification Permission", Toast.LENGTH_SHORT).show()
        }
    }

    private val locationPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            when {
                it.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (ActivityCompat.checkSelfPermission(
                                this,
                                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            backgroundLocation.launch(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        }
                    }
                }
                it.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    val service = Intent(this, LocationService::class.java)
                    startService(service)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPrefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val sharedPrefs2 = getSharedPreferences("LocationPrefs", Context.MODE_PRIVATE)
        sharedPrefs2.getBoolean("enteredCampusToday",false)?.let { Log.d("LocationService",
            it.toString()
        ) }
        val editor = sharedPrefs2.edit()
        editor.putBoolean("enteredCampusToday", false)
        editor.putBoolean("exitedCampusToday", false)

        val isFirstLaunch = sharedPrefs.getBoolean("isFirstLaunch", true)

        if (isFirstLaunch) {
            setMidnightAlarm()
            val editor = sharedPrefs.edit()
            editor.putBoolean("isFirstLaunch", false)
            editor.apply()
        }
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)

        val header = navigationView.getHeaderView(0)
        val img : ImageView = header.findViewById(R.id.header_profile)
        val username = header.findViewById<TextView>(R.id.header_Username)
        val db = FirebaseFirestore.getInstance()
        db.collection("Users").document(auth.toString()).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val currentuser = document.toObject(User::class.java)!!
                    Glide.with(this).load(currentuser.profilePic).into(img)
                    username.text = currentuser.name
                    if(currentuser.role == "Student"){
                        checkPermissions()
                    }else{
                        val service = Intent(this, LocationService::class.java)
                        stopService(service)
                    }
                }
            }
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Setup ActionBarDrawerToggle
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close)
        toggle.drawerArrowDrawable.color = resources.getColor(R.color.white)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Navigation Drawer item selection
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> navController.navigate(R.id.homeFragment)
                R.id.nav_elections -> navController.navigate(R.id.electionFragment)
                R.id.nav_complaints -> navController.navigate(R.id.complaintFragment)
                R.id.nav_booking -> navController.navigate(R.id.facilityFragment)
                R.id.nav_logout -> logout()
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
    private fun setMidnightAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this,MidnightReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                locationPermissions.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            } else {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }else{
                    val service = Intent(this, LocationService::class.java)
                    startForegroundService(service)
                }
            }
        }
    }
}