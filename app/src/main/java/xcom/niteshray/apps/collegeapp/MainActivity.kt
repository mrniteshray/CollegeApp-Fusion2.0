package xcom.niteshray.apps.collegeapp

import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract.Colors
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
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

class MainActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var navController: NavController
    private var auth = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
}
