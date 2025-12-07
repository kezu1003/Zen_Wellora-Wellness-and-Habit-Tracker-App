package com.example.zen_wellora

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class HabitIconAdapter(
    private val icons: List<HabitIcon>,
    private val onIconSelected: (HabitIcon) -> Unit
) : RecyclerView.Adapter<HabitIconAdapter.IconViewHolder>() {

    private var selectedPosition = -1

    class IconViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.card_habit_icon)
        val imageView: ImageView = itemView.findViewById(R.id.iv_habit_icon)
        val selectedIndicator: View = itemView.findViewById(R.id.view_selected_indicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit_icon, parent, false)
        return IconViewHolder(view)
    }

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        val icon = icons[position]
        
        // Set icon
        holder.imageView.setImageResource(icon.iconRes)
        
        // Set selection state
        val isSelected = position == selectedPosition
        updateSelectionState(holder, isSelected)
        
        // Set click listener
        holder.cardView.setOnClickListener {
            selectedPosition = position
            notifyDataSetChanged() // Refresh all items to update selection states
            onIconSelected(icon)
        }
    }

    private fun updateSelectionState(holder: IconViewHolder, isSelected: Boolean) {
        if (isSelected) {
            holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.primary)
            )
            holder.imageView.setColorFilter(
                ContextCompat.getColor(holder.itemView.context, R.color.white)
            )
            holder.selectedIndicator.visibility = View.VISIBLE
            holder.cardView.elevation = 8f
        } else {
            holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.primary_light)
            )
            holder.imageView.setColorFilter(
                ContextCompat.getColor(holder.itemView.context, R.color.primary)
            )
            holder.selectedIndicator.visibility = View.GONE
            holder.cardView.elevation = 2f
        }
    }

    override fun getItemCount(): Int = icons.size

    fun getSelectedIcon(): HabitIcon? {
        return if (selectedPosition >= 0 && selectedPosition < icons.size) {
            icons[selectedPosition]
        } else null
    }

    fun setSelectedIcon(iconName: String) {
        val position = icons.indexOfFirst { it.name == iconName }
        if (position >= 0) {
            selectedPosition = position
            notifyDataSetChanged()
        }
    }
}

data class HabitIcon(
    val name: String,
    val iconRes: Int,
    val displayName: String
)
