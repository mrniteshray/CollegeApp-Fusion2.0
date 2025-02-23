package xcom.niteshray.apps.collegeapp.UiScreens.facility

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import xcom.niteshray.apps.collegeapp.R
import xcom.niteshray.apps.collegeapp.databinding.FragmentFacilityBookingBinding
import xcom.niteshray.apps.collegeapp.model.Facility
import xcom.niteshray.apps.collegeapp.model.Slot
import java.util.Calendar
import java.util.Date
import java.util.UUID

class FacilityBookingFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SlotAdapter
    private lateinit var bookButton: Button

    private val args: FacilityBookingFragmentArgs by navArgs()

    private lateinit var binding : FragmentFacilityBookingBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFacilityBookingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val facility = args.facility
        recyclerView = view.findViewById(R.id.recyclerViewBookedSlots)
        bookButton = view.findViewById(R.id.btnBookSlot)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.textView8.text = facility.name
        Glide.with(this).load(facility.bannerImage).into(binding.imageView4)
        adapter = SlotAdapter(facility.slots)
        recyclerView.adapter = adapter
        bookButton.setOnClickListener { bookSlot() }

    }

    private fun bookSlot() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_selectdatetime, null)
        val startDateButton = dialogView.findViewById<Button>(R.id.btnSelectStartDateTime)
        val endDateButton = dialogView.findViewById<Button>(R.id.btnSelectEndDateTime)
        val okButton = dialogView.findViewById<Button>(R.id.btnSaveTiming)

        var startTimestamp: Timestamp? = null
        var endTimestamp: Timestamp? = null

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        startDateButton.setOnClickListener {
            showDateTimePicker { selectedDateTime ->
                startTimestamp = Timestamp(selectedDateTime)
                startDateButton.text = selectedDateTime.toString()
            }
        }

        endDateButton.setOnClickListener {
            showDateTimePicker { selectedDateTime ->
                endTimestamp = Timestamp(selectedDateTime)
                endDateButton.text = selectedDateTime.toString()
            }
        }

        okButton.setOnClickListener {
            if (startTimestamp != null && endTimestamp != null) {
                savetoFirestore(startTimestamp!!, endTimestamp!!)
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Please select both start and end time", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()

    }
    fun savetoFirestore(startTime: Timestamp, endTime: Timestamp){
        val newSlot = Slot(startTime, endTime,args.User.authId,args.User.name, UUID.randomUUID().toString())

        val db = FirebaseFirestore.getInstance()
        db.collection("facilities").document(args.facility.facilityId)
            .update("slots", FieldValue.arrayUnion(newSlot))
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Slot Booked!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e -> Log.e("Firestore", "Booking failed", e) }
    }
    private fun showDateTimePicker(onDateTimeSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            val timePicker = TimePickerDialog(requireContext(), { _, hour, minute ->
                calendar.set(year, month, dayOfMonth, hour, minute)
                onDateTimeSelected(calendar.time)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false)
            timePicker.show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        datePicker.show()
    }
}