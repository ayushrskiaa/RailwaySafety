package com.example.myapplication2.models

data class Alert(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val priority: String,
    val type: String,
    val isRead: Boolean = false
)
