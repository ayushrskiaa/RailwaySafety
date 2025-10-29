package com.example.myapplication2.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import com.example.myapplication2.R
import com.example.myapplication2.repository.RailwayCrossingRepository

class NotificationService : Service() {

    private lateinit var railwayRepository: RailwayCrossingRepository
    private var lastTrainStatus = "Loading Update..."
    private var hasNotifiedForCurrentApproach = false
    
    private val trainStatusObserver = Observer<String> { status ->
        Log.d("NotificationService", "Train status changed to: $status")
        
        // Check if train is approaching (not crossed, not reset, not loading)
        val isApproaching = status.contains("Approaching", ignoreCase = true) || 
                           status.contains("Moving", ignoreCase = true)
        
        // Send notification only when train starts approaching (state change)
        if (isApproaching && !hasNotifiedForCurrentApproach && 
            lastTrainStatus != status) {
            sendTrainApproachingNotification()
            hasNotifiedForCurrentApproach = true
        }
        
        // Reset the flag when train crosses or resets
        if (status.contains("Crossed", ignoreCase = true) || 
            status.contains("No Train", ignoreCase = true) ||
            status.contains("system_reset", ignoreCase = true)) {
            hasNotifiedForCurrentApproach = false
        }
        
        lastTrainStatus = status
    }

    override fun onCreate() {
        super.onCreate()
        railwayRepository = RailwayCrossingRepository.getInstance()
        railwayRepository.trainStatus.observeForever(trainStatusObserver)
        Log.d("NotificationService", "Service Created and Observer attached to RailwayCrossingRepository.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, "TRAIN_APPROACH_CHANNEL")
            .setContentTitle("Railway Safety Service")
            .setContentText("Monitoring for train proximity alerts.")
            .setSmallIcon(R.drawable.ic_train)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(1, notification)
        }
        Log.d("NotificationService", "Service Started in foreground.")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        railwayRepository.trainStatus.removeObserver(trainStatusObserver)
        Log.d("NotificationService", "Service Destroyed and Observer removed.")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun sendTrainApproachingNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationBuilder = NotificationCompat.Builder(this, "TRAIN_APPROACH_CHANNEL")
            .setSmallIcon(R.drawable.ic_train) // Make sure you have this drawable
            .setContentTitle("Train Approaching")
            .setContentText("A train is approaching the gate. Please be cautious.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(1, notificationBuilder.build())
        Log.d("NotificationService", "Notification sent.")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Train Approach Notifications"
            val descriptionText = "Notifications for when a train is approaching"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("TRAIN_APPROACH_CHANNEL", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d("NotificationService", "Notification channel created.")
        }
    }
}
