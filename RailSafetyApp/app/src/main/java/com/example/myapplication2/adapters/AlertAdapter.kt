package com.example.myapplication2.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication2.R
import com.example.myapplication2.models.Alert

class AlertAdapter(private val alerts: List<Alert>) :
    RecyclerView.Adapter<AlertAdapter.AlertViewHolder>() {

    class AlertViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val priorityIndicator: View = view.findViewById(R.id.priority_indicator)
        val title: TextView = view.findViewById(R.id.alert_title)
        val message: TextView = view.findViewById(R.id.alert_message)
        val priority: TextView = view.findViewById(R.id.alert_priority)
        val type: TextView = view.findViewById(R.id.alert_type)
        val timestamp: TextView = view.findViewById(R.id.alert_timestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alert, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val alert = alerts[position]
        holder.title.text = alert.title
        holder.message.text = alert.message
        holder.priority.text = alert.priority
        holder.type.text = alert.type
        holder.timestamp.text = alert.timestamp
        
        // Set priority color
        val color = when (alert.priority.uppercase()) {
            "CRITICAL" -> Color.parseColor("#F44336")
            "HIGH" -> Color.parseColor("#FF6F00")
            "MEDIUM" -> Color.parseColor("#FFC107")
            "LOW" -> Color.parseColor("#4CAF50")
            else -> Color.parseColor("#2196F3")
        }
        holder.priorityIndicator.setBackgroundColor(color)
        holder.priority.setBackgroundColor(color)
    }

    override fun getItemCount() = alerts.size
}
