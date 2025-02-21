package xcom.niteshray.apps.collegeapp.UiScreens.Cheaters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import xcom.niteshray.apps.collegeapp.R
import xcom.niteshray.apps.collegeapp.model.CheatingRecord

class CheatingAdapter(
    private val context: Context,
    private val cheatingList: List<CheatingRecord>,
    private val viewProof: (String) -> Unit,
    private val onAppealClick: (CheatingRecord) -> Unit
) : RecyclerView.Adapter<CheatingAdapter.ViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val studentimg : ImageView = itemView.findViewById(R.id.cheater_img)
        val studentName: TextView = itemView.findViewById(R.id.cheater_name)
        val reason: TextView = itemView.findViewById(R.id.tv_reason)
        val proofBtn: Button = itemView.findViewById(R.id.btn_viewProof)
        val appealBtn: Button = itemView.findViewById(R.id.btn_appeal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_cheaters, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = cheatingList[position]

        holder.studentName.text = record.studentName
        holder.reason.text = record.reason
        Glide.with(context).load(record.studentImg).into(holder.studentimg)

        holder.proofBtn.setOnClickListener {
            viewProof(record.proofUrl)
        }

        if (record.studentid == currentUserId && record.appealStatus == "Pending") {
            holder.appealBtn.visibility = View.VISIBLE
            holder.appealBtn.setOnClickListener {
                onAppealClick(record)
            }
        } else {
            holder.appealBtn.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = cheatingList.size
}
