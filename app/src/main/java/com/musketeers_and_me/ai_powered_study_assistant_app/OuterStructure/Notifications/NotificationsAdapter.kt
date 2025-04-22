import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.NotificationItem
import com.musketeers_and_me.ai_powered_study_assistant_app.R

class NotificationsAdapter(
    private val notifications: MutableList<NotificationItem>
) : RecyclerView.Adapter<NotificationsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.titleText)
        val descriptionText: TextView = itemView.findViewById(R.id.descriptionText)
        val closeButton: ImageView = itemView.findViewById(R.id.closeButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = notifications.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = notifications[position]
        holder.titleText.text = item.title
        holder.descriptionText.text = item.description

        holder.closeButton.setOnClickListener {
            notifications.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, notifications.size)
        }
    }
}
