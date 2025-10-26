package com.example.myapplication2.ui.gallery

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication2.databinding.FragmentGalleryBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val galleryViewModel =
            ViewModelProvider(this).get(GalleryViewModel::class.java)

        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupLineChart(galleryViewModel)
        setupBarChart(galleryViewModel)
        setupPieChart(galleryViewModel)

        return root
    }

    private fun setupLineChart(viewModel: GalleryViewModel) {
        binding.lineChart.apply {
            data = viewModel.getLineChartData()
            description.isEnabled = false
            setDrawGridBackground(false)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun"))
                granularity = 1f
                textColor = Color.parseColor("#424242")
            }

            axisLeft.textColor = Color.parseColor("#424242")
            axisRight.isEnabled = false
            legend.textColor = Color.parseColor("#424242")

            animateX(1000)
            invalidate()
        }
    }

    private fun setupBarChart(viewModel: GalleryViewModel) {
        binding.barChart.apply {
            data = viewModel.getBarChartData()
            description.isEnabled = false
            setDrawGridBackground(false)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(arrayOf("Track", "Signal", "Train", "Platform"))
                granularity = 1f
                textColor = Color.parseColor("#424242")
            }

            axisLeft.textColor = Color.parseColor("#424242")
            axisRight.isEnabled = false
            legend.textColor = Color.parseColor("#424242")

            animateY(1000)
            invalidate()
        }
    }

    private fun setupPieChart(viewModel: GalleryViewModel) {
        binding.pieChart.apply {
            data = viewModel.getPieChartData()
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            holeRadius = 40f
            transparentCircleRadius = 45f

            legend.textColor = Color.parseColor("#424242")
            setEntryLabelColor(Color.parseColor("#424242"))

            animateY(1000)
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}