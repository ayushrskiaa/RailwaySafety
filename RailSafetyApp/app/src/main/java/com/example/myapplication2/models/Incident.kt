package com.example.myapplication2.models

data class Incident(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: String,
    val severity: String,
    val location: String,
    val status: String
)
