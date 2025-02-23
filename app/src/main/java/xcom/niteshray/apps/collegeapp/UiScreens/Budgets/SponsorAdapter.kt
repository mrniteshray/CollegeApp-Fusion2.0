package xcom.niteshray.apps.collegeapp.UiScreens.Budgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import xcom.niteshray.apps.collegeapp.R
import xcom.niteshray.apps.collegeapp.model.Sponsor

class SponsorAdapter(private val context: Context, private val Sponsors: List<Sponsor>) :
    RecyclerView.Adapter<SponsorAdapter.SponsorViewHolder>() {

    class SponsorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sponsorname = itemView.findViewById<TextView>(R.id.sponsorname)
        val sponsoramount = itemView.findViewById<TextView>(R.id.sponsor_amount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SponsorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sponsors, parent, false)
        return SponsorViewHolder(view)
    }

    override fun onBindViewHolder(holder: SponsorViewHolder, position: Int) {
        val current = Sponsors[position]
        holder.sponsorname.text = current.sponsor.toString()
        holder.sponsoramount.text = "+$"+current.amount.toString()
    }
    override fun getItemCount(): Int = Sponsors.size
}