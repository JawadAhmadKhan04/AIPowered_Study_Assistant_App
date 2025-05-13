import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.NoteItem
import com.musketeers_and_me.ai_powered_study_assistant_app.R

class NoteAdapter(
    private var originalNotes: List<NoteItem>,
    private val onClick: (NoteItem) -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    private var filteredNotes: List<NoteItem> = originalNotes

    class NoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val noteTitle: TextView = view.findViewById(R.id.note_title)
        val noteAge: TextView = view.findViewById(R.id.note_age)
        val noteType: TextView = view.findViewById(R.id.note_type)
        val noteTypeIcon: ImageView = view.findViewById(R.id.note_type_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = filteredNotes[position]
        holder.noteTitle.text = note.title
        holder.noteAge.text = note.age

        if (note.type == "text") {
            holder.noteType.text = "Text Note"
            holder.noteTypeIcon.setImageResource(R.drawable.notes)
        } else {
            holder.noteType.text = "Voice Note"
            holder.noteTypeIcon.setImageResource(R.drawable.audio)
        }

        holder.itemView.setOnClickListener { onClick(note) }
    }

    override fun getItemCount(): Int = filteredNotes.size

    fun filter(query: String) {
        filteredNotes = if (query.isEmpty()) {
            originalNotes
        } else {
            originalNotes.filter {
                it.title.contains(query, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }
}
