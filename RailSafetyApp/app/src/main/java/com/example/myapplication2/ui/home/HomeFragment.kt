package com.example.myapplication2.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication2.databinding.FragmentHomeBinding
import com.example.myapplication2.ui.adapters.EventAdapter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var eventAdapter: EventAdapter
    
    // Track max ETA for progress calculation
    private var maxETA: Float = 30.0f // Default max ETA in seconds (adjusts dynamically)
    
    companion object {
        private const val TAG = "HomeFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView called")
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        
        setupRecyclerView()
        observeViewModel()
        
        Log.d(TAG, "Fragment setup complete")
        return root
    }
    
    private fun setupRecyclerView() {
        eventAdapter = EventAdapter()
        binding.eventLogRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = eventAdapter
        }
        Log.d(TAG, "RecyclerView setup complete")
    }
    
    private fun observeViewModel() {
        Log.d(TAG, "Setting up ViewModel observers")
        
        // Observe train status
        homeViewModel.trainStatus.observe(viewLifecycleOwner) { status ->
            Log.d(TAG, "Train Status updated: $status")
            binding.trainStatusValue.text = status
        }
        
        // Observe gate status
        homeViewModel.gateStatus.observe(viewLifecycleOwner) { status ->
            Log.d(TAG, "Gate Status updated: $status")
            binding.gateStatusValue.text = status
        }
        
        // Observe current speed
        homeViewModel.currentSpeed.observe(viewLifecycleOwner) { speed ->
            Log.d(TAG, "Speed updated: $speed")
            binding.currentSpeedValue.text = speed
        }
        
        // Observe ETA to gate
        homeViewModel.etaToGate.observe(viewLifecycleOwner) { eta ->
            Log.d(TAG, "ETA updated: $eta")
            binding.etaToGateValue.text = eta
            updateETAProgress(eta)
        }
        
        // Observe last update time
        homeViewModel.lastUpdate.observe(viewLifecycleOwner) { time ->
            Log.d(TAG, "Last Update time: $time")
            binding.trainStatusTime.text = "Last updated: $time"
            binding.gateStatusTime.text = "Last updated: $time"
        }
        
        // Observe event log
        homeViewModel.eventLog.observe(viewLifecycleOwner) { events ->
            Log.d(TAG, "Event log updated: ${events.size} events")
            eventAdapter.submitList(events)
        }
        
        Log.d(TAG, "All observers set up successfully")
    }
    
    private fun updateETAProgress(etaString: String) {
        try {
            val eta = etaString.toFloatOrNull() ?: 0f
            
            // Update max ETA if current ETA is higher
            if (eta > maxETA) {
                maxETA = eta
            }
            
            // Calculate progress percentage (inverse - as ETA decreases, progress increases)
            val progress = if (maxETA > 0) {
                // Progress goes from 0 to 100 as ETA goes from maxETA to 0
                ((maxETA - eta) / maxETA * 100).toInt().coerceIn(0, 100)
            } else {
                0
            }
            
            // Update progress bar
            binding.etaProgressBar.progress = progress
            
            // Change color based on proximity
            val progressColor = when {
                eta <= 5.0f -> android.graphics.Color.parseColor("#F44336") // Red - Very close
                eta <= 10.0f -> android.graphics.Color.parseColor("#FF9933") // Orange - Close
                eta <= 20.0f -> android.graphics.Color.parseColor("#FFC107") // Yellow - Approaching
                else -> android.graphics.Color.parseColor("#FF9933") // Orange default
            }
            binding.etaProgressBar.progressTintList = android.content.res.ColorStateList.valueOf(progressColor)
            
            // Reset maxETA if train has crossed (ETA is 0 and was tracking)
            if (eta == 0f && maxETA > 0) {
                maxETA = 30.0f // Reset to default
            }
            
            Log.d(TAG, "ETA Progress: $progress% (ETA: $eta, Max: $maxETA)")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating ETA progress: ${e.message}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}