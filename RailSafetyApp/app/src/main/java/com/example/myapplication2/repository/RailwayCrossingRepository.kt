package com.example.myapplication2.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myapplication2.models.Event
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class RailwayCrossingRepository private constructor() {
    
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance("https://iot-implementation-e7fcd-default-rtdb.firebaseio.com")
    private val logsRef: DatabaseReference = database.getReference("logs")
    private val storedLogsRef: DatabaseReference = database.getReference("storedLogs")
    
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
        "system_reset" to "No Train Nearby"
    )
    
    private val gateMap = mapOf(
        "opening" to "Opening",
        "closed" to "Closed",
        "closing" to "Closing"
    )
    
    init {
        setupListeners()
    }
    
    private fun setupListeners() {
        Log.d(TAG, "Setting up Firebase listeners for Railway Crossing")
        
        // Listen to logs for real-time updates
        logsRef.limitToLast(1).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Log.d(TAG, "No log data available")
                    setDefaultValues()
                    return
                }
                
                snapshot.children.lastOrNull()?.let { latestLog ->
                    processLatestLog(latestLog)
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to read logs: ${error.message}")
            }
        })
        
        // Listen to stored logs for event log display
        storedLogsRef.orderByKey().limitToLast(20)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val events = mutableListOf<Event>()
                    
                    snapshot.children.forEach { child ->
                        try {
                            val time = child.child("time").getValue(String::class.java) ?: ""
                            val event = child.child("event").getValue(String::class.java) ?: ""
                            val gateStatus = child.child("gateStatus").getValue(String::class.java) ?: ""
                            
                            val eventText = eventMap[event] ?: event
                            val description = if (gateStatus.isNotEmpty()) {
                                "$eventText: $gateStatus"
                            } else {
                                eventText
                            }
                            
                            if (description.isNotEmpty()) {
                                events.add(Event(
                                    timestamp = time,
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
            val event = logSnapshot.child("event").getValue(String::class.java) ?: "system_reset"
            val gateStatus = logSnapshot.child("gate_status").getValue(String::class.java) ?: "opening"
            val speed = logSnapshot.child("speed").getValue(String::class.java) ?: "0.00"
            val eta = logSnapshot.child("eta").getValue(String::class.java) ?: "0.00"
            val timestamp = logSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L
            
            // Update train status
            val trainStatusText = eventMap[event] ?: "Loading Update..."
            _trainStatus.postValue(trainStatusText)
            
            // Update gate status
            val gateStatusText = if (event == "system_reset") {
                "Open"
            } else {
                gateMap[gateStatus] ?: gateStatus
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
            
            Log.d(TAG, "Updated - Train: $trainStatusText, Gate: $gateStatusText, Speed: $speedValue, ETA: $etaValue")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing log: ${e.message}")
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