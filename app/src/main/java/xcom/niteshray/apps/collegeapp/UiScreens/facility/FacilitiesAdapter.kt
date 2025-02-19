package xcom.niteshray.apps.collegeapp.UiScreens.facility

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import xcom.niteshray.apps.collegeapp.R
import xcom.niteshray.apps.collegeapp.model.Facility

class FacilitiesAdapter(
    private val context: Context,
    private var FacilitiList: List<Facility>,
    private val onbtnClick: (Facility) -> Unit
) : RecyclerView.Adapter<FacilitiesAdapter.FacilityViewHolder>() {

    inner class FacilityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val facilityImg = itemView.findViewById<ImageView>(R.id.facility_img)
        val facilityname = itemView.findViewById<TextView>(R.id.facilityName)
        val availability = itemView.findViewById<TextView>(R.id.tv_availability)
        val btnBook = itemView.findViewById<TextView>(R.id.btn_Book)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacilityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_facilities, parent, false)
        return FacilityViewHolder(view)
    }

    override fun onBindViewHolder(holder: FacilityViewHolder, position: Int) {
        val current = FacilitiList[position]
        holder.facilityname.text = current.name
        Glide.with(context).load(current.FacilityImgUrl).into(holder.facilityImg)
        if (current.Availability){
            holder.availability.text  = "Availability : Available"
        }else{
            holder.availability.text  = "Availability : Booked"
            holder.btnBook.visibility = View.GONE
        }

        holder.btnBook.setOnClickListener {
            onbtnClick(current)
        }
    }

    override fun getItemCount(): Int = FacilitiList.size
}
