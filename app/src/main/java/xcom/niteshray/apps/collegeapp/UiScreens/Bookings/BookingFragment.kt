package xcom.niteshray.apps.collegeapp.UiScreens.Bookings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import xcom.niteshray.apps.collegeapp.R
import xcom.niteshray.apps.collegeapp.model.FacilityRequest

class BookingFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var bookingAdapter: BookingAdapter
    private lateinit var bookingList: MutableList<FacilityRequest>
    private lateinit var currentUserId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_booking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.bookingrec)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        bookingList = mutableListOf()
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        loadBookings()
    }

    private fun loadBookings() {
        val db = FirebaseFirestore.getInstance()
        val bookingsCollection = db.collection("Bookings")

        bookingsCollection.get()
            .addOnSuccessListener { snapshots ->
                bookingList.clear()

                snapshots.forEach { document ->
                    val booking = document.toObject(FacilityRequest::class.java)
                        bookingList.add(booking)
                }

                bookingAdapter = BookingAdapter(requireContext(), bookingList, currentUserId)
                recyclerView.adapter = bookingAdapter
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error fetching bookings: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
