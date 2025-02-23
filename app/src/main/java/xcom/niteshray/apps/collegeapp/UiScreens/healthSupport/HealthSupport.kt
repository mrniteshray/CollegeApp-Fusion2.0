package xcom.niteshray.apps.collegeapp.UiScreens.healthSupport

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import xcom.niteshray.apps.collegeapp.R
import xcom.niteshray.apps.collegeapp.model.Appointment

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import xcom.niteshray.apps.collegeapp.model.User

import java.text.SimpleDateFormat
import java.util.*

class HealthSupport : Fragment() {

    private lateinit var etReason: EditText
    private lateinit var tvSelectedDate: TextView
    private lateinit var tvSelectedTime: TextView
    private lateinit var btnSelectDate: Button
    private lateinit var btnSelectTime: Button
    private lateinit var btnBookAppointment: Button
    private lateinit var progressDialog: ProgressDialog

    private var selectedDate: String = ""
    private var selectedTime: String = ""
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    lateinit var CurrentUser : User

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_health_support, container, false)

        etReason = view.findViewById(R.id.etReason)
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate)
        tvSelectedTime = view.findViewById(R.id.tvSelectedTime)
        btnSelectDate = view.findViewById(R.id.btnSelectDate)
        btnSelectTime = view.findViewById(R.id.btnSelectTime)
        btnBookAppointment = view.findViewById(R.id.btnBookAppointment)

        progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Booking appointment...")
        progressDialog.setCancelable(false)

        btnSelectDate.setOnClickListener { showDatePicker() }
        btnSelectTime.setOnClickListener { showTimePicker() }
        btnBookAppointment.setOnClickListener { bookAppointment() }

        fetchUserData()

        return view
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                calendar.set(year, month, dayOfMonth)
                selectedDate = sdf.format(calendar.time)
                tvSelectedDate.text = "Selected Date: $selectedDate"
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.datePicker.minDate = System.currentTimeMillis() // Restrict past dates
        datePicker.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val timePicker = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                selectedTime = sdf.format(calendar.time)
                tvSelectedTime.text = "Selected Time: $selectedTime"
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )
        timePicker.show()
    }

    fun fetchUserData(){
        firestore.collection("Users")
            .document(auth.currentUser!!.uid)
            .get().addOnSuccessListener {
                val currentUser = it.toObject(User::class.java)
                if (currentUser != null) {
                    CurrentUser = currentUser
                }
            }
    }

    private fun bookAppointment() {
        val reason = etReason.text.toString().trim()
        val studentAuthId = auth.currentUser?.uid ?: "Unknown"
        val studentEmail = auth.currentUser?.email ?: "Unknown"

        if (reason.isEmpty() || selectedDate.isEmpty() || selectedTime.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all details", Toast.LENGTH_SHORT).show()
            return
        }

        progressDialog.show()

        val appointmentRef = firestore.collection("appointments").document()
        val appointment = CurrentUser.parentemail?.let {
            Appointment(
                bookingId = appointmentRef.id,
                studentAuthId = studentAuthId,
                studentEmail = studentEmail,
                studentParentemail = it,
                reason = reason,
                date = selectedDate,
                time = selectedTime
            )
        }

        if (appointment != null) {
            appointmentRef.set(appointment)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(requireContext(), "Appointment booked successfully", Toast.LENGTH_LONG).show()
                    requireActivity().supportFragmentManager.popBackStack()
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}
