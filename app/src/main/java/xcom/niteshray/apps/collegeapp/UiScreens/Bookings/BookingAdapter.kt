package xcom.niteshray.apps.collegeapp.UiScreens.Bookings

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import xcom.niteshray.apps.collegeapp.R
import xcom.niteshray.apps.collegeapp.model.FacilityRequest

class BookingAdapter(
    private val context: Context,
    private val bookingList: List<FacilityRequest>,
    private val currentUserId: String
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    class BookingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val facilityName: TextView = view.findViewById(R.id.facilityName)
        val status: TextView = view.findViewById(R.id.status)
        val actionButtons: LinearLayout = view.findViewById(R.id.actionButtons)
        val btnApprove: Button = view.findViewById(R.id.btnApprove)
        val btnReject: Button = view.findViewById(R.id.btnReject)
        val facilityImage: ImageView = view.findViewById(R.id.facilityImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookingList[position]
        holder.facilityName.text = booking.facilityName
        holder.status.text = " Status: "+booking.status
        Glide.with(context).load(booking.facilityImage).into(holder.facilityImage)

        if (booking.upperAuthority == currentUserId && booking.status == "pending") {
            holder.actionButtons.visibility = View.VISIBLE
        } else {
            holder.actionButtons.visibility = View.GONE
        }

        holder.btnApprove.setOnClickListener {
            updateApprovalStatus(booking.requestId, true)
        }

        holder.btnReject.setOnClickListener {
            showRejectDialog(booking.requestId)
        }
    }

    override fun getItemCount(): Int = bookingList.size

    private fun updateApprovalStatus(requestId: String, isApproved: Boolean) {
        val db = FirebaseFirestore.getInstance()
        val bookingRef = db.collection("Bookings").document(requestId)

        bookingRef.get()
            .addOnSuccessListener { snapshot ->
                val booking = snapshot.toObject(FacilityRequest::class.java)

                if (booking != null && booking.status == "pending") {
                    if (isApproved) {
                        booking.upperAuthority?.let { upperAuthority ->
                            db.collection("Users").document(upperAuthority)
                                .get()
                                .addOnSuccessListener { document ->
                                    val nextApprover = document.getString("upperAuthority")
                                    if (nextApprover != "") {
                                        bookingRef.update("upperAuthority", nextApprover)
                                            .addOnSuccessListener {
                                                Toast.makeText(context, "Updated Successfully", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener { exception ->
                                                Toast.makeText(context, "Update Failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    } else {
                                        bookingRef.update("status", "approved")
                                            .addOnSuccessListener {
                                                Toast.makeText(context, "Updated Successfully", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener { exception ->
                                                Toast.makeText(context, "Update Failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(context, "Failed to get user data: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }
                        } ?: run {
                            bookingRef.update("status", "approved")
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Updated Successfully", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(context, "Update Failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        bookingRef.update("status", "rejected")
                            .addOnSuccessListener {
                                Toast.makeText(context, "Updated Successfully", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(context, "Update Failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Failed to get booking data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showRejectDialog(requestId: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Reject Booking")
        val input = EditText(context)
        input.hint = "Enter reason for rejection"
        builder.setView(input)

        builder.setPositiveButton("Reject") { _, _ ->
            val reason = input.text.toString()
            if (reason.isNotEmpty()) {
                FirebaseFirestore.getInstance().collection("FacilityRequests")
                    .document(requestId)
                    .update(mapOf("status" to "rejected", "rejectionReason" to reason))
                    .addOnSuccessListener {
                        Toast.makeText(context, "Rejected Successfully", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
    }
}
