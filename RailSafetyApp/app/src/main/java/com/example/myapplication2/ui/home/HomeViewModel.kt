package com.example.myapplication2.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication2.models.Event
import com.example.myapplication2.repository.RailwayCrossingRepository

class HomeViewModel : ViewModel() {
    
    private val repository = RailwayCrossingRepository.getInstance()
    
    // Expose LiveData from repository
    val trainStatus: LiveData<String> = repository.trainStatus
    val gateStatus: LiveData<String> = repository.gateStatus
    val currentSpeed: LiveData<String> = repository.currentSpeed
    val etaToGate: LiveData<String> = repository.etaToGate
    val lastUpdate: LiveData<String> = repository.lastUpdate
    val eventLog: LiveData<List<Event>> = repository.eventLog
}
