package xcom.niteshray.apps.collegeapp.UiScreens.Budgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import xcom.niteshray.apps.collegeapp.R
import xcom.niteshray.apps.collegeapp.model.Event


class EventAdapter(private val context: Context, private val eventImages: List<Event>, private val onItemClick: (Event) -> Unit) :
    RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventImageView: ImageView = itemView.findViewById(R.id.event_img)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val eventImage = eventImages[position]
        Glide.with(context).load(eventImage.eventImg).into(holder.eventImageView)

        holder.itemView.setOnClickListener {
            onItemClick(eventImage)
        }
    }
    override fun getItemCount(): Int = eventImages.size
}