package xcom.niteshray.apps.collegeapp.UiScreens.Budgets

import android.app.Activity
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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xcom.niteshray.apps.collegeapp.R
import xcom.niteshray.apps.collegeapp.api.RetrofitInstance
import xcom.niteshray.apps.collegeapp.databinding.FragmentEventDetailsBinding
import xcom.niteshray.apps.collegeapp.model.Expense
import java.util.UUID

class EventDetailsFragment : Fragment() {
    private lateinit var binding : FragmentEventDetailsBinding

    lateinit var photourl : String
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
                        photourl = response.body()?.data?.url.toString()
                        Toast.makeText(requireContext(), "Image Uploaded", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Image Upload ", Toast.LENGTH_SHORT).show()
                        Log.e("ImageUploadError", "Error uploading image: ${response.errorBody()?.string()}")
                    }
                } catch (e: Exception) {
                }
            }
            if (eventImgUri != null) {

            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEventDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = EventDetailsFragmentArgs.fromBundle(requireArguments())
        val event = args.event
        Toast.makeText(requireContext(), "Event Name: ${event.title}", Toast.LENGTH_SHORT).show()

        binding.title.text = event.title
        binding.totalAmount.text = "$"+event.totalBudget.toString()

        binding.sponsorRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.sponsorRecycler.adapter = SponsorAdapter(requireContext(),event.sponsorships)

        binding.Expenserec.layoutManager = LinearLayoutManager(requireContext())
        binding.Expenserec.adapter = ExpenseAdapter(requireContext(),event.expenses)

        binding.btnAdExpense.setOnClickListener {
            showAddExpenseDialog(event.id)
        }
    }

    fun showAddExpenseDialog(eventId: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_expense, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val expenseItem = dialogView.findViewById<EditText>(R.id.expenseItem)
        val expenseCost = dialogView.findViewById<EditText>(R.id.expenseCost)
        val uploadimg = dialogView.findViewById<Button>(R.id.proofUrl)
        val addExpenseBtn = dialogView.findViewById<Button>(R.id.addExpenseBtn)

        uploadimg.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImageLauncher.launch(intent)
        }

        addExpenseBtn.setOnClickListener {
            val itemName = expenseItem.text.toString().trim()
            val cost = expenseCost.text.toString().trim().toIntOrNull()

            if (itemName.isEmpty() || cost == null) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newExpense = Expense(
                id = UUID.randomUUID().toString(),
                item = itemName,
                cost = cost,
                proofUrl = photourl,
                approved = false
            )

            addExpenseToFirestore(eventId, newExpense)
            dialog.dismiss()
        }

        dialog.show()
    }

    fun addExpenseToFirestore(eventId: String, expense: Expense) {
        val db = FirebaseFirestore.getInstance()
        val eventRef = db.collection("events").document(eventId)

        eventRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val currentBudget = document.getLong("totalBudget") ?: 0

                if (currentBudget < expense.cost) {
                    Toast.makeText(requireContext(), "Not enough budget!", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val updatedBudget = currentBudget - expense.cost

                eventRef.update(
                    mapOf(
                        "expenses" to FieldValue.arrayUnion(expense),
                        "totalBudget" to updatedBudget
                    )
                ).addOnSuccessListener {
                    Toast.makeText(requireContext(), "Expense added & budget updated!", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to update: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Event not found!", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to fetch event data", Toast.LENGTH_SHORT).show()
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

}