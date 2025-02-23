package xcom.niteshray.apps.collegeapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response
import retrofit2.Callback
import xcom.niteshray.apps.collegeapp.UiScreens.DoctorAdapter
import xcom.niteshray.apps.collegeapp.api.EmailRequest
import xcom.niteshray.apps.collegeapp.api.Recipient
import xcom.niteshray.apps.collegeapp.api.RetrofitInstance
import xcom.niteshray.apps.collegeapp.api.Sender
import xcom.niteshray.apps.collegeapp.databinding.ActivityDoctorBinding
import xcom.niteshray.apps.collegeapp.model.Appointment
import xcom.niteshray.apps.collegeapp.model.User

class DoctorActivity : AppCompatActivity() {
    private lateinit var binding : ActivityDoctorBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

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

        binding.recycler.layoutManager = LinearLayoutManager(this)
        fetchAppointments()

        fetchDoctorInfo()

        binding.btnLogout.setOnClickListener{
            auth.signOut()
            startActivity(Intent(this , LoginActivity::class.java))
            finish()
        }

    }
    fun fetchAppointments(){
        db.collection("appointments").get().addOnSuccessListener {
            val list = mutableListOf<Appointment>()
            Toast.makeText(this, "${it.size()}", Toast.LENGTH_SHORT).show()
            for(document in it){
                val appointment = document.toObject(Appointment::class.java)
                list.add(appointment)
            }
            binding.recycler.adapter = DoctorAdapter(list){
                writePrescription(it)
            }
        }
    }

    fun writePrescription(appointment: Appointment) {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_writeprescrip, null)
        builder.setView(view)
        val dialog = builder.create()
        dialog.show()
        view.findViewById<Button>(R.id.btn_sendReport).setOnClickListener {
            val precription = view.findViewById<EditText>(R.id.inputTxt).text.toString()
            if(precription.isNotEmpty()){
                sendEmail(this,appointment,precription)
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
        appointment: Appointment,
        precription: String
    ) {
        val emailBody = """
        Dear ,

        I hope this email finds you well. Following our consultation on , please find your prescribed medications and treatment details below.
        $precription

        Sincerely,
        Dr. ${Doctor.name}
        ${Doctor.email}
    """.trimIndent()
        val emailRequest = EmailRequest(
            sender = Sender("dopa696969696969@gmail.com", "Sitrc College"),
            to = listOf(Recipient(appointment.studentEmail, "User")),
            subject = "Medical Prescription",
            textContent = emailBody
        )
        CoroutineScope(Dispatchers.IO).launch {
            RetrofitInstance.brevoapi(context).sendEmail(emailRequest).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Email sent successfully", Toast.LENGTH_SHORT).show()

                    } else {
                        Toast.makeText(context, "Failed to send email", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    t.printStackTrace()
                    Toast.makeText(context, "Failed to send email", Toast.LENGTH_SHORT).show()

                }
            })
        }
    }
}