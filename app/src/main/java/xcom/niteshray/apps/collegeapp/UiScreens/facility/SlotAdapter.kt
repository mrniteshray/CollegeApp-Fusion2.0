package xcom.niteshray.apps.collegeapp.UiScreens.facility

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import xcom.niteshray.apps.collegeapp.R
import xcom.niteshray.apps.collegeapp.model.Slot
import java.text.SimpleDateFormat
import java.util.Locale

class SlotAdapter(private val slots: List<Slot>) : RecyclerView.Adapter<SlotAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bookedBy: TextView = view.findViewById(R.id.tvBookedBy)
        val fromtv : TextView = view.findViewById(R.id.fromtv)
        val Totv : TextView = view.findViewById(R.id.Totv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_slot, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val slot = slots[position]
        val startTimeStamp = slot.startTime as Timestamp
        val endTimeStamp = slot.endtime as Timestamp

        val startTime = startTimeStamp.toDate()
        val endTime = endTimeStamp.toDate()
        val dateTimeFormat = SimpleDateFormat("EEE, MMM d, yyyy hh:mm a", Locale.getDefault())

        // Format for Date and Time (Example 2 - ISO 8601)
        // val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) // e.g., 2023-10-24 14:30:00

        // Format for Date and Time (Example 3 - 24-hour format)
        // val dateTimeFormat = SimpleDateFormat("EEE, MMM d, yyyy HH:mm", Locale.getDefault()) // e.g., Tue, Oct 24, 2023 14:30


        val startTimeString = dateTimeFormat.format(startTime)
        val endTimeString = dateTimeFormat.format(endTime)

        holder.fromtv.text = "From : $startTimeString"
        holder.Totv.text = "To : $endTimeString"
        holder.bookedBy.text = "Booked by: ${slot.bookedByName ?: "Unknown"}"
    }

    override fun getItemCount() = slots.size
}