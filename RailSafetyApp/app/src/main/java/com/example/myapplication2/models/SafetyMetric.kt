package com.example.myapplication2.models

data class SafetyMetric(
    val title: String,
    val value: String,
    val status: String,
    val statusColor: Int,
    val iconResId: Int? = null
)
