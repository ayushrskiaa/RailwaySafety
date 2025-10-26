package com.example.myapplication2.ui.slideshow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication2.adapters.AlertAdapter
import com.example.myapplication2.databinding.FragmentSlideshowBinding
import com.google.android.material.tabs.TabLayout

class SlideshowFragment : Fragment() {

    private var _binding: FragmentSlideshowBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var slideshowViewModel: SlideshowViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        slideshowViewModel = ViewModelProvider(this).get(SlideshowViewModel::class.java)
        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()
        setupTabs()
        observeAlerts()

        return root
    }

    private fun setupRecyclerView() {
        binding.alertsRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun setupTabs() {
        binding.alertTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val filter = when (tab?.position) {
                    0 -> "Active"
                    1 -> "History"
                    2 -> "All"
                    else -> "Active"
                }
                slideshowViewModel.filterAlerts(filter)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun observeAlerts() {
        slideshowViewModel.activeAlerts.observe(viewLifecycleOwner) { alerts ->
            if (alerts.isEmpty()) {
                binding.alertsRecyclerView.visibility = View.GONE
                binding.emptyState.visibility = View.VISIBLE
            } else {
                binding.alertsRecyclerView.visibility = View.VISIBLE
                binding.emptyState.visibility = View.GONE
                binding.alertsRecyclerView.adapter = AlertAdapter(alerts)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}