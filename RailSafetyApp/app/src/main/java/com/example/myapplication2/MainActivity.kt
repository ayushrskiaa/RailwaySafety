package com.example.myapplication2

import android.os.Bundle
import android.util.Log
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

        // Create a container layout for multiple input fields
        val container = android.widget.LinearLayout(this)
        container.orientation = android.widget.LinearLayout.VERTICAL
        container.setPadding(50, 20, 50, 20)

        // User email input (optional)
        val emailInput = EditText(this)
        emailInput.hint = "Your email (optional)"
        emailInput.inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        emailInput.setPadding(20, 30, 20, 30)
        container.addView(emailInput)

        // User phone input (optional)
        val phoneInput = EditText(this)
        phoneInput.hint = "Your phone (optional)"
        phoneInput.inputType = android.text.InputType.TYPE_CLASS_PHONE
        phoneInput.setPadding(20, 30, 20, 30)
        container.addView(phoneInput)

        // Complaint details input (required)
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

    private fun submitComplaint(type: String, details: String, userEmail: String = "", userPhone: String = "") {
        val database = FirebaseDatabase.getInstance("https://iot-implementation-e7fcd-default-rtdb.firebaseio.com")
        val complaintsRef = database.getReference("complaints")

        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val complaint = mapOf(
            "type" to type,
            "details" to details,
            "timestamp" to timestamp,
            "status" to "pending",
            "userEmail" to userEmail.ifEmpty { "Not provided" },
            "userPhone" to userPhone.ifEmpty { "Not provided" }
        )

        complaintsRef.push().setValue(complaint)
            .addOnSuccessListener {
                Toast.makeText(this, "✅ Complaint submitted successfully", Toast.LENGTH_LONG).show()
                
                // Send email to maintainer
                sendEmailToMaintainer(type, details, timestamp, userEmail, userPhone)
                
                Snackbar.make(binding.root, "Complaint recorded. Sending email notification...", Snackbar.LENGTH_LONG)
                    .setAction("OK", null)
                    .show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "❌ Failed to submit complaint: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
    
    private fun sendEmailToMaintainer(type: String, details: String, timestamp: String, userEmail: String = "", userPhone: String = "") {
        // Send email directly using SMTP
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val emailSent = sendEmailViaSMTP(type, details, timestamp, userEmail, userPhone)
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
    
    private fun sendEmailViaSMTP(type: String, details: String, timestamp: String, userEmail: String = "", userPhone: String = ""): Boolean {
        return try {
            Log.d("EmailSMTP", "📤 Starting SMTP email send...")
            Log.d("EmailSMTP", "Sender: $SENDER_EMAIL → Recipient: $MAINTAINER_EMAIL")
            
            // SMTP Configuration for Gmail
            val props = Properties().apply {
                put("mail.smtp.host", "smtp.gmail.com")
                put("mail.smtp.port", "587")
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.starttls.required", "true")
                
                // Enhanced SSL/TLS settings
                put("mail.smtp.ssl.protocols", "TLSv1.2")
                put("mail.smtp.ssl.trust", "smtp.gmail.com")
                
                // Timeout settings (10 seconds each)
                put("mail.smtp.connectiontimeout", "10000")
                put("mail.smtp.timeout", "10000")
                put("mail.smtp.writetimeout", "10000")
            }
            
            Log.d("EmailSMTP", "✅ Creating mail session...")
            
            // Create session with authentication
            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD)
                }
            })
            
            // Enable debug mode for detailed SMTP conversation logging
            session.debug = true
            
            Log.d("EmailSMTP", "✅ Creating message...")
            
            // Build user contact information section
            val contactInfo = buildString {
                if (userEmail.isNotEmpty()) append("\n📧 Email: $userEmail")
                if (userPhone.isNotEmpty()) append("\n📱 Phone: $userPhone")
                if (userEmail.isEmpty() && userPhone.isEmpty()) append("\nAnonymous User")
            }
            
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

👤 User Contact:$contactInfo

📝 Description:
$details

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
⚠️ This complaint was submitted via Railway Safety Android App
Please take immediate action if required.

Maintainer: $MAINTAINER_EMAIL
                """.trimIndent())
            }
            
            Log.d("EmailSMTP", "✅ Connecting to SMTP server...")
            
            // Send email
            Transport.send(message)
            
            Log.d("EmailSMTP", "✅ Email sent successfully!")
            true
            
        } catch (e: javax.mail.AuthenticationFailedException) {
            Log.e("EmailSMTP", "❌ Authentication failed: ${e.message}")
            Log.e("EmailSMTP", "💡 Check: 1) App Password is correct 2) 2-Step Verification is enabled")
            e.printStackTrace()
            false
        } catch (e: javax.mail.MessagingException) {
            Log.e("EmailSMTP", "❌ Messaging error: ${e.message}")
            Log.e("EmailSMTP", "💡 Check: SMTP settings and network connection")
            e.printStackTrace()
            false
        } catch (e: java.net.UnknownHostException) {
            Log.e("EmailSMTP", "❌ Network error: Cannot reach smtp.gmail.com")
            Log.e("EmailSMTP", "💡 Check: Internet connection")
            e.printStackTrace()
            false
        } catch (e: java.net.SocketTimeoutException) {
            Log.e("EmailSMTP", "❌ Timeout: Connection timed out")
            Log.e("EmailSMTP", "💡 Check: Network speed and firewall settings")
            e.printStackTrace()
            false
        } catch (e: Exception) {
            Log.e("EmailSMTP", "❌ Unexpected error: ${e.javaClass.simpleName} - ${e.message}")
            e.printStackTrace()
            false
        }
    }
}