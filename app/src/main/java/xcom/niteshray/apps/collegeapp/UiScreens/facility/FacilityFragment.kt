package xcom.niteshray.apps.collegeapp.UiScreens.facility

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import xcom.niteshray.apps.collegeapp.R
import xcom.niteshray.apps.collegeapp.databinding.FragmentFacilityBinding
import xcom.niteshray.apps.collegeapp.model.Facility
import xcom.niteshray.apps.collegeapp.model.FacilityRequest
import xcom.niteshray.apps.collegeapp.model.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class FacilityFragment : Fragment() {
    private lateinit var binding: FragmentFacilityBinding

    private lateinit var currentUerData : User
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFacilityBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.floatingActionButton.setOnClickListener{
            findNavController().navigate(R.id.action_facilityFragment_to_bookingFragment)
        }

        val auth = FirebaseAuth.getInstance().currentUser
        auth?.uid?.let { uid ->
            FirebaseFirestore.getInstance().collection("Users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    currentUerData = document.toObject(User::class.java)!!
                }
        }
        binding.facilityRec.layoutManager = LinearLayoutManager(requireContext())
        fetchFacilities()
    }

    private fun fetchFacilities() {
        val db = FirebaseFirestore.getInstance()
        db.collection("Facilities").get().addOnSuccessListener { documents ->
            val list = mutableListOf<Facility>()
            for (document in documents) {
                val facility = document.toObject(Facility::class.java).apply {
                    this.id = document.id
                }
                list.add(facility)
            }
            binding.facilityRec.adapter = FacilitiesAdapter(requireContext(), list) { facility ->
                onBookClick(facility)
            }
        }
    }

    private fun onBookClick(facility: Facility) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select a booking date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(selection))
            checkAvailabilityAndBook(facility, selectedDate)
        }

        datePicker.show(parentFragmentManager, "MATERIAL_DATE_PICKER")
    }

    private fun checkAvailabilityAndBook(facility: Facility, selectedDate: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("Bookings")
            .whereEqualTo("facilityId", facility.id)
            .whereEqualTo("bookingDate", selectedDate)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    Toast.makeText(requireContext(), "Already booked for selected date", Toast.LENGTH_SHORT).show()
                } else {
                    bookFacility(facility, selectedDate)
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error checking availability", Toast.LENGTH_SHORT).show()
            }
    }


    private fun bookFacility(facility: Facility, selectedDate: String) {
        val bookingData = FacilityRequest(
            requestId = UUID.randomUUID().toString(),
            facilityId = facility.id,
            facilityName = facility.name,
            facilityImage = facility.FacilityImgUrl,
            bookedBy = currentUerData.name,
            bookedById = currentUerData.authId,
            bookingDate = selectedDate,
            status = "pending",
            approvedBy = null,
            upperAuthority = currentUerData.upperAuthority
        )

        FirebaseFirestore.getInstance().collection("Bookings")
            .document(bookingData.requestId)
            .set(bookingData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Booking Request Sent!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
