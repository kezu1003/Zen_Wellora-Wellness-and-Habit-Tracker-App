package com.example.zen_wellora

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class RecentActivityAdapter(
    private val activities: List<RecentActivity>,
    private val onActivityClick: (RecentActivity) -> Unit
) : RecyclerView.Adapter<RecentActivityAdapter.ActivityViewHolder>() {

    class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.card_recent_activity)
        val iconView: ImageView = itemView.findViewById(R.id.iv_activity_icon)
        val titleView: TextView = itemView.findViewById(R.id.tv_activity_title)
        val descriptionView: TextView = itemView.findViewById(R.id.tv_activity_description)
        val timeView: TextView = itemView.findViewById(R.id.tv_activity_time)
        val pointsView: TextView = itemView.findViewById(R.id.tv_activity_points)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_activity, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val activity = activities[position]
        
        // Set activity data
        holder.iconView.setImageResource(activity.iconRes)
        holder.titleView.text = activity.title
        holder.descriptionView.text = activity.description
        holder.timeView.text = activity.getTimeAgo()
        
        // Set points (only show if > 0)
        if (activity.points > 0) {
            holder.pointsView.text = "+${activity.points} pts"
            holder.pointsView.visibility = View.VISIBLE
        } else {
            holder.pointsView.visibility = View.GONE
        }
        
        // Set click listener
        holder.cardView.setOnClickListener {
            onActivityClick(activity)
        }
    }

    override fun getItemCount(): Int = activities.size
}










