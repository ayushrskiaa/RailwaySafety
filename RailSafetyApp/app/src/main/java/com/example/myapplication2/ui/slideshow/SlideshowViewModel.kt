package com.example.myapplication2.ui.slideshow

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication2.models.Alert
import com.example.myapplication2.repository.FirebaseRepository

class SlideshowViewModel : ViewModel() {

    private val firebaseRepository = FirebaseRepository.getInstance()
    
    private val _activeAlerts = MutableLiveData<List<Alert>>()
    val activeAlerts: LiveData<List<Alert>> = _activeAlerts
    
    private var currentFilter = "Active"
    private var allAlertsList = listOf<Alert>()

    init {
        // Observe Firebase alerts
        observeFirebaseAlerts()
    }
    
    private fun observeFirebaseAlerts() {
        // Use MediatorLiveData to observe Firebase data
        val mediator = MediatorLiveData<List<Alert>>()
        mediator.addSource(firebaseRepository.allAlerts) { alerts ->
            allAlertsList = alerts
            applyFilter(alerts, currentFilter)
        }
    }

    fun filterAlerts(filter: String) {
        currentFilter = filter
        applyFilter(allAlertsList, filter)
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
