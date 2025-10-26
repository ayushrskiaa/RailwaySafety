package com.example.myapplication2.ui.slideshow

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication2.models.Alert

class SlideshowViewModel : ViewModel() {

    // Sample alerts data (Firebase temporarily disabled)
    private val _allAlerts = MutableLiveData<List<Alert>>().apply {
        value = listOf(
            Alert(
                id = "1",
                title = "Track Inspection Due",
                message = "Scheduled inspection for Track 5",
                timestamp = "5 mins ago",
                priority = "MEDIUM",
                type = "Maintenance",
                isRead = false
            ),
            Alert(
                id = "2",
                title = "Speed Limit Change",
                message = "Speed limit reduced to 40 km/h on Section B",
                timestamp = "15 mins ago",
                priority = "HIGH",
                type = "Safety",
                isRead = false
            ),
            Alert(
                id = "3",
                title = "Weather Warning",
                message = "Heavy rain expected in next 2 hours",
                timestamp = "30 mins ago",
                priority = "MEDIUM",
                type = "Weather",
                isRead = false
            ),
            Alert(
                id = "4",
                title = "Maintenance Complete",
                message = "Track 3 maintenance completed successfully",
                timestamp = "2 hours ago",
                priority = "LOW",
                type = "Maintenance",
                isRead = true
            ),
            Alert(
                id = "5",
                title = "System Update",
                message = "Safety monitoring system updated to v2.1",
                timestamp = "1 day ago",
                priority = "LOW",
                type = "System",
                isRead = true
            )
        )
    }
    
    private val _activeAlerts = MutableLiveData<List<Alert>>()
    val activeAlerts: LiveData<List<Alert>> = _activeAlerts
    
    private var currentFilter = "Active"

    init {
        // Apply initial filter
        applyFilter(_allAlerts.value ?: emptyList(), currentFilter)
    }

    fun filterAlerts(filter: String) {
        currentFilter = filter
        applyFilter(_allAlerts.value ?: emptyList(), filter)
    }
    
    private fun applyFilter(alerts: List<Alert>, filter: String) {
        val filteredList = when (filter) {
            "Active" -> alerts.filter { !it.isRead }
            "History" -> alerts.filter { it.isRead }
            "All" -> alerts
            else -> alerts
        }
        _activeAlerts.value = filteredList
    }
}
