package com.example.myapplication2

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication2.databinding.ActivityMainBinding
import com.example.myapplication2.utils.TestDataPopulator
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.myapplication2.services.NotificationService

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    
    // Maintainer email address
    private val MAINTAINER_EMAIL = "ayushrskiaa@gmail.com"
    
    // Gmail SMTP configuration (create an app-specific password for this)
    private val SENDER_EMAIL = "ayushkumar823932@gmail.com"  // Change this
    private val SENDER_PASSWORD = "hokrnsfxtiucscxz"      // Use Gmail App Password (remove spaces)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notifications permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Notifications permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        askNotificationPermission()

        // Start the NotificationService
        val serviceIntent = Intent(this, NotificationService::class.java)
        startService(serviceIntent)
        
        // TEST: Write sample data to Firebase to verify connection
        testFirebaseConnection()
        
        // DIAGNOSTIC: Read existing data from RailwayGate/current
        testReadRailwayGateData()

        binding.appBarMain.fab.setOnClickListener { view ->
            showComplaintDialog()
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level 33+ (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: Display an educational UI explaining why the user should enable notifications
                // for your app.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_populate_test_data -> {
                populateTestData()
                true
            }
            R.id.action_settings -> {
                // Handle settings action
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun populateTestData() {
        AlertDialog.Builder(this)
            .setTitle("Populate Test Data")
            .setMessage("This will add sample gate events and complaints to the database for testing. Continue?")
            .setPositiveButton("Yes") { _, _ ->
                TestDataPopulator.populateAllSampleData()
                Toast.makeText(this, "✅ Test data populated! Check Alerts tab.", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun testFirebaseConnection() {
        android.util.Log.d("FirebaseTest", "🧪 Testing Firebase connection...")
        
        val database = FirebaseDatabase.getInstance()
        val testRef = database.getReference("logs").child("test_${System.currentTimeMillis()}")
        
        val testData = mapOf(
            "event" to "train_detected",
            "gate_status" to "closing",
            "speed" to 45.5,
            "eta" to 25.3,
            "timestamp" to System.currentTimeMillis()
        )
        
        testRef.setValue(testData)
            .addOnSuccessListener {
                android.util.Log.d("FirebaseTest", "✅ Test data written successfully!")
                Toast.makeText(this, "✅ Firebase Connected! Data written.", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                android.util.Log.e("FirebaseTest", "❌ Firebase write failed: ${e.message}")
                Toast.makeText(this, "❌ Firebase Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun testReadRailwayGateData() {
        android.util.Log.d("DiagnosticTest", "🔍 Testing read from RailwayGate/current...")
        
        val database = FirebaseDatabase.getInstance()
        database.getReference("RailwayGate/current")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    android.util.Log.d("DiagnosticTest", "✅ Data EXISTS in RailwayGate/current!")
                    snapshot.children.forEach { 
                        android.util.Log.d("DiagnosticTest", "  ${it.key} = ${it.value}")
                    }
                    Toast.makeText(this, "✅ Railway data found: ${snapshot.child("event").value}", Toast.LENGTH_LONG).show()
                } else {
                    android.util.Log.e("DiagnosticTest", "❌ NO DATA in RailwayGate/current!")
                    Toast.makeText(this, "❌ No data in RailwayGate/current", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("DiagnosticTest", "❌ Failed to read: ${e.message}")
                Toast.makeText(this, "❌ Read failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showComplaintDialog() {
        val complaintTypes = arrayOf(
            "Gate Malfunction",
            "Sensor Issue",
            "Delayed Response",
            "Safety Concern",
            "Other Issue"
        )

        var selectedComplaintType = complaintTypes[0]

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Report Railway Crossing Issue")

        // Create input field for complaint details
        val input = EditText(this)
        input.hint = "Describe the issue..."
        input.setPadding(50, 40, 50, 40)

        // Set up the dialog
        builder.setSingleChoiceItems(complaintTypes, 0) { _, which ->
            selectedComplaintType = complaintTypes[which]
        }

        builder.setView(input)

        builder.setPositiveButton("Submit") { dialog, _ ->
            val complaintDetails = input.text.toString().trim()

            if (complaintDetails.isNotEmpty()) {
                submitComplaint(selectedComplaintType, complaintDetails)
            } else {
                Toast.makeText(this, "Please provide complaint details", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun submitComplaint(type: String, details: String) {
        val database = FirebaseDatabase.getInstance("https://iot-implementation-e7fcd-default-rtdb.firebaseio.com")
        val complaintsRef = database.getReference("complaints")

        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val complaint = mapOf(
            "type" to type,
            "details" to details,
            "timestamp" to timestamp,
            "status" to "pending"
        )

        complaintsRef.push().setValue(complaint)
            .addOnSuccessListener {
                Toast.makeText(this, "✅ Complaint submitted successfully", Toast.LENGTH_LONG).show()
                
                // Send email to maintainer
                sendEmailToMaintainer(type, details, timestamp)
                
                Snackbar.make(binding.root, "Complaint recorded. Opening email app...", Snackbar.LENGTH_LONG)
                    .setAction("OK", null)
                    .show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "❌ Failed to submit complaint: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
    
    private fun sendEmailToMaintainer(type: String, details: String, timestamp: String) {
        // Send email directly using SMTP
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val emailSent = sendEmailViaSMTP(type, details, timestamp)
                withContext(Dispatchers.Main) {
                    if (emailSent) {
                        Toast.makeText(this@MainActivity, "📧 Email sent successfully to maintainer!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@MainActivity, "⚠️ Email failed, but complaint saved in database", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "⚠️ Email error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun sendEmailViaSMTP(type: String, details: String, timestamp: String): Boolean {
        return try {
            // SMTP Configuration for Gmail
            val props = Properties().apply {
                put("mail.smtp.host", "smtp.gmail.com")
                put("mail.smtp.port", "587")
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
            }
            
            // Create session with authentication
            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD)
                }
            })
            
            // Create email message
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(SENDER_EMAIL))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(MAINTAINER_EMAIL))
                subject = "🚂 Railway Crossing Complaint - $type"
                setText("""
Railway Crossing Safety Alert
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📋 COMPLAINT DETAILS

Type: $type
Time: $timestamp

Description:
$details

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
⚠️ This complaint was submitted via Railway Safety Android App
Please take immediate action if required.

Maintainer: $MAINTAINER_EMAIL
                """.trimIndent())
            }
            
            // Send email
            Transport.send(message)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}