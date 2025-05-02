import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.musketeers_and_me.ai_powered_study_assistant_app.R

class ColorPickerAdapter(
    private val colorList: List<Int>,
    private val onColorSelected: ((Int) -> Unit)? = null
) : RecyclerView.Adapter<ColorPickerAdapter.ColorViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    inner class ColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val colorView: View = itemView.findViewById(R.id.color_view)

        init {
            itemView.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onColorSelected?.invoke(colorList[selectedPosition])
            }
        }

        fun bind(color: Int, isSelected: Boolean) {
            colorView.setBackgroundColor(color)
            colorView.alpha = if (isSelected) 1f else 0.5f
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_color, parent, false)
        return ColorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        holder.bind(colorList[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = colorList.size
}
