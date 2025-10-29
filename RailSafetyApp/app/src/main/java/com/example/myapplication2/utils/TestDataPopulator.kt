package com.example.myapplication2.utils

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility class to populate sample data for testing the Alerts feature
 */
object TestDataPopulator {
    
    private const val TAG = "TestDataPopulator"
    private val database = FirebaseDatabase.getInstance()
    
    /**
     * Populates sample gate events for testing
     */
    fun populateSampleGateEvents() {
        val gateEventsRef = database.getReference("gate_events")
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val now = Date()
        
        val events = listOf(
            // Recent events (active)
            mapOf(
                "event" to "closed",
                "timestamp" to sdf.format(Date(now.time - 5 * 60 * 1000)), // 5 mins ago
                "status" to "active"
            ),
            mapOf(
                "event" to "opened",
                "timestamp" to sdf.format(Date(now.time - 10 * 60 * 1000)), // 10 mins ago
                "status" to "active"
            ),
            mapOf(
                "event" to "closed",
                "timestamp" to sdf.format(Date(now.time - 30 * 60 * 1000)), // 30 mins ago
                "status" to "active"
            ),
            // Historical events (resolved)
            mapOf(
                "event" to "opened",
                "timestamp" to sdf.format(Date(now.time - 2 * 60 * 60 * 1000)), // 2 hours ago
                "status" to "resolved"
            ),
            mapOf(
                "event" to "closed",
                "timestamp" to sdf.format(Date(now.time - 5 * 60 * 60 * 1000)), // 5 hours ago
                "status" to "resolved"
            ),
            mapOf(
                "event" to "opened",
                "timestamp" to sdf.format(Date(now.time - 24 * 60 * 60 * 1000)), // 1 day ago
                "status" to "resolved"
            )
        )
        
        events.forEach { event ->
            gateEventsRef.push().setValue(event)
                .addOnSuccessListener {
                    Log.d(TAG, "Sample gate event added: ${event["event"]}")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to add gate event: ${e.message}")
                }
        }
    }
    
    /**
     * Populates sample complaints for testing
     */
    fun populateSampleComplaints() {
        val complaintsRef = database.getReference("complaints")
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val now = Date()
        
        val complaints = listOf(
            // Pending complaints
            mapOf(
                "type" to "Gate Malfunction",
                "details" to "Gate did not open immediately after train passed",
                "timestamp" to sdf.format(Date(now.time - 15 * 60 * 1000)), // 15 mins ago
                "status" to "pending"
            ),
            mapOf(
                "type" to "Signal Issue",
                "details" to "Warning lights not working properly during last closure",
                "timestamp" to sdf.format(Date(now.time - 45 * 60 * 1000)), // 45 mins ago
                "status" to "pending"
            ),
            // In Progress complaints
            mapOf(
                "type" to "Barrier Problem",
                "details" to "Barrier arm moving slower than usual",
                "timestamp" to sdf.format(Date(now.time - 3 * 60 * 60 * 1000)), // 3 hours ago
                "status" to "in_progress"
            ),
            mapOf(
                "type" to "Noise Complaint",
                "details" to "Excessive noise from warning bells at night",
                "timestamp" to sdf.format(Date(now.time - 6 * 60 * 60 * 1000)), // 6 hours ago
                "status" to "in_progress"
            ),
            // Resolved complaints
            mapOf(
                "type" to "Track Obstruction",
                "details" to "Debris on tracks near crossing",
                "timestamp" to sdf.format(Date(now.time - 24 * 60 * 60 * 1000)), // 1 day ago
                "status" to "resolved"
            ),
            mapOf(
                "type" to "Gate Malfunction",
                "details" to "Gate stuck in closed position for extended period",
                "timestamp" to sdf.format(Date(now.time - 48 * 60 * 60 * 1000)), // 2 days ago
                "status" to "resolved"
            ),
            mapOf(
                "type" to "Signal Issue",
                "details" to "Red warning light not functioning",
                "timestamp" to sdf.format(Date(now.time - 72 * 60 * 60 * 1000)), // 3 days ago
                "status" to "resolved"
            )
        )
        
        complaints.forEach { complaint ->
            complaintsRef.push().setValue(complaint)
                .addOnSuccessListener {
                    Log.d(TAG, "Sample complaint added: ${complaint["type"]}")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to add complaint: ${e.message}")
                }
        }
    }
    
    /**
     * Populates all sample data (gate events + complaints)
     */
    fun populateAllSampleData() {
        populateSampleGateEvents()
        populateSampleComplaints()
        Log.d(TAG, "All sample data population initiated")
    }
    
    /**
     * Clears all alerts data (use with caution!)
     */
    fun clearAllAlertsData() {
        database.getReference("gate_events").removeValue()
        database.getReference("complaints").removeValue()
        Log.d(TAG, "All alerts data cleared")
    }
}
