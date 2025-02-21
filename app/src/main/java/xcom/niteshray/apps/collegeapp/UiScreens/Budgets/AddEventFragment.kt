package xcom.niteshray.apps.collegeapp.UiScreens.Budgets

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xcom.niteshray.apps.collegeapp.R
import xcom.niteshray.apps.collegeapp.api.RetrofitInstance
import xcom.niteshray.apps.collegeapp.databinding.FragmentAddEventBinding
import xcom.niteshray.apps.collegeapp.model.ClubMembers
import xcom.niteshray.apps.collegeapp.model.Event
import xcom.niteshray.apps.collegeapp.model.Roles
import xcom.niteshray.apps.collegeapp.model.Sponsor
import java.util.UUID


class AddEventFragment : Fragment() {

    private lateinit var _binding: FragmentAddEventBinding
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()

    private val sponsorsList = mutableListOf<Sponsor>()
    private val membersList = mutableListOf<ClubMembers>()
    private var BannerImgUri : String = ""

    val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val eventImgUri = data?.data
            val base64Image = eventImgUri?.let { getBase64StringFromUri(it) } ?: return@registerForActivityResult
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitInstance.imgbbApi().uploadImage(
                        "01229b2dc3185eb6de4bb0dafe8f438a",
                        base64Image
                    )
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Image Uploaded", Toast.LENGTH_SHORT).show()
                        BannerImgUri = response.body()?.data?.url.toString()
                    } else {
                        Toast.makeText(requireContext(), "Image Upload ", Toast.LENGTH_SHORT).show()
                        Log.e("ImageUploadError", "Error uploading image: ${response.errorBody()?.string()}")
                    }
                } catch (e: Exception) {
                }
            }
            if (eventImgUri != null) {
                binding.eventBanner.setImageURI(eventImgUri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.eventBanner.setOnClickListener {
            openImagePicker()
        }
        binding.addSponsorshipBtn.setOnClickListener {
            showAddSponsorDialog()
        }

        binding.addclubmembers.setOnClickListener {
            showAddMemberDialog()
        }

        binding.submitEventBtn.setOnClickListener {
            saveEventToFirestore()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun showAddSponsorDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.addsponsor_dialog, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()

        val sponsorName = dialogView.findViewById<EditText>(R.id.sponsorName)
        val sponsorAmount = dialogView.findViewById<EditText>(R.id.sponsorAmount)
        val addSponsorBtn = dialogView.findViewById<Button>(R.id.addSponsorBtn)

        addSponsorBtn.setOnClickListener {
            val name = sponsorName.text.toString()
            val amount = sponsorAmount.text.toString().toIntOrNull()

            if (name.isEmpty() || amount == null || amount <= 0) {
                Toast.makeText(requireContext(), "Invalid Sponsor Data!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sponsor = Sponsor(name,amount)
            sponsorsList.add(sponsor)

            Toast.makeText(requireContext(), "Sponsor Added!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showAddMemberDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_member, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()

        val memberName = dialogView.findViewById<EditText>(R.id.memberName)
        val memberEmail = dialogView.findViewById<EditText>(R.id.memberEmail)
        val addMemberBtn = dialogView.findViewById<Button>(R.id.addMemberBtn)

        addMemberBtn.setOnClickListener {
            val name = memberName.text.toString()
            val email = memberEmail.text.toString()

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(requireContext(), "Invalid Member Data!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val member = ClubMembers(name,email)
            membersList.add(member)

            Toast.makeText(requireContext(), "Member Added!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
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

    private fun saveEventToFirestore() {
        val name = binding.eventName.text.toString()
        val club = binding.clubName.text.toString()
        val budgetAmount = binding.budget.text.toString().toIntOrNull() ?: 0
        val roles = Roles(listOf(),membersList)

        if (name.isEmpty() || club.isEmpty() || budgetAmount == 0) {
            Toast.makeText(requireContext(), "Please fill all fields!", Toast.LENGTH_SHORT).show()
            return
        }

        val event = Event(
            id = UUID.randomUUID().toString(),
            title = name,
            clubId = club,
            eventImg = BannerImgUri,
            totalBudget = budgetAmount,
            sponsorships = sponsorsList,
            expenses = listOf(),
            roles = roles,
            timestamp = FieldValue.serverTimestamp().toString()
        )

        db.collection("events").add(event)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Event Created!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}