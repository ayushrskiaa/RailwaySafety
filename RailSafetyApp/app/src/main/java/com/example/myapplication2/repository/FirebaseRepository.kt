package com.example.myapplication2.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myapplication2.models.Alert
import com.example.myapplication2.models.Incident
import com.google.firebase.database.*
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat

class FirebaseRepository {
    
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val metricsRef: DatabaseReference = database.getReference("safety_metrics")
    private val incidentsRef: DatabaseReference = database.getReference("incidents")
    private val alertsRef: DatabaseReference = database.getReference("alerts")
    private val gateRef: DatabaseReference = database.getReference("gate_status")
    private val complaintsRef: DatabaseReference = database.getReference("complaints")
    private val gateEventsRef: DatabaseReference = database.getReference("gate_events")
    
    companion object {
        private const val TAG = "FirebaseRepository"
        
        @Volatile
        private var instance: FirebaseRepository? = null
        
        fun getInstance(): FirebaseRepository {
            return instance ?: synchronized(this) {
                instance ?: FirebaseRepository().also { instance = it }
            }
        }
    }
    
    // Real-time metrics data
    private val _trainStatus = MutableLiveData<String>()
    val trainStatus: LiveData<String> = _trainStatus
    
    private val _trackCondition = MutableLiveData<String>()
    val trackCondition: LiveData<String> = _trackCondition
    
    private val _activeAlerts = MutableLiveData<String>()
    val activeAlerts: LiveData<String> = _activeAlerts
    
    private val _safetyScore = MutableLiveData<String>()
    val safetyScore: LiveData<String> = _safetyScore
    
    private val _incidents = MutableLiveData<List<Incident>>()
    val incidents: LiveData<List<Incident>> = _incidents
    
    private val _alerts = MutableLiveData<List<Alert>>()
    val alerts: LiveData<List<Alert>> = _alerts

    private val _gateStatus = MutableLiveData<Boolean>()
    val gateStatus: LiveData<Boolean> = _gateStatus
    
    // Combined alerts including gate events and complaints
    private val _allAlerts = MutableLiveData<List<Alert>>()
    val allAlerts: LiveData<List<Alert>> = _allAlerts
    
    private val alertsList = mutableListOf<Alert>()
    
    init {
        setupRealtimeListeners()
    }
    
    private fun setupRealtimeListeners() {
        // Listen for train status changes
        metricsRef.child("train_status").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.child("value").getValue(String::class.java) ?: "0"
                _trainStatus.postValue(value)
                Log.d(TAG, "Train Status updated: $value")
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Train Status listener cancelled: ${error.message}")
            }
        })
        
        // Listen for track condition changes
        metricsRef.child("track_condition").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.child("value").getValue(String::class.java) ?: "0%"
                _trackCondition.postValue(value)
                Log.d(TAG, "Track Condition updated: $value")
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Track Condition listener cancelled: ${error.message}")
            }
        })
        
        // Listen for active alerts count
        metricsRef.child("active_alerts_count").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.child("value").getValue(String::class.java) ?: "0"
                _activeAlerts.postValue(value)
                Log.d(TAG, "Active Alerts updated: $value")
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Active Alerts listener cancelled: ${error.message}")
            }
        })
        
        // Listen for safety score changes
        metricsRef.child("safety_score").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.child("value").getValue(String::class.java) ?: "0"
                _safetyScore.postValue(value)
                Log.d(TAG, "Safety Score updated: $value")
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Safety Score listener cancelled: ${error.message}")
            }
        })
        
        // Listen for new incidents
        incidentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val incidentList = mutableListOf<Incident>()
                for (incidentSnapshot in snapshot.children) {
                    val incident = incidentSnapshot.child("value").getValue(Incident::class.java)
                    incident?.let { incidentList.add(it) }
                }
                _incidents.postValue(incidentList)
                Log.d(TAG, "Incidents updated: ${incidentList.size} incidents loaded.")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Incidents listener cancelled: ${error.message}")
            }
        })

        // Listen for new alerts
        alertsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val alertList = mutableListOf<Alert>()
                for (alertSnapshot in snapshot.children) {
                    val alert = alertSnapshot.child("value").getValue(Alert::class.java)
                    alert?.let { alertList.add(it) }
                }
                _alerts.postValue(alertList)
                Log.d(TAG, "Alerts updated: ${alertList.size} alerts loaded.")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Alerts listener cancelled: ${error.message}")
            }
        })

        // Listen for gate status changes
        gateRef.child("isTrainApproaching").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isApproaching = snapshot.getValue(Boolean::class.java) ?: false
                _gateStatus.postValue(isApproaching)
                Log.d(TAG, "Gate status updated: isTrainApproaching = $isApproaching")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Gate status listener cancelled: ${error.message}")
            }
        })
        
        // Listen for gate events (open/close)
        gateEventsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val gateAlerts = mutableListOf<Alert>()
                snapshot.children.forEach { eventSnapshot ->
                    val eventType = eventSnapshot.child("event").getValue(String::class.java) ?: ""
                    val timestamp = eventSnapshot.child("timestamp").getValue(String::class.java) ?: ""
                    val status = eventSnapshot.child("status").getValue(String::class.java) ?: "active"
                    
                    if (eventType.isNotEmpty()) {
                        val alert = Alert(
                            id = eventSnapshot.key ?: "",
                            title = if (eventType == "opened") "🚧 Gate Opened" else "🔒 Gate Closed",
                            message = if (eventType == "opened") 
                                "Railway crossing gate has been opened for traffic" 
                            else 
                                "Railway crossing gate has been closed - Train approaching",
                            timestamp = formatTimestamp(timestamp),
                            priority = if (eventType == "closed") "HIGH" else "MEDIUM",
                            type = "Gate Event",
                            isRead = status == "resolved"
                        )
                        gateAlerts.add(alert)
                    }
                }
                updateCombinedAlerts("gate", gateAlerts)
                Log.d(TAG, "Gate events loaded: ${gateAlerts.size} events")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Gate events listener cancelled: ${error.message}")
            }
        })
        
        // Listen for complaints
        complaintsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val complaintAlerts = mutableListOf<Alert>()
                snapshot.children.forEach { complaintSnapshot ->
                    val type = complaintSnapshot.child("type").getValue(String::class.java) ?: ""
                    val details = complaintSnapshot.child("details").getValue(String::class.java) ?: ""
                    val timestamp = complaintSnapshot.child("timestamp").getValue(String::class.java) ?: ""
                    val status = complaintSnapshot.child("status").getValue(String::class.java) ?: "pending"
                    
                    if (type.isNotEmpty()) {
                        val alert = Alert(
                            id = complaintSnapshot.key ?: "",
                            title = "📝 Complaint: $type",
                            message = details,
                            timestamp = formatTimestamp(timestamp),
                            priority = when (status) {
                                "pending" -> "MEDIUM"
                                "in_progress" -> "HIGH"
                                else -> "LOW"
                            },
                            type = "Complaint",
                            isRead = status == "resolved"
                        )
                        complaintAlerts.add(alert)
                    }
                }
                updateCombinedAlerts("complaints", complaintAlerts)
                Log.d(TAG, "Complaints loaded: ${complaintAlerts.size} complaints")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Complaints listener cancelled: ${error.message}")
            }
        })
    }
    
    // Maps to store different alert types
    private val alertsMap = mutableMapOf<String, List<Alert>>()
    
    private fun updateCombinedAlerts(source: String, alerts: List<Alert>) {
        alertsMap[source] = alerts
        
        // Combine all alerts from different sources
        val combined = mutableListOf<Alert>()
        alertsMap.values.forEach { combined.addAll(it) }
        
        // Sort by timestamp (most recent first)
        val sorted = combined.sortedByDescending { it.timestamp }
        
        _allAlerts.postValue(sorted)
    }
    
    private fun formatTimestamp(timestamp: String): String {
        if (timestamp.isEmpty()) return "Just now"
        
        return try {
            // Try to parse the timestamp and format it
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            val date = sdf.parse(timestamp)
            
            if (date != null) {
                val now = java.util.Date()
                val diff = now.time - date.time
                
                when {
                    diff < 60000 -> "Just now"
                    diff < 3600000 -> "${diff / 60000} mins ago"
                    diff < 86400000 -> "${diff / 3600000} hours ago"
                    diff < 604800000 -> "${diff / 86400000} days ago"
                    else -> SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(date)
                }
            } else {
                timestamp
            }
        } catch (e: Exception) {
            timestamp
        }
    }
    
    // Function to initialize sample data in Firebase
    suspend fun initializeSampleData() {
        try {
            // Set sample metrics
            metricsRef.child("train_status").setValue("45").await()
            metricsRef.child("track_condition").setValue("98%").await()
            metricsRef.child("active_alerts_count").setValue("3").await()
            metricsRef.child("safety_score").setValue("95").await()
            
            // Add sample incidents
            val sampleIncidents = listOf(
                mapOf(
                    "title" to "Track Maintenance Required",
                    "description" to "Routine inspection detected minor track wear on Section A",
                    "timestamp" to "2 hours ago",
                    "severity" to "LOW",
                    "location" to "Track Section A, Zone 3",
                    "status" to "In Progress"
                ),
                mapOf(
                    "title" to "Signal System Check",
                    "description" to "Automated signal testing reported minor delay in response time",
                    "timestamp" to "5 hours ago",
                    "severity" to "MEDIUM",
                    "location" to "Station Junction B",
                    "status" to "Under Review"
                ),
                mapOf(
                    "title" to "Platform Safety Inspection",
                    "description" to "Scheduled platform inspection completed successfully",
                    "timestamp" to "1 day ago",
                    "severity" to "LOW",
                    "location" to "Platform 4, Central Station",
                    "status" to "Resolved"
                )
            )
            
            sampleIncidents.forEach { incident ->
                incidentsRef.push().setValue(incident).await()
            }
            
            // Add sample alerts
            val sampleAlerts = listOf(
                mapOf(
                    "title" to "Track Maintenance Scheduled",
                    "message" to "Routine track maintenance scheduled for Section A tomorrow at 3:00 AM",
                    "timestamp" to "5 mins ago",
                    "priority" to "MEDIUM",
                    "type" to "Maintenance",
                    "isRead" to false
                ),
                mapOf(
                    "title" to "Signal System Update",
                    "message" to "Signal system software update completed successfully",
                    "timestamp" to "1 hour ago",
                    "priority" to "LOW",
                    "type" to "System",
                    "isRead" to false
                ),
                mapOf(
                    "title" to "Weather Advisory",
                    "message" to "Heavy rain expected in the region. Monitor track conditions closely",
                    "timestamp" to "2 hours ago",
                    "priority" to "HIGH",
                    "type" to "Weather",
                    "isRead" to false
                ),
                mapOf(
                    "title" to "Speed Restriction",
                    "message" to "Temporary speed restriction on Zone 5 due to ongoing inspection",
                    "timestamp" to "3 hours ago",
                    "priority" to "CRITICAL",
                    "type" to "Operations",
                    "isRead" to true
                ),
                mapOf(
                    "title" to "Platform Sensor Check",
                    "message" to "All platform sensors are functioning within normal parameters",
                    "timestamp" to "5 hours ago",
                    "priority" to "LOW",
                    "type" to "System",
                    "isRead" to true
                )
            )
            
            sampleAlerts.forEach { alert ->
                alertsRef.push().setValue(alert).await()
            }
            
            Log.d(TAG, "Sample data initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing sample data: ${e.message}")
        }
    }
    
    // Function to update a metric
    fun updateMetric(metricName: String, value: String) {
        metricsRef.child(metricName).setValue(value)
            .addOnSuccessListener {
                Log.d(TAG, "Metric $metricName updated to $value")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to update metric $metricName: ${e.message}")
            }
    }
    
    // Function to add a new incident
    fun addIncident(incident: Incident) {
        val incidentMap = mapOf(
            "title" to incident.title,
            "description" to incident.description,
            "timestamp" to incident.timestamp,
            "severity" to incident.severity,
            "location" to incident.location,
            "status" to incident.status
        )
        
        incidentsRef.push().setValue(incidentMap)
            .addOnSuccessListener {
                Log.d(TAG, "Incident added successfully")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to add incident: ${e.message}")
            }
    }
    
    // Function to add a new alert
    fun addAlert(alert: Alert) {
        val alertMap = mapOf(
            "title" to alert.title,
            "message" to alert.message,
            "timestamp" to alert.timestamp,
            "priority" to alert.priority,
            "type" to alert.type,
            "isRead" to alert.isRead
        )
        
        alertsRef.push().setValue(alertMap)
            .addOnSuccessListener {
                Log.d(TAG, "Alert added successfully")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to add alert: ${e.message}")
            }
    }
    
    // Function to log gate events (open/close)
    fun logGateEvent(eventType: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        
        val gateEvent = mapOf(
            "event" to eventType,
            "timestamp" to timestamp,
            "status" to "active"
        )
        
        gateEventsRef.push().setValue(gateEvent)
            .addOnSuccessListener {
                Log.d(TAG, "Gate event logged: $eventType at $timestamp")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to log gate event: ${e.message}")
            }
    }
}
