package com.example.myapplication2.ui.gallery

import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.*

class GalleryViewModel : ViewModel() {

    fun getLineChartData(): LineData {
        val entries = ArrayList<Entry>()
        // Sample monthly incident data
        entries.add(Entry(0f, 12f))
        entries.add(Entry(1f, 8f))
        entries.add(Entry(2f, 15f))
        entries.add(Entry(3f, 6f))
        entries.add(Entry(4f, 10f))
        entries.add(Entry(5f, 4f))

        val dataSet = LineDataSet(entries, "Monthly Incidents")
        dataSet.color = android.graphics.Color.parseColor("#1976D2")
        dataSet.valueTextColor = android.graphics.Color.parseColor("#424242")
        dataSet.lineWidth = 2f
        dataSet.setCircleColor(android.graphics.Color.parseColor("#1976D2"))
        dataSet.circleRadius = 4f
        dataSet.setDrawValues(true)

        return LineData(dataSet)
    }

    fun getBarChartData(): BarData {
        val entries = ArrayList<BarEntry>()
        // Sample safety metrics data
        entries.add(BarEntry(0f, 85f))
        entries.add(BarEntry(1f, 92f))
        entries.add(BarEntry(2f, 88f))
        entries.add(BarEntry(3f, 95f))

        val dataSet = BarDataSet(entries, "Safety Scores")
        dataSet.colors = listOf(
            android.graphics.Color.parseColor("#4CAF50"),
            android.graphics.Color.parseColor("#2196F3"),
            android.graphics.Color.parseColor("#FFC107"),
            android.graphics.Color.parseColor("#FF6F00")
        )
        dataSet.valueTextColor = android.graphics.Color.parseColor("#424242")

        return BarData(dataSet)
    }

    fun getPieChartData(): PieData {
        val entries = ArrayList<PieEntry>()
        // Sample incident distribution data
        entries.add(PieEntry(65f, "Resolved"))
        entries.add(PieEntry(20f, "In Progress"))
        entries.add(PieEntry(10f, "Pending"))
        entries.add(PieEntry(5f, "Critical"))

        val dataSet = PieDataSet(entries, "Incident Status")
        dataSet.colors = listOf(
            android.graphics.Color.parseColor("#4CAF50"),
            android.graphics.Color.parseColor("#2196F3"),
            android.graphics.Color.parseColor("#FFC107"),
            android.graphics.Color.parseColor("#F44336")
        )
        dataSet.valueTextColor = android.graphics.Color.WHITE
        dataSet.valueTextSize = 12f

        return PieData(dataSet)
    }
}
