package xcom.niteshray.apps.collegeapp.UiScreens.elections

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import xcom.niteshray.apps.collegeapp.R
import xcom.niteshray.apps.collegeapp.databinding.FragmentVotingBinding
import xcom.niteshray.apps.collegeapp.model.Candidate
import xcom.niteshray.apps.collegeapp.model.User
import xcom.niteshray.apps.collegeapp.utils.EncryptionUtil

class VotingFragment : Fragment() {
    private var _binding: FragmentVotingBinding? = null
    private val binding get() = _binding!!

    private var isElection = false

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    val args: VotingFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVotingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkElectionStatus()

        binding.textView.text = "${args.position} Candidates"
        if (args.role =="Admin"){
            binding.floatingActionButton.visibility = View.VISIBLE
        }
        binding.candidateRecycler.layoutManager = LinearLayoutManager(requireContext())

        if(args.isElection){
            fetchElectionCounts()
        }else{
            binding.tvTotalVotersCount.text = "Available Soon..........."
        }

        fetchCandidates()


        binding.floatingActionButton.setOnClickListener {
            showAddCandidateDialog()
        }
    }

    private fun fetchCandidates() {
        db.collection("elections")
            .document(args.position)
            .collection("candidates")
            .get()
            .addOnSuccessListener { documents ->
                val candidates = mutableListOf<Candidate>()
                for (document in documents) {
                    val candidate = document.toObject(Candidate::class.java)
                    candidates.add(candidate)
                }
                binding.candidateRecycler.adapter = VotingAdapter(requireContext(), candidates, isElection = isElection) { candidate ->
                    checkIfUserVoted(candidate)
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to fetch candidates", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkElectionStatus() {
        db.collection("elections").document("electionInfo")
            .addSnapshotListener { document, error ->
                if (error != null) {
                    Log.e("VotingFragment", "Error fetching election status", error)
                    return@addSnapshotListener
                }

                if (document != null && document.exists()) {
                    val isCompleted = document.getBoolean("isElectionCompleted") ?: false
                    isElection = isCompleted
                    if (isCompleted) {
                        fetchElectionCounts()
                    } else {
                        binding.tvTotalVotersCount.text = "Available Soon..........."
                    }
                }
            }
    }


    private fun checkIfUserVoted(candidate: Candidate) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Please log in to vote", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUserEncrypt = EncryptionUtil.encrypt(currentUser.uid)
        val candidateRef = db.collection("elections")
            .document(args.position)
            .collection("total_voters")
            .document("votersdocumentid")

        candidateRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val votedUsers = document.get("votedUsers") as? List<String> ?: emptyList()
                if (currentUserEncrypt in votedUsers) {
                    Toast.makeText(requireContext(), "You have already voted!", Toast.LENGTH_SHORT).show()
                } else {
                    castVote(candidate)
                }
            }
        }
    }

    fun fetchElectionCounts(){
        db.collection("elections")
            .document(args.position)
            .collection("total_voters")
            .get().addOnSuccessListener { documents ->
                val count = documents.size()
                binding.tvTotalVotersCount.text = count.toString()
            }
    }

    private fun castVote(candidate: Candidate) {
        val currentUser = auth.currentUser ?: return

        val currentUserEncrypt = EncryptionUtil.encrypt(currentUser.uid)
        val candidateRef = db.collection("elections")
            .document(args.position)
            .collection("candidates")
            .document(candidate.candidateId)

        val allVoteref = db.collection("elections")
            .document(args.position)
            .collection("total_voters")
            .document("votersdocumentid")

        candidateRef.update("votedUsers", FieldValue.arrayUnion(currentUserEncrypt))
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Voted Successfullly!", Toast.LENGTH_SHORT).show()
                fetchCandidates()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to vote", Toast.LENGTH_SHORT).show()
            }

        allVoteref.update("votedUsers",FieldValue.arrayUnion(currentUserEncrypt)).addOnSuccessListener {
            Toast.makeText(requireContext(), "Voted Successfullly! voted", Toast.LENGTH_LONG).show()
            fetchCandidates()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to vote", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddCandidateDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val inputView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_bio, null)
        builder.setView(inputView)
        val dialog = builder.create()

        val username = inputView.findViewById<EditText>(R.id.etUsername)
        val profileimg = inputView.findViewById<ImageView>(R.id.profilepic)
        val name = inputView.findViewById<TextView>(R.id.name)
        val btnSearch = inputView.findViewById<Button>(R.id.btnSearch)
        val progressBar = inputView.findViewById<ProgressBar>(R.id.progressBar)
        val btnAdd = inputView.findViewById<Button>(R.id.btn_add)
        val btnCancel = inputView.findViewById<Button>(R.id.btn_cancel)
        val etBio = inputView.findViewById<TextInputEditText>(R.id.etbio)
        val linearlayout2 = inputView.findViewById<LinearLayout>(R.id.linearLayout2)
        val linearlayout3 = inputView.findViewById<LinearLayout>(R.id.linearlayout3)

        var user: User? = null

        btnSearch.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            val usersRef = db.collection("Users")
            usersRef.whereEqualTo("name", username.text.toString())
                .get()
                .addOnSuccessListener { documents ->
                    progressBar.visibility = View.GONE
                    if (!documents.isEmpty) {
                        user = documents.documents[0].toObject(User::class.java)
                        Glide.with(requireContext()).load(user!!.profilePic).into(profileimg)
                        name.text = user!!.name
                        etBio.visibility = View.VISIBLE
                        btnAdd.visibility = View.VISIBLE
                        linearlayout2.visibility = View.VISIBLE
                        linearlayout3.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(requireContext(), "User not found!", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error searching user", Toast.LENGTH_SHORT).show()
                }
        }

        btnAdd.setOnClickListener {
            if (etBio.text.toString().isNotBlank() && user != null) {
                dialog.dismiss()
                addCandidate(user!!, etBio.text.toString())
            } else {
                Toast.makeText(requireContext(), "Add Some Bio", Toast.LENGTH_LONG).show()
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun addCandidate(user: User, bio: String) {
        val candidate = Candidate(
            candidateId = user.authId,
            name = user.name,
            profilePic = user.profilePic,
            bio = bio,
            votedUsers = emptyList()
        )

        val candidateRef = db.collection("elections")
            .document(args.position)
            .collection("candidates")
            .document(user.authId)

        candidateRef.set(candidate).addOnSuccessListener {
            Toast.makeText(context, "Candidate added successfully!", Toast.LENGTH_SHORT).show()
            fetchCandidates()
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to add candidate!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
