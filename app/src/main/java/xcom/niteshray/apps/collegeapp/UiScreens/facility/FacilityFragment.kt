package xcom.niteshray.apps.collegeapp.UiScreens.facility

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import xcom.niteshray.apps.collegeapp.databinding.FragmentFacilityBinding
import xcom.niteshray.apps.collegeapp.model.Facility
import xcom.niteshray.apps.collegeapp.model.User

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
        db.collection("facilities").get()
            .addOnSuccessListener { documents ->
                val facilityList = mutableListOf<Facility>()
                for (document in documents) {
                    val facility = document.toObject(Facility::class.java)
                    facilityList.add(facility)
                }
                binding.facilityRec.adapter = FacilitiesAdapter(requireContext(),facilityList) {
                    val action = FacilityFragmentDirections.actionFacilityFragmentToFacilityBookingFragment(it,currentUerData)
                    findNavController().navigate(action)
                }
            }
            .addOnFailureListener { e -> Log.e("Firestore", "Error fetching slots", e) }
        }
}
