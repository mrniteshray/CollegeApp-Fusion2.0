package xcom.niteshray.apps.collegeapp.UiScreens.Cheaters

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import xcom.niteshray.apps.collegeapp.R
import xcom.niteshray.apps.collegeapp.api.RetrofitInstance
import xcom.niteshray.apps.collegeapp.databinding.FragmentCheatersBinding
import xcom.niteshray.apps.collegeapp.model.CheatingRecord
import xcom.niteshray.apps.collegeapp.model.User
import java.io.File
import java.util.UUID

class CheatersFragment : Fragment() {

    lateinit var proofImgUri: Uri
    val pickImgLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                proofImgUri = data?.data!!
                Toast.makeText(requireContext(), "Image Selected", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(requireContext(), "No Image Selected", Toast.LENGTH_SHORT).show()
            }
        }

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var binding: FragmentCheatersBinding

    private lateinit var currentusr : User
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCheatersBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchCurrentUser()
        setUpRecyclerView()

        binding.floatingActionButton.setOnClickListener{
            showAddDialog()
        }
    }

    private fun fetchCurrentUser() {
        auth.currentUser?.let { db.collection("Users").document(it.uid).get().addOnSuccessListener {
            currentusr = it.toObject(User::class.java)!!
            if(currentusr.role == "Admin") {
                binding.floatingActionButton.visibility = View.VISIBLE
            }else{
                binding.floatingActionButton.visibility = View.GONE
            }
        } }
    }

    fun setUpRecyclerView(){
        binding.rec.layoutManager = LinearLayoutManager(requireContext())
        db.collection("CheatingRecords").get().addOnSuccessListener { documents ->
            val cheatingList = mutableListOf<CheatingRecord>()
            for (document in documents) {
                val cheatingRecord = document.toObject(CheatingRecord::class.java)
                cheatingList.add(cheatingRecord)
            }
            binding.rec.adapter = CheatingAdapter(requireContext(),cheatingList,{ url ->
                openProofDialog(url)
            }){
                Toast.makeText(requireContext(), "Appeal Clicked", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAddDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val inputView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_cheaters, null)
        builder.setView(inputView)
        val dialog = builder.create()

        val username = inputView.findViewById<EditText>(R.id.etUsername)
        val etus = inputView.findViewById<TextInputLayout>(R.id.etus)
        val profileimg = inputView.findViewById<ImageView>(R.id.profilepic)
        val name = inputView.findViewById<TextView>(R.id.name)
        val btnSearch = inputView.findViewById<Button>(R.id.btnSearch)
        val progressBar = inputView.findViewById<ProgressBar>(R.id.progressBar)
        val btnAdd = inputView.findViewById<Button>(R.id.btn_add)
        val btnAddProof = inputView.findViewById<Button>(R.id.btn_addProof)
        val btnCancel = inputView.findViewById<Button>(R.id.btn_cancel)
        val etBio = inputView.findViewById<TextInputEditText>(R.id.etbio)
        val linearLayout = inputView.findViewById<LinearLayout>(R.id.linearLayout2)
        val linearLayout3 = inputView.findViewById<LinearLayout>(R.id.linearlayout3)

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
                        linearLayout.visibility = View.VISIBLE
                        linearLayout3.visibility = View.VISIBLE
                        btnAddProof.visibility = View.VISIBLE
                        name.text = user!!.name
                        etBio.visibility = View.VISIBLE
                        btnAdd.visibility = View.VISIBLE
                        btnSearch.visibility = View.GONE
                        etus.visibility = View.GONE
                    } else {
                        Toast.makeText(requireContext(), "User not found!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                .addOnFailureListener {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error searching user", Toast.LENGTH_SHORT)
                        .show()
                }
        }

        btnAddProof.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImgLauncher.launch(intent)
        }

        btnAdd.setOnClickListener {
            if (etBio.text.toString().isNotBlank() && user != null && proofImgUri != null) {
                addCheater(user!!, etBio.text.toString(), proofImgUri)
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Add some reason", Toast.LENGTH_LONG).show()
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun addCheater(user: User, reason: String, proofImgUri: Uri) {
        val base64Image = getBase64StringFromUri(proofImgUri) ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.imgbbApi().uploadImage(
                    "01229b2dc3185eb6de4bb0dafe8f438a",
                    base64Image
                )

                if (response.isSuccessful) {
                    val imageUrl = response.body()?.data?.url
                    Log.d("Cheating", "Success: $imageUrl")
                    saveToFirestore(user, reason, imageUrl)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("Cheating", "Error: $errorBody")
                }
            } catch (e: Exception) {
                Log.e("Cheating", "Exception: ${e.message}")
            }
        }
    }

    private fun saveToFirestore(user: User, reason: String, imageUrl: String?) {
        val cheater = CheatingRecord(
            id = UUID.randomUUID().toString(),
            studentImg = user.profilePic,
            studentid = user.authId,
            studentName = user.name,
            reason = reason,
            proofUrl = imageUrl ?: "",
            reportedBy = currentusr.name,
            reportedById = currentusr.authId,
            timestamp = System.currentTimeMillis().toString(),
            appealStatus = "Pending",
            appealMessage = null,
            appealedBy = null
        )

        db.collection("CheatingRecords")
            .add(cheater)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Cheater added", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getBase64StringFromUri(uri: Uri): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e("Base64Error", "Error converting image: ${e.message}")
            null
        }
    }

    private fun openProofDialog(url : String){
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.proof_dialog, null)
        builder.setView(dialogView)
        val dialog = builder.create()
        val proofImg = dialogView.findViewById<ImageView>(R.id.proof_img)
        Glide.with(requireContext()).load(url).into(proofImg)
        dialog.show()
    }


}
