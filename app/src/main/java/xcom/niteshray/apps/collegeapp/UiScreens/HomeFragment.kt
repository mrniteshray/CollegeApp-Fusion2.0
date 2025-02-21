package xcom.niteshray.apps.collegeapp.UiScreens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import xcom.niteshray.apps.collegeapp.R
import xcom.niteshray.apps.collegeapp.databinding.FragmentHomeBinding
import xcom.niteshray.apps.collegeapp.model.User

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var currentuser : User


    private var auth = FirebaseAuth.getInstance().currentUser?.uid

    lateinit var role : String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getUserRole()

        binding.cardElection.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_electionFragment)
        }
        binding.complaints.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_complaintFragment)
        }

        binding.collegeFacilities.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_facilityFragment)
        }
        binding.cardCheater.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_cheatersFragment)
        }

        fetchuser()
    }

    private fun fetchuser() {
        val db = FirebaseFirestore.getInstance()
        db.collection("Users").document(auth.toString()).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    currentuser = document.toObject(User::class.java)!!
                }
                loadui(currentuser)
            }

    }

    private fun loadui(currentuser: User) {
        binding.name.text = currentuser.name
        binding.email.text = currentuser.email
        binding.branch.text = currentuser.branch
        binding.role.text = currentuser.role

        Glide.with(this).load(currentuser.profilePic).into(binding.profilepic)
    }

    fun getUserRole(){
        val auth = FirebaseAuth.getInstance().currentUser

        auth?.uid?.let { uid ->
            FirebaseFirestore.getInstance().collection("Users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    val current = document.toObject(User::class.java)!!
                    if (document.exists()) {
                        role = current.role
                        }
                    }
                .addOnFailureListener {
                }
        }
    }
}