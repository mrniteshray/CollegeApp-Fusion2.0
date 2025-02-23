package xcom.niteshray.apps.collegeapp.UiScreens

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import xcom.niteshray.apps.collegeapp.R
import xcom.niteshray.apps.collegeapp.model.Appointment

class DoctorAdapter(val regisList : List<Appointment>, val onAppointmentClick : (Appointment) -> Unit) : RecyclerView.Adapter<DoctorAdapter.DoctorHolder>() {
    inner class DoctorHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val registerId: TextView = itemView.findViewById(R.id.register)
        val time : TextView =  itemView.findViewById(R.id.appointTime)
        val reason : TextView = itemView.findViewById(R.id.reason)
        val btn : TextView = itemView.findViewById(R.id.writePrecript)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorHolder{
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_doctor_booking, parent, false)
        return DoctorHolder(view)
    }

    override fun onBindViewHolder(holder : DoctorHolder, position: Int) {
        val register = regisList[position]
        holder.registerId.text = "Booking Id :"+register.bookingId
        holder.time.text = "Appointment Time :"+register.time
        holder.reason.text = "Reason :"+register.reason
        holder.btn.setOnClickListener{
            onAppointmentClick(register)
        }
    }

    override fun getItemCount(): Int = regisList.size
}
