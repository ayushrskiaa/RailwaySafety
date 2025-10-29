package com.example.myapplication2.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myapplication2.models.Event
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class RailwayCrossingRepository private constructor() {
    
    // Use the Firebase URL from google-services.json
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val logsRef: DatabaseReference = database.getReference("RailwayGate/current")
    private val storedLogsRef: DatabaseReference = database.getReference("RailwayGate/history")
    
    // LiveData for UI
    private val _trainStatus = MutableLiveData<String>()
    val trainStatus: LiveData<String> = _trainStatus
    
    private val _gateStatus = MutableLiveData<String>()
    val gateStatus: LiveData<String> = _gateStatus
    
    private val _currentSpeed = MutableLiveData<String>()
    val currentSpeed: LiveData<String> = _currentSpeed
    
    private val _etaToGate = MutableLiveData<String>()
    val etaToGate: LiveData<String> = _etaToGate
    
    private val _lastUpdate = MutableLiveData<String>()
    val lastUpdate: LiveData<String> = _lastUpdate
    
    private val _eventLog = MutableLiveData<List<Event>>()
    val eventLog: LiveData<List<Event>> = _eventLog
    
    private val eventMap = mapOf(
        "train_detected" to "Train Approaching",
        "speed_calculated" to "Train Moving towards Gate",
        "train_crossed" to "Train Crossed",
        "system_reset" to "No Train Nearby",
        "gate_opened" to "Gate Opened",
        "gate_closed" to "Gate Closed",
        "gate_opening" to "Gate Opening",
        "gate_closing" to "Gate Closing"
    )
    
    private val gateMap = mapOf(
        "opening" to "Opening",
        "closed" to "Closed",
        "closing" to "Closing"
    )
    
    init {
        // Set initial values
        setDefaultValues()
        // Setup Firebase listeners
        setupListeners()
    }
    
    private fun setupListeners() {
        Log.d(TAG, "Setting up Firebase listeners for Railway Crossing")
        Log.d(TAG, "Firebase Database URL: ${database.reference.toString()}")
        Log.d(TAG, "Listening to path: RailwayGate/current/")
        Log.d(TAG, "Full reference: ${logsRef.toString()}")
        
        // Listen to RailwayGate/current for real-time updates
        logsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "Snapshot value: ${snapshot.value}")
                // Diagnostic Toast (uncomment if you have context):
                // Toast.makeText(context, "Data: ${snapshot.value}", Toast.LENGTH_LONG).show()
                Log.d(TAG, "onDataChange called - data exist: ${snapshot.exists()}")
                
                if (!snapshot.exists()) {
                    Log.w(TAG, "No data available in Firebase RailwayGate/current")
                    setDefaultValues()
                    return
                }
                
                Log.d(TAG, "Current railway data found")
                snapshot.children.forEach { field ->
                    Log.d(TAG, "  ${field.key} = ${field.value}")
                }
                
                processLatestLog(snapshot)
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error: ${error.message}")
                // Diagnostic Toast (uncomment if you have context):
                // Toast.makeText(context, "Firebase error: ${error.message}", Toast.LENGTH_LONG).show()
                Log.e(TAG, "Failed to read logs: ${error.message}")
                Log.e(TAG, "Error code: ${error.code}, Details: ${error.details}")
                setDefaultValues()
            }
        })
        
        // Listen to RailwayGate/history for event log display
        storedLogsRef.orderByKey().limitToLast(20)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val events = mutableListOf<Event>()
                    
                    snapshot.children.forEach { child ->
                        try {
                            val datetime = child.child("datetime").getValue(String::class.java) ?: ""
                            val event = child.child("event").getValue(String::class.java) ?: ""
                            val gateStatus = child.child("gate_status").getValue(String::class.java) ?: ""
                            
                            // Use the event from eventMap, or format the raw event if not found
                            val eventText = eventMap[event] ?: event.replace("_", " ").split(" ")
                                .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                            
                            val description = if (gateStatus.isNotEmpty() && !event.startsWith("gate_")) {
                                // For non-gate events, show gate status
                                "$eventText - Gate: ${gateMap[gateStatus] ?: gateStatus}"
                            } else {
                                // For gate events or events without gate status, just show the event
                                eventText
                            }
                            
                            if (description.isNotEmpty() && datetime.isNotEmpty()) {
                                events.add(Event(
                                    timestamp = datetime,
                                    description = description
                                ))
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing event: ${e.message}")
                        }
                    }
                    
                    _eventLog.postValue(events.reversed()) // Show latest first
                    Log.d(TAG, "Event log updated: ${events.size} events")
                }
                
                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Failed to read event log: ${error.message}")
                }
            })
    }
    
    private fun processLatestLog(logSnapshot: DataSnapshot) {
        try {
            Log.d(TAG, "Processing current railway data")
            
            val event = logSnapshot.child("event").getValue(String::class.java) ?: "system_reset"
            val gateStatus = logSnapshot.child("gate_status").getValue(String::class.java) ?: "opening"
            val direction = logSnapshot.child("direction").getValue(String::class.java) ?: ""
            val sensor = logSnapshot.child("sensor").value?.toString() ?: ""
            
            // Speed and ETA - matching ESP32 field names: speed_kmh and eta_sec
            val speedRaw = logSnapshot.child("speed_kmh").value
            val speed = when (speedRaw) {
                is Double -> String.format("%.2f", speedRaw)
                is Long -> speedRaw.toDouble().toString()
                is String -> speedRaw
                else -> "0.00"
            }
            
            val etaRaw = logSnapshot.child("eta_sec").value
            val eta = when (etaRaw) {
                is Double -> String.format("%.2f", etaRaw)
                is Long -> etaRaw.toDouble().toString()
                is String -> etaRaw
                else -> "0.00"
            }
            
            val timestamp = logSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L
            
            Log.d(TAG, "Parsed - Event: $event, Gate: $gateStatus, Direction: $direction, Sensor: $sensor, Speed: $speed, ETA: $eta")
            
            // Update train status
            val trainStatusText = eventMap[event] ?: "Unknown Event"
            _trainStatus.postValue(trainStatusText)
            
            // Update gate status
            val gateStatusText = if (event == "system_reset") {
                "Open"
            } else {
                gateMap[gateStatus] ?: gateStatus.replaceFirstChar { it.uppercase() }
            }
            _gateStatus.postValue(gateStatusText)
            
            // Update speed
            val speedValue = if (event == "train_crossed" || event == "system_reset") {
                "0.00"
            } else {
                speed
            }
            _currentSpeed.postValue(speedValue)
            
            // Update ETA
            val etaValue = if (event == "train_crossed" || event == "system_reset") {
                "0.00"
            } else {
                eta
            }
            _etaToGate.postValue(etaValue)
            
            // Update last update time
            val currentTime = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
                .format(Date())
            _lastUpdate.postValue(currentTime)
            
            Log.d(TAG, "UI Updated - Train: $trainStatusText, Gate: $gateStatusText, Speed: $speedValue, ETA: $etaValue")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing log: ${e.message}", e)
            setDefaultValues()
        }
    }
    
    private fun setDefaultValues() {
        _trainStatus.postValue("Loading Update...")
        _gateStatus.postValue("Loading Update...")
        _currentSpeed.postValue("0.00")
        _etaToGate.postValue("0.00")
        _lastUpdate.postValue("--")
        _eventLog.postValue(emptyList())
    }
    
    companion object {
        private const val TAG = "RailwayCrossingRepo"
        
        @Volatile
        private var instance: RailwayCrossingRepository? = null
        
        fun getInstance(): RailwayCrossingRepository {
            return instance ?: synchronized(this) {
                instance ?: RailwayCrossingRepository().also { instance = it }
            }
        }
    }
}