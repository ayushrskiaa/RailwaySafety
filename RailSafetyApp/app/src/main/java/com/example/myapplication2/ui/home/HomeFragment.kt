package com.example.myapplication2.ui.home

import android.os.Bundle
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        
        setupRecyclerView()
        observeViewModel()
        
        return root
    }
    
    private fun setupRecyclerView() {
        eventAdapter = EventAdapter()
        binding.eventLogRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = eventAdapter
        }
    }
    
    private fun observeViewModel() {
        // Observe train status
        homeViewModel.trainStatus.observe(viewLifecycleOwner) { status ->
            binding.trainStatusValue.text = status
        }
        
        // Observe gate status
        homeViewModel.gateStatus.observe(viewLifecycleOwner) { status ->
            binding.gateStatusValue.text = status
        }
        
        // Observe current speed
        homeViewModel.currentSpeed.observe(viewLifecycleOwner) { speed ->
            binding.currentSpeedValue.text = speed
        }
        
        // Observe ETA to gate
        homeViewModel.etaToGate.observe(viewLifecycleOwner) { eta ->
            binding.etaToGateValue.text = eta
        }
        
        // Observe last update time
        homeViewModel.lastUpdate.observe(viewLifecycleOwner) { time ->
            binding.trainStatusTime.text = "Last updated: $time"
            binding.gateStatusTime.text = "Last updated: $time"
        }
        
        // Observe event log
        homeViewModel.eventLog.observe(viewLifecycleOwner) { events ->
            eventAdapter.submitList(events)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}