package xcom.niteshray.apps.collegeapp.UiScreens.complaints

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import xcom.niteshray.apps.collegeapp.R
import xcom.niteshray.apps.collegeapp.databinding.FragmentComplaintBinding
import xcom.niteshray.apps.collegeapp.model.Complaint
import xcom.niteshray.apps.collegeapp.model.User
import xcom.niteshray.apps.collegeapp.utils.EncryptionUtil

class ComplaintFragment : Fragment() {
    private lateinit var binding: FragmentComplaintBinding
    private lateinit var complaintAdapter: ComplaintAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var  current : User

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentComplaintBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getUserRole(){ role ->
            setupRecyclerView(role)
            fetchComplaints()
        }
        binding.fabAddComplaint.setOnClickListener {
            showComplaintDialog()
        }
    }

    private fun setupRecyclerView(role : String) {
        complaintAdapter = ComplaintAdapter(requireContext(),role,listOf())
        binding.recyclerComplaints.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = complaintAdapter
        }
    }

    private fun fetchComplaints() {
        db.collectionGroup("complaints")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, _ ->
                Toast.makeText(requireContext(),"Fetched",Toast.LENGTH_LONG).show()
                snapshots?.let {
                    val complaints = it.toObjects(Complaint::class.java)
                    complaintAdapter.updateComplaints(complaints)
                }
            }
    }

    private fun showComplaintDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add__complaint, null)
        val editTextComplaint = dialogView.findViewById<EditText>(R.id.et_complaint_text)
        val checkBoxAnonymous = dialogView.findViewById<CheckBox>(R.id.checkbox_anonymous)
        val spinnerDepartment = dialogView.findViewById<Spinner>(R.id.spinner_department)
        val submitbtn = dialogView.findViewById<Button>(R.id.btnaddcomplaint)
        val cancelbtn = dialogView.findViewById<Button>(R.id.btn_cancel)


        val departments = listOf("Select Department", "Labs", "Transport", "Canteen", "Library", "Hostel", "Other")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, departments)
        spinnerDepartment.adapter = adapter
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        submitbtn.setOnClickListener {
            val complaintText = editTextComplaint.text.toString().trim()
            val isAnonymous = checkBoxAnonymous.isChecked
            val selectedDepartment = spinnerDepartment.selectedItem.toString()

            if(complaintText.isNotBlank()){
                checkForVulgarity(complaintText) { isClean ->
                    if (isClean) {
                        if (selectedDepartment =="Select Department"){
                            Toast.makeText(requireContext(),"Please Select department",Toast.LENGTH_LONG).show()
                        }else{
                            submitComplaint(complaintText, isAnonymous,selectedDepartment)
                        }
                        dialog.dismiss()
                    } else {
                        Toast.makeText(context, "Complaint contains inappropriate content!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                }
            }else{
                Toast.makeText(context, "Please enter a complaint", Toast.LENGTH_SHORT).show()
            }
        }
        cancelbtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun submitComplaint(text: String, isAnonymous: Boolean,selectedDept : String) {
        if (text.isBlank()) {
            Toast.makeText(context, "Please enter a complaint", Toast.LENGTH_SHORT).show()
            return
        }
        val complaintId = db.collection("complaints").document().id
        val encryptedUsername = EncryptionUtil.encrypt(current.name)
        val encryptedUserid = EncryptionUtil.encrypt(auth.currentUser!!.uid)
        val complaint = Complaint(
            id = complaintId,
            text = text,
            userId = encryptedUserid,
            originalUsername = encryptedUsername,
            timestamp = System.currentTimeMillis(),
            anonymous = isAnonymous,
            isResolved = false,
            department = selectedDept,
            votesToReveal = listOf()
        )
        db.collection("complaints").document(selectedDept).collection("complaints").document(complaintId).set(complaint)
            .addOnSuccessListener {
                Toast.makeText(context, "Complaint submitted", Toast.LENGTH_SHORT).show()
                complaintAdapter.notifyDataSetChanged()
            }
    }

    private fun checkForVulgarity(text: String, callback: (Boolean) -> Unit) {
        val bannedWords = listOf(
            "fuck", "shit", "bitch", "bastard", "asshole", "dumbass", "moron",
            "idiot", "stupid", "cunt", "pussy", "dick", "cock", "whore", "slut",
            "rape", "kill yourself", "suicide", "die", "faggot", "nigger", "chutiya",
            "madarchod", "bhosdike", "gaand", "lodu", "lavde", "randi", "randi ka bacha",
            "bc", "mc", "kutti", "kutte", "hijra", "meetha", "kamina", "kaminey",
            "harami", "nalayak", "bewakoof", "behenchod", "teri maa ki", "teri behen ki",
            "lund", "chodu", "tatti", "gaand mara", "jhantu", "jhant", "bakchod",
            "ghanta", "gandu", "chakka", "chichora", "chapri", "chhapri","Idiot","BullShit","idiot"
        )

        val words = text.lowercase().split(" ")
        val containsBadWord = words.any { it in bannedWords }

        callback(!containsBadWord)
    }

    fun getUserRole(done : (String) -> Unit){
        val auth = FirebaseAuth.getInstance().currentUser

        auth?.uid?.let { uid ->
            FirebaseFirestore.getInstance().collection("Users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    current = document.toObject(User::class.java)!!
                    if (document.exists()) {
                        val role = current.role
                        if (role.isNotBlank()){
                            done(role)
                        }
                        if(current.role == "Student"){
                            binding.fabAddComplaint.visibility = View.VISIBLE
                        }else{
                            binding.fabAddComplaint.visibility = View.GONE
                        }
                    }
                }
                .addOnFailureListener {
                }
        }
    }
}