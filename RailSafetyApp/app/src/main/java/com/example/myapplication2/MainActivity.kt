package com.example.myapplication2

import android.os.Bundle
import android.view.Menu
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

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    
    // Maintainer email address
    private val MAINTAINER_EMAIL = "ayushrskiaa@gmail.com"
    
    // Gmail SMTP configuration (create an app-specific password for this)
    private val SENDER_EMAIL = "ayushkumar823932@gmail.com"  // Change this
    private val SENDER_PASSWORD = "hokrnsfxtiucscxz"      // Use Gmail App Password (remove spaces)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

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
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
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