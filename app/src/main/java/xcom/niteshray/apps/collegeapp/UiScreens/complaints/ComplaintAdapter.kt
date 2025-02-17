package xcom.niteshray.apps.collegeapp.UiScreens.complaints

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import xcom.niteshray.apps.collegeapp.R
import xcom.niteshray.apps.collegeapp.model.Complaint

class ComplaintAdapter(val context : Context,
    val role : String,
    private var complaints: List<Complaint>
) : RecyclerView.Adapter<ComplaintAdapter.ComplaintViewHolder>() {
    inner class ComplaintViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewComplaint: TextView = itemView.findViewById(R.id.tv_complaint_text)
        val textViewUser: TextView = itemView.findViewById(R.id.tv_complaint_user)
        val votetorevel : Button = itemView.findViewById(R.id.btn_voteReveal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComplaintViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_complaint, parent, false)
        return ComplaintViewHolder(view)
    }

    override fun onBindViewHolder(holder: ComplaintViewHolder, position: Int) {
        val complaint = complaints[position]
        if(role=="Student"){
            holder.votetorevel.visibility = View.GONE
        }else{
            holder.votetorevel.visibility = View.VISIBLE
        }
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        holder.textViewComplaint.text = complaint.text
        if (complaint.anonymous){
            holder.textViewUser.text = complaint.userId
        }else{
            holder.textViewUser.text = complaint.originalUserId
        }
        val complaintRef = FirebaseFirestore.getInstance().collection("complaints").document(complaint.id)
        complaintRef.get().addOnSuccessListener { document ->
            val revealRequests = document.get("votesToReveal") as? List<String> ?: emptyList()
            val totalBoardMembers = 10
            val majority = (totalBoardMembers / 2) + 1

            if (revealRequests.contains(uid)) {
                holder.votetorevel.text = "Voted"
                holder.votetorevel.setBackgroundColor(Color.parseColor("#ADD8E6")) // Light Blue
                holder.votetorevel.isEnabled = false
                holder.votetorevel.visibility = View.VISIBLE
            } else {
                holder.votetorevel.text = "Vote to Reveal"
                holder.votetorevel.isEnabled = true

                holder.votetorevel.setOnClickListener {
                    if (uid != null) {
                        complaintRef.update("votesToReveal", FieldValue.arrayUnion(uid))
                            .addOnSuccessListener {
                                if (revealRequests.size + 1 >= majority) {
                                    complaintRef.update("anonymous", false) // Anonymous reveal
                                }
                                holder.votetorevel.text = "Voted"
                                holder.votetorevel.setBackgroundColor(Color.parseColor("#ADD8E6")) // Light Blue
                                holder.votetorevel.isEnabled = false
                            }
                    }
                }
            }
        }

    }

    override fun getItemCount(): Int = complaints.size

    fun updateComplaints(newComplaints: List<Complaint>) {
        complaints = newComplaints
        notifyDataSetChanged()
    }
}