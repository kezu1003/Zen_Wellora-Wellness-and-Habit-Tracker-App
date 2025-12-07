package com.example.zen_wellora

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MoodEntryAdapter(
    private var moodEntries: List<Fragment_mood_journal.MoodEntry>,
    private val onEditClick: (Fragment_mood_journal.MoodEntry) -> Unit,
    private val onDeleteClick: (Fragment_mood_journal.MoodEntry) -> Unit
) : RecyclerView.Adapter<MoodEntryAdapter.MoodEntryViewHolder>() {

    inner class MoodEntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMoodEmoji: TextView = itemView.findViewById(R.id.tvMoodEmoji)
        val tvDateTime: TextView = itemView.findViewById(R.id.tvDateTime)
        val tvNotes: TextView = itemView.findViewById(R.id.tvNotes)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        // Optional: Add click listener for the entire item
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val moodEntry = moodEntries[position]
                    // You can add item click functionality here if needed
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodEntryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_mood_entry, parent, false)
        return MoodEntryViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodEntryViewHolder, position: Int) {
        val moodEntry = moodEntries[position]

        // Format date and time
        val dateFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
        val dateTime = dateFormat.format(Date(moodEntry.date))

        // Set data to views
        holder.tvMoodEmoji.text = moodEntry.emoji
        holder.tvDateTime.text = dateTime
        holder.tvNotes.text = moodEntry.description

        // Set click listeners for edit and delete buttons
        holder.btnEdit.setOnClickListener {
            onEditClick(moodEntry)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(moodEntry)
        }

        // Optional: Change background based on mood type for visual feedback
        setMoodBackground(holder, moodEntry.mood)
    }

    override fun getItemCount(): Int = moodEntries.size

    /**
     * Update the adapter with new list of mood entries
     */
    fun updateEntries(newEntries: List<Fragment_mood_journal.MoodEntry>) {
        moodEntries = newEntries
        notifyDataSetChanged()
    }

    /**
     * Add a single mood entry to the list
     */
    fun addEntry(moodEntry: Fragment_mood_journal.MoodEntry) {
        val newList = moodEntries.toMutableList().apply {
            add(0, moodEntry) // Add to beginning for newest first
        }
        updateEntries(newList)
    }

    /**
     * Remove a mood entry from the list
     */
    fun removeEntry(moodEntry: Fragment_mood_journal.MoodEntry) {
        val newList = moodEntries.toMutableList().apply {
            remove(moodEntry)
        }
        updateEntries(newList)
    }

    /**
     * Update a specific mood entry
     */
    fun updateEntry(updatedEntry: Fragment_mood_journal.MoodEntry) {
        val newList = moodEntries.toMutableList().map { entry ->
            if (entry.id == updatedEntry.id) updatedEntry else entry
        }
        updateEntries(newList)
    }

    /**
     * Get mood entry at specific position
     */
    fun getEntryAt(position: Int): Fragment_mood_journal.MoodEntry? {
        return if (position in 0 until moodEntries.size) {
            moodEntries[position]
        } else {
            null
        }
    }

    /**
     * Clear all entries
     */
    fun clearEntries() {
        updateEntries(emptyList())
    }

    /**
     * Optional: Set different background colors based on mood type
     */
    private fun setMoodBackground(holder: MoodEntryViewHolder, mood: String) {
        val context = holder.itemView.context
        val backgroundColor = when (mood.toLowerCase(Locale.ROOT)) {
            "happy", "excited", "loved", "loving", "cool" -> {
                // Positive moods - light green
                context.getColor(android.R.color.holo_green_light)
            }
            "sad", "angry", "anxious", "sick" -> {
                // Negative moods - light red
                context.getColor(android.R.color.holo_red_light)
            }
            "tired", "neutral" -> {
                // Neutral moods - light blue
                context.getColor(android.R.color.holo_blue_light)
            }
            else -> {
                // Default - light gray
                context.getColor(android.R.color.darker_gray)
            }
        }

        // Apply background color to the card view or main layout
        holder.itemView.findViewById<View>(R.id.main)?.setBackgroundColor(backgroundColor)
    }

    /**
     * Filter entries by mood type
     */
    fun filterByMood(moodType: String) {
        if (moodType.isEmpty()) {
            // Show all entries if filter is empty
            notifyDataSetChanged()
        } else {
            val filteredList = moodEntries.filter {
                it.mood.equals(moodType, ignoreCase = true)
            }
            updateEntries(filteredList)
        }
    }

    /**
     * Sort entries by date (newest first)
     */
    fun sortByDateNewestFirst() {
        val sortedList = moodEntries.sortedByDescending { it.date }
        updateEntries(sortedList)
    }

    /**
     * Sort entries by date (oldest first)
     */
    fun sortByDateOldestFirst() {
        val sortedList = moodEntries.sortedBy { it.date }
        updateEntries(sortedList)
    }

    /**
     * Get statistics about mood entries
     */
    fun getMoodStatistics(): Map<String, Int> {
        return moodEntries.groupingBy { it.mood }.eachCount()
    }

    /**
     * Get the most common mood
     */
    fun getMostCommonMood(): String? {
        return getMoodStatistics().maxByOrNull { it.value }?.key
    }

    /**
     * Check if adapter has any entries
     */
    fun hasEntries(): Boolean {
        return moodEntries.isNotEmpty()
    }

    /**
     * Get total number of entries (same as itemCount but more descriptive)
     */
    fun getTotalEntries(): Int {
        return moodEntries.size
    }
}