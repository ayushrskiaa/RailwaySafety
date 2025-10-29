package com.example.myapplication2

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import android.util.Log
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
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.myapplication2.services.NotificationService
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    
    // Maintainer email address (for display only, not for SMTP)
    private val MAINTAINER_EMAIL = "ayushrskiaa@gmail.com"
    
    // EmailJS Configuration - Replace with your actual credentials from https://www.emailjs.com/
    private val EMAILJS_SERVICE_ID = "service_5crjwbf"  // e.g., "service_abc123"
    private val EMAILJS_TEMPLATE_ID = "template_vpy1z29"  // e.g., "template_xyz456"
    private val EMAILJS_PUBLIC_KEY = "RsTjfLtTKf8EevdBG"  // e.g., "user_ABC123xyz"

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

        // Create container for inputs
        val container = android.widget.LinearLayout(this)
        container.orientation = android.widget.LinearLayout.VERTICAL
        container.setPadding(50, 20, 50, 20)

        // User email input
        val emailInput = EditText(this)
        emailInput.hint = "Your email (optional)"
        emailInput.inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        emailInput.setPadding(20, 30, 20, 30)
        container.addView(emailInput)

        // User phone input
        val phoneInput = EditText(this)
        phoneInput.hint = "Your phone (optional)"
        phoneInput.inputType = android.text.InputType.TYPE_CLASS_PHONE
        phoneInput.setPadding(20, 30, 20, 30)
        container.addView(phoneInput)

        // Complaint details input
        val detailsInput = EditText(this)
        detailsInput.hint = "Describe the issue..."
        detailsInput.setPadding(20, 30, 20, 30)
        detailsInput.minLines = 3
        container.addView(detailsInput)

        // Set up the dialog
        builder.setSingleChoiceItems(complaintTypes, 0) { _, which ->
            selectedComplaintType = complaintTypes[which]
        }

        builder.setView(container)

        builder.setPositiveButton("Submit") { dialog, _ ->
            val complaintDetails = detailsInput.text.toString().trim()
            val userEmail = emailInput.text.toString().trim()
            val userPhone = phoneInput.text.toString().trim()

            if (complaintDetails.isNotEmpty()) {
                submitComplaint(selectedComplaintType, complaintDetails, userEmail, userPhone)
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

    private fun submitComplaint(type: String, details: String, userEmail: String, userPhone: String) {
        val database = FirebaseDatabase.getInstance("https://iot-implementation-e7fcd-default-rtdb.firebaseio.com")
        val complaintsRef = database.getReference("complaints")

        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val complaint = mapOf(
            "type" to type,
            "details" to details,
            "timestamp" to timestamp,
            "status" to "pending",
            "userEmail" to userEmail.ifEmpty { "Anonymous" },
            "userPhone" to userPhone.ifEmpty { "Not provided" },
            "deviceModel" to "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}",
            "appVersion" to "1.0"
        )

        // Generate unique ID first
        val newComplaintRef = complaintsRef.push()
        val complaintId = newComplaintRef.key ?: "unknown"
        
        newComplaintRef.setValue(complaint)
            .addOnSuccessListener {
                Toast.makeText(this, "✅ Complaint submitted successfully", Toast.LENGTH_LONG).show()
                
                // Send FCM notification to maintainer
                sendNotificationToMaintainer(complaintId, type, details, userEmail)
                
                Snackbar.make(binding.root, "Complaint #${complaintId.takeLast(6)} recorded. Maintainer will be notified.", Snackbar.LENGTH_LONG)
                    .setAction("OK", null)
                    .show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "❌ Failed to submit complaint: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
    
    private fun sendNotificationToMaintainer(complaintId: String, type: String, details: String, userEmail: String) {
        val database = FirebaseDatabase.getInstance("https://iot-implementation-e7fcd-default-rtdb.firebaseio.com")
        val notificationsRef = database.getReference("maintainer_notifications")
        
        val notification = mapOf(
            "title" to "🚨 New Complaint: $type",
            "body" to "From: ${userEmail.ifEmpty { "Anonymous" }}\n$details",
            "complaintId" to complaintId,
            "type" to type,
            "priority" to "high",
            "timestamp" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
            "read" to false
        )
        
        notificationsRef.push().setValue(notification)
            .addOnSuccessListener {
                Log.d("MainActivity", "Notification sent to maintainer for complaint: $complaintId")
                
                // Send actual email via EmailJS
                sendEmailViaEmailJS(complaintId, type, details, userEmail)
            }
            .addOnFailureListener { e ->
                Log.e("MainActivity", "Failed to send notification: ${e.message}")
            }
    }
    
    private fun sendEmailViaEmailJS(complaintId: String, type: String, details: String, userEmail: String) {
        // Check if EmailJS credentials are configured
        if (EMAILJS_SERVICE_ID == "YOUR_SERVICE_ID" || 
            EMAILJS_TEMPLATE_ID == "YOUR_TEMPLATE_ID" || 
            EMAILJS_PUBLIC_KEY == "YOUR_PUBLIC_KEY") {
            Log.w("MainActivity", "⚠️ EmailJS not configured. Please add your credentials from https://www.emailjs.com/")
            Toast.makeText(this, "⚠️ Email credentials not configured", Toast.LENGTH_SHORT).show()
            return
        }
        
        Log.d("EmailJS", "🔄 Preparing to send email for complaint: $complaintId")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                
                // Prepare email template parameters - matching your EmailJS template
                val templateParams = JSONObject().apply {
                    put("complaint_type", type)
                    put("complaint_details", details)
                    put("user_email", userEmail.ifEmpty { "Anonymous" })
                    put("complaint_id", complaintId)
                    put("timestamp", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
                }
                
                // Build EmailJS request
                val requestBody = JSONObject().apply {
                    put("service_id", EMAILJS_SERVICE_ID)
                    put("template_id", EMAILJS_TEMPLATE_ID)
                    put("user_id", EMAILJS_PUBLIC_KEY)
                    put("template_params", templateParams)
                }
                
                Log.d("EmailJS", "📤 Sending request to EmailJS API...")
                Log.d("EmailJS", "Service: $EMAILJS_SERVICE_ID, Template: $EMAILJS_TEMPLATE_ID")
                
                val request = Request.Builder()
                    .url("https://api.emailjs.com/api/v1.0/email/send")
                    .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()
                
                // Execute request
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: "No response body"
                
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Log.d("EmailJS", "✅ Email sent successfully to $MAINTAINER_EMAIL")
                        Log.d("EmailJS", "Response: $responseBody")
                        Toast.makeText(this@MainActivity, "📧 Email sent to maintainer", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("EmailJS", "❌ Email failed - Code: ${response.code}")
                        Log.e("EmailJS", "❌ Message: ${response.message}")
                        Log.e("EmailJS", "❌ Response body: $responseBody")
                        Toast.makeText(this@MainActivity, "⚠️ Email failed (${response.code})", Toast.LENGTH_LONG).show()
                    }
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("EmailJS", "❌ Email exception: ${e.javaClass.simpleName}")
                    Log.e("EmailJS", "❌ Error message: ${e.message}")
                    e.printStackTrace()
                    Toast.makeText(this@MainActivity, "⚠️ Email error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}