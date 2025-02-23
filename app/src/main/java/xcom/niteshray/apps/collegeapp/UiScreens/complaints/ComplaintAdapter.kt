package xcom.niteshray.apps.collegeapp.UiScreens.complaints

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import xcom.niteshray.apps.collegeapp.R
import xcom.niteshray.apps.collegeapp.model.Complaint
import xcom.niteshray.apps.collegeapp.utils.EncryptionUtil

class ComplaintAdapter(
    private val context: Context,
    private val role: String,
    private var complaints: List<Complaint>
) : RecyclerView.Adapter<ComplaintAdapter.ComplaintViewHolder>() {
    private val currentuser = FirebaseAuth.getInstance().currentUser?.uid
    inner class ComplaintViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewComplaint: TextView = itemView.findViewById(R.id.tv_complaint_text)
        val textViewUser: TextView = itemView.findViewById(R.id.tv_complaint_user)
        val voteToReveal: Button = itemView.findViewById(R.id.btn_voteReveal)
        val resolvedBtn: Button = itemView.findViewById(R.id.btn_resolved)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComplaintViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_complaint, parent, false)
        return ComplaintViewHolder(view)
    }
    override fun onBindViewHolder(holder: ComplaintViewHolder, position: Int) {
        val complaint = complaints[position]
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val complaintRef = FirebaseFirestore.getInstance()
            .collection("complaints")
            .document(complaint.department)
            .collection("complaints")
            .document(complaint.id)

        val encryptedUserid = currentuser?.let { EncryptionUtil.encrypt(it) }
        if (complaint.userId == encryptedUserid){
            if (complaint.isResolved){
                holder.resolvedBtn.visibility = View.GONE
            }else{
                holder.resolvedBtn.visibility = View.VISIBLE
            }
        }

        holder.textViewComplaint.text = complaint.text

        holder.textViewUser.text = if (complaint.anonymous) "Anonymous" else EncryptionUtil.decrypt(complaint.originalUsername)

        holder.voteToReveal.visibility = if (role == "Student") View.GONE else View.VISIBLE

        complaintRef.get().addOnSuccessListener { document ->
            val revealRequests = document.get("votesToReveal") as? List<String> ?: emptyList()
            val resolved = document.getBoolean("resolved") ?: false
            val totalBoardMembers = 10
            val majority = (totalBoardMembers / 2) + 1

            if (revealRequests.contains(uid)) {
                holder.voteToReveal.text = "Voted"
                holder.voteToReveal.setBackgroundColor(Color.parseColor("#ADD8E6")) // Light Blue
                holder.voteToReveal.isEnabled = false
            } else {
                holder.voteToReveal.text = "Vote to Reveal"
                holder.voteToReveal.isEnabled = true
            }

            if (resolved) {
                holder.resolvedBtn.text = "Resolved"
                holder.resolvedBtn.setBackgroundColor(Color.GRAY)
                holder.resolvedBtn.isEnabled = false
            } else {
                holder.resolvedBtn.text = "Mark as Resolved"
                holder.resolvedBtn.isEnabled = true
            }
        }

        // **Handle Vote to Reveal Click**
        holder.voteToReveal.setOnClickListener {
            complaintRef.update("votesToReveal", FieldValue.arrayUnion(uid))
                .addOnSuccessListener {
                    holder.voteToReveal.text = "Voted"
                    holder.voteToReveal.setBackgroundColor(Color.parseColor("#ADD8E6"))
                    holder.voteToReveal.isEnabled = false

                    complaintRef.get().addOnSuccessListener { document ->
                        val revealRequests = document.get("votesToReveal") as? List<String> ?: emptyList()

                        if (revealRequests.size >= (10 / 2) + 1) {
                            complaintRef.update("anonymous", false)
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Vote failed", Toast.LENGTH_SHORT).show()
                }
        }

        holder.resolvedBtn.setOnClickListener {
            complaintRef.update("isResolved", true)
                .addOnSuccessListener {
                    holder.resolvedBtn.text = "Resolved"
                    holder.resolvedBtn.setBackgroundColor(Color.GRAY)
                    holder.resolvedBtn.isEnabled = false
                    Toast.makeText(context, "Complaint marked as resolved", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to resolve complaint", Toast.LENGTH_SHORT).show()
                }
        }
    }


    override fun getItemCount(): Int = complaints.size

    fun updateComplaints(newComplaints: List<Complaint>) {
        complaints = newComplaints
        notifyDataSetChanged()
    }
}
