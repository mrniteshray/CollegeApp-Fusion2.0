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
    private val onItemClick: (Facility) -> Unit
) : RecyclerView.Adapter<FacilitiesAdapter.FacilityViewHolder>() {

    inner class FacilityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val facilityImg = itemView.findViewById<ImageView>(R.id.facility_img)
        val facilityname = itemView.findViewById<TextView>(R.id.facilityName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacilityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_facilities, parent, false)
        return FacilityViewHolder(view)
    }

    override fun onBindViewHolder(holder: FacilityViewHolder, position: Int) {
        val current = FacilitiList[position]
        holder.facilityname.text = current.name
        Glide.with(context).load(current.bannerImage).into(holder.facilityImg)
        holder.itemView.setOnClickListener{
            onItemClick(current)
        }
    }

    override fun getItemCount(): Int = FacilitiList.size
}
