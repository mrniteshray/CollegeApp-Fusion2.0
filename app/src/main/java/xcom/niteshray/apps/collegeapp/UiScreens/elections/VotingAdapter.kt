package xcom.niteshray.apps.collegeapp.UiScreens.elections

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView
import xcom.niteshray.apps.collegeapp.R
import xcom.niteshray.apps.collegeapp.model.Candidate
import xcom.niteshray.apps.collegeapp.utils.EncryptionUtil

class VotingAdapter(
    private val context: Context,
    private var candidates: List<Candidate>,
    private val isElection : Boolean,
    private val onVoteClick: (Candidate) -> Unit
) : RecyclerView.Adapter<VotingAdapter.CandidateViewHolder>() {

    inner class CandidateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profilePic: CircleImageView = itemView.findViewById(R.id.img)
        val name: TextView = itemView.findViewById(R.id.nametv)
        val bio: TextView = itemView.findViewById(R.id.bio)
        val voteButton: Button = itemView.findViewById(R.id.vote)
        val votes : TextView = itemView.findViewById(R.id.votes)

        init {
            voteButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val candidate = candidates[position]
                    onVoteClick(candidate)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CandidateViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.candidate_itemview, parent, false)
        return CandidateViewHolder(view)
    }

    override fun onBindViewHolder(holder: CandidateViewHolder, position: Int) {
        val candidate = candidates[position]

        holder.name.text = candidate.name
        holder.bio.text = candidate.bio

        if (isElection){
            holder.votes.visibility = View.VISIBLE
            holder.votes.text = candidate.votedUsers.size.toString()
        }else{
            holder.votes.visibility = View.GONE
        }
        Glide.with(context)
            .load(candidate.profilePic)
            .placeholder(R.drawable.profileimg)
            .error(R.drawable.profileimg)
            .into(holder.profilePic)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        val currentUserEncrypt = EncryptionUtil.encrypt(userId)
        Log.d("CheckEncryption", "Encrypted: $currentUserEncrypt")

        if (currentUserEncrypt in candidate.votedUsers) {
            holder.voteButton.isEnabled = false
            holder.voteButton.text = "Voted"
            holder.voteButton.alpha = 0.5f
        } else {
            holder.voteButton.isEnabled = true
            holder.voteButton.text = "Vote"
            holder.voteButton.alpha = 1.0f
        }
    }

    override fun getItemCount(): Int = candidates.size

    fun updateCandidates(newCandidates: List<Candidate>) {
        candidates = newCandidates
        notifyDataSetChanged()
    }
}
