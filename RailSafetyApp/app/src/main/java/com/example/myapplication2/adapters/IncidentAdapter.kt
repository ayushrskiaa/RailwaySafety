package com.example.myapplication2.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication2.R
import com.example.myapplication2.models.Incident

class IncidentAdapter(private val incidents: List<Incident>) :
    RecyclerView.Adapter<IncidentAdapter.IncidentViewHolder>() {

    class IncidentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.incident_title)
        val description: TextView = view.findViewById(R.id.incident_description)
        val severity: TextView = view.findViewById(R.id.incident_severity)
        val location: TextView = view.findViewById(R.id.incident_location)
        val timestamp: TextView = view.findViewById(R.id.incident_timestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncidentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_incident, parent, false)
        return IncidentViewHolder(view)
    }

    override fun onBindViewHolder(holder: IncidentViewHolder, position: Int) {
        val incident = incidents[position]
        holder.title.text = incident.title
        holder.description.text = incident.description
        holder.severity.text = incident.severity
        holder.location.text = incident.location
        holder.timestamp.text = incident.timestamp
        
        // Set severity color
        when (incident.severity.uppercase()) {
            "CRITICAL" -> holder.severity.setBackgroundColor(Color.parseColor("#F44336"))
            "HIGH" -> holder.severity.setBackgroundColor(Color.parseColor("#FF6F00"))
            "MEDIUM" -> holder.severity.setBackgroundColor(Color.parseColor("#FFC107"))
            "LOW" -> holder.severity.setBackgroundColor(Color.parseColor("#4CAF50"))
            else -> holder.severity.setBackgroundColor(Color.parseColor("#9E9E9E"))
        }
    }

    override fun getItemCount() = incidents.size
}
