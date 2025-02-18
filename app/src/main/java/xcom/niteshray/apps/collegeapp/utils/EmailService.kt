package xcom.niteshray.apps.collegeapp.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xcom.niteshray.apps.collegeapp.api.Recipient
import xcom.niteshray.apps.collegeapp.api.RetrofitInstance
import xcom.niteshray.apps.collegeapp.api.Sender
import xcom.niteshray.apps.collegeapp.api.EmailRequest
import xcom.niteshray.apps.collegeapp.model.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object EmailService {
    fun sendEmail(context: Context, action: String) {
        val auth = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()

        val currentTime = System.currentTimeMillis()
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val formattedTime = timeFormat.format(Date(currentTime))

        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        auth?.let {
            db.collection("Users").document(it).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val currentuser = document.toObject(User::class.java)!!
                        val emailRequest = EmailRequest(
                            sender = Sender("dopa696969696969@gmail.com", "Sitrc College"),
                            to = listOf(Recipient(currentuser.parentemail!!, "User")),
                            subject = "${currentuser.name} has $action the college campus",
                            textContent = """
                                Dear Parent,

                                We hope you are doing well. This is to inform you that your ward ${currentuser.name} has $action the college premises today at $formattedTime.
                                
                                ✅ Date: $currentDate
                                ✅ Time of Entry: $formattedTime
                                
                                Your child's presence has been recorded successfully. If you have any concerns, please feel free to contact us.

                                Best Regards,
                                SITRC College
                            """.trimIndent()
                        )

                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val response = RetrofitInstance.apiservice(context).sendEmail(emailRequest).execute()
                                withContext(Dispatchers.Main) {
                                    if (response.isSuccessful) {
                                        Toast.makeText(context, "Email sent successfully", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                                        Log.e("EmailService", "Error email: $errorMessage")
                                        Toast.makeText(context, "Failed to send email: $errorMessage", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Log.e("EmailService", "Error sending email: ${e.message}")
                                    Toast.makeText(context, "Failed to send email", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
        }
    }
}
