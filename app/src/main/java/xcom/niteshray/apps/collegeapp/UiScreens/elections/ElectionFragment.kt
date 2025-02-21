package xcom.niteshray.apps.collegeapp.UiScreens.elections

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import xcom.niteshray.apps.collegeapp.R
import java.util.Date
import xcom.niteshray.apps.collegeapp.databinding.FragmentElectionBinding

class ElectionFragment : Fragment() {

    private var _binding: FragmentElectionBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()

    private lateinit var Userrole : String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentElectionBinding.inflate(inflater, container, false)
        val view = binding.root

        fetchElectionTiming()
        getUserRole()

        binding.president.setOnClickListener {
            val action = ElectionFragmentDirections.actionElectionFragmentToVotingFragment(position = "President", role = Userrole)
            findNavController().navigate(action)
        }

        binding.vicePresident.setOnClickListener {
            val action = ElectionFragmentDirections.actionElectionFragmentToVotingFragment(position = "Vice President", role = Userrole)
            findNavController().navigate(action)
        }

        binding.GeneralSecretary.setOnClickListener {
            val action = ElectionFragmentDirections.actionElectionFragmentToVotingFragment(position = "General Secretary", role = Userrole)
            findNavController().navigate(action)
        }

        binding.JointSecretary.setOnClickListener {
            val action = ElectionFragmentDirections.actionElectionFragmentToVotingFragment(position = "Joint Secretary", role = Userrole)
            findNavController().navigate(action)
        }
        binding.Treasurer.setOnClickListener {
            val action = ElectionFragmentDirections.actionElectionFragmentToVotingFragment(position = "Treasurer", role = Userrole)
            findNavController().navigate(action)
        }

        binding.SportsSecretary.setOnClickListener {
            val action = ElectionFragmentDirections.actionElectionFragmentToVotingFragment(position = "SportsSecretary", role = Userrole)
            findNavController().navigate(action)
        }

        binding.btnEditTiming.setOnClickListener {
            showEditTimingDialog()
        }

        return view
    }


    private fun fetchElectionTiming() {
        db.collection("elections").document("electionInfo")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val startTime = document.getTimestamp("startTime")?.toDate()
                    val endTime = document.getTimestamp("endTime")?.toDate()

                    if (startTime != null && endTime != null) {
                        startCountdownTimer(startTime, endTime)
                    } else {
                        binding.tvElectionTime.text = "Election timing not available"
                    }
                } else {
                    binding.tvElectionTime.text = "Election timing not available"
                }
            }
    }

    private fun startCountdownTimer(startTime: Date, endTime: Date) {
        val currentTime = Date()

        if (currentTime < startTime) {
            binding.tvElectionTime.text = ""

            object : CountDownTimer(startTime.time - currentTime.time, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val days = millisUntilFinished / (1000 * 60 * 60 * 24)
                    val hours = (millisUntilFinished / (1000 * 60 * 60)) % 24
                    val minutes = (millisUntilFinished / (1000 * 60)) % 60
                    val seconds = (millisUntilFinished / 1000) % 60

                    binding.tvElectionTime.text ="$days         $hours          $minutes                $seconds  "
                }

                override fun onFinish() {
                    startCountdownTimer(startTime, endTime)
                }
            }.start()

        } else if (currentTime < endTime) {

            object : CountDownTimer(endTime.time - currentTime.time, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val days = millisUntilFinished / (1000 * 60 * 60 * 24)
                    val hours = (millisUntilFinished / (1000 * 60 * 60)) % 24
                    val minutes = (millisUntilFinished / (1000 * 60)) % 60
                    val seconds = (millisUntilFinished / 1000) % 60

                    binding.tvElectionTime.text="$days         $hours          $minutes                $seconds  "
                }

                override fun onFinish() {
                    binding.tvElectionTime.text = "Election has ended"
                }
            }.start()

        } else {
            binding.tvElectionTime.text = "Election has ended"
        }
    }


    fun getUserRole(){
        val currentUser = FirebaseAuth.getInstance().currentUser

        currentUser?.uid?.let { uid ->
            FirebaseFirestore.getInstance().collection("Users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val role = document.getString("role") ?: "Student"
                        Userrole = role
                        if(role == "Admin"){
                            binding.btnEditTiming.visibility = View.VISIBLE
                        }else{
                            binding.btnEditTiming.visibility = View.GONE
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e("ElectionFragment", "Error fetching user role", it)
                }
        }
    }


    private fun showEditTimingDialog() {
        val dialog = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_timing, null)
        val etStartTime = dialogView.findViewById<EditText>(R.id.etStartTime)
        val etEndTime = dialogView.findViewById<EditText>(R.id.etEndTime)

        dialog.setView(dialogView)
        dialog.setPositiveButton("Save") { _, _ ->
            val startTime = Timestamp(Date(etStartTime.text.toString().toLong()))
            val endTime = Timestamp(Date(etEndTime.text.toString().toLong()))

            db.collection("elections").document("electionInfo")
                .set(mapOf("startTime" to startTime, "endTime" to endTime))
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Election timing updated", Toast.LENGTH_SHORT).show()
                    fetchElectionTiming()
                }
        }
        dialog.setNegativeButton("Cancel", null)
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}