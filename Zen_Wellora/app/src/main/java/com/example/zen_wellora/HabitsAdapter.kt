// HabitsAdapter.kt
package com.example.zen_wellora

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HabitsAdapter(
    private var habits: List<Habit>,
    private val onHabitAction: (Habit, String) -> Unit
) : RecyclerView.Adapter<HabitsAdapter.HabitViewHolder>() {

    private var isEditMode = false

    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val habitName: TextView = itemView.findViewById(R.id.tvHabitName)
        val habitDescription: TextView = itemView.findViewById(R.id.tvHabitDescription)
        val completeButton: Button = itemView.findViewById(R.id.btnComplete)
        val editButton: ImageButton = itemView.findViewById(R.id.btnEdit)
        val deleteButton: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]

        holder.habitName.text = habit.name
        holder.habitDescription.text = habit.description

        // Set completion status
        holder.completeButton.text = if (habit.completed) "Completed" else "Mark Complete"
        holder.completeButton.isEnabled = !habit.completed && !isEditMode

        // Show/hide edit buttons based on mode
        val editVisibility = if (isEditMode) View.VISIBLE else View.GONE
        holder.editButton.visibility = editVisibility
        holder.deleteButton.visibility = editVisibility

        holder.completeButton.setOnClickListener {
            if (!isEditMode) {
                onHabitAction(habit, "complete")
            }
        }

        holder.editButton.setOnClickListener {
            onHabitAction(habit, "edit")
        }

        holder.deleteButton.setOnClickListener {
            onHabitAction(habit, "delete")
        }
    }

    override fun getItemCount(): Int = habits.size

    fun updateHabits(newHabits: List<Habit>) {
        this.habits = newHabits
        notifyDataSetChanged()
    }

    fun toggleEditMode(): Boolean {
        isEditMode = !isEditMode
        notifyDataSetChanged()
        return isEditMode
    }
}