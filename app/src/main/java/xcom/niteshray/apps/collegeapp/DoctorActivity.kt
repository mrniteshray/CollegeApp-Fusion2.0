package xcom.niteshray.apps.collegeapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response
import retrofit2.Callback
import xcom.niteshray.apps.collegeapp.api.EmailRequest
import xcom.niteshray.apps.collegeapp.api.Recipient
import xcom.niteshray.apps.collegeapp.api.RetrofitInstance
import xcom.niteshray.apps.collegeapp.api.Sender
import xcom.niteshray.apps.collegeapp.databinding.ActivityDoctorBinding
import xcom.niteshray.apps.collegeapp.model.User

class DoctorActivity : AppCompatActivity() {
    private lateinit var binding : ActivityDoctorBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var Student : User
    private lateinit var Doctor : User
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDoctorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fetchDoctorInfo()

        binding.btnLogout.setOnClickListener{
            auth.signOut()
            startActivity(Intent(this , LoginActivity::class.java))
            finish()
        }

        binding.btnSearch.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            val usersRef = db.collection("Users")
            usersRef.whereEqualTo("name", binding.etUsername.text.toString())
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        Student = documents.documents[0].toObject(User::class.java)!!
                        Glide.with(this).load(Student!!.profilePic).into(binding.profilepic)
                        binding.progressBar.visibility = View.GONE
                        binding.linear2.visibility = View.VISIBLE
                        binding.linearlayout3.visibility = View.VISIBLE
                        binding.name.text = Student!!.name
                        binding.etillness.visibility = View.VISIBLE
                        binding.btnSend.visibility = View.VISIBLE
                    } else {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Error searching user", Toast.LENGTH_SHORT).show()
                }
        }

        binding.btnSend.setOnClickListener {
            if (binding.etillness.text.toString().isNotEmpty()) {
                binding.progressBar.visibility = View.VISIBLE
                searchClassCoordinator(Student,binding.etillness.text.toString())
            }
        }
    }

    private fun searchClassCoordinator(student: User,Illness: String) {
        db.collection("Users")
            .whereEqualTo("authId", student.upperAuthority)
            .get().addOnSuccessListener {
                if (!it.isEmpty) {
                    val coordinator = it.documents[0].toObject(User::class.java)
                    if (coordinator != null) {
                        sendEmail(this,coordinator.email,student.name,Illness,coordinator.name)
                    }
                }
            }
    }

    private fun fetchDoctorInfo() {
        db.collection("Users").document(auth.currentUser!!.uid).get().addOnSuccessListener {
             Doctor = it.toObject(User::class.java)!!
            Glide.with(this).load(Doctor!!.profilePic).into(binding.circleImageView)
            binding.textView5.text = "Welcome\nDr.${Doctor!!.name}"
        }
    }

    private fun sendEmail(
        context: Context,
        toEmail: String,
        StudentName: String,
        Illness: String,
        Teachername: String
    ) {

        val emailBody = """
        Dear $Teachername,

        This is to inform you that $StudentName is currently under my care for $Illness. I am monitoring their condition and will provide updates as necessary. Please don't hesitate to contact me if you have any questions or require further information.

        Sincerely,

        Dr. ${Doctor.name}
        ${Doctor.email}
    """.trimIndent()
        val emailRequest = EmailRequest(
            sender = Sender("dopa696969696969@gmail.com", "Sitrc College"),
            to = listOf(Recipient(toEmail, "User")),
            subject = "Medical Notification for $StudentName",
            textContent = emailBody
        )
        CoroutineScope(Dispatchers.IO).launch {
            RetrofitInstance.brevoapi(context).sendEmail(emailRequest).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Email sent successfully", Toast.LENGTH_SHORT).show()
                        resetUI()
                    } else {
                        Toast.makeText(context, "Failed to send email", Toast.LENGTH_SHORT).show()
                        resetUI()
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    t.printStackTrace()
                    Toast.makeText(context, "Failed to send email", Toast.LENGTH_SHORT).show()
                    resetUI()
                }
            })
        }
    }

    private fun resetUI(){
        binding.progressBar.visibility = View.GONE
        binding.linear2.visibility = View.GONE
        binding.linearlayout3.visibility = View.GONE
        binding.etillness.visibility = View.GONE
        binding.emailLayout.visibility = View.GONE
        binding.btnSend.visibility = View.GONE
    }
}