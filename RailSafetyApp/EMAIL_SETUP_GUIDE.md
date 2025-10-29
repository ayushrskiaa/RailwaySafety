# 📧 Email Notification Setup Guide

## Current Status

✅ **Complaints are saved to Firebase** (`/complaints/`)
✅ **Notifications are saved to Firebase** (`/maintainer_notifications/`)
❌ **Email is NOT being sent** (no email service configured)

---

## 🎯 Solutions to Send Actual Emails

### **Option 1: Use EmailJS (Easiest - No Backend Required)**

EmailJS allows you to send emails directly from your Android app without exposing credentials.

#### **Setup Steps:**

**1. Create EmailJS Account**
- Go to https://www.emailjs.com/
- Sign up for free account (200 emails/month free)
- Create an email service (Gmail, Outlook, etc.)
- Create an email template

**2. Add EmailJS to your app**

Add to `build.gradle.kts`:
```kotlin
dependencies {
    // ... existing dependencies
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
```

**3. Update MainActivity.kt:**

```kotlin
private fun sendEmailToMaintainer(complaintId: String, type: String, details: String, userEmail: String) {
    // EmailJS configuration
    val serviceId = "YOUR_SERVICE_ID"  // From EmailJS dashboard
    val templateId = "YOUR_TEMPLATE_ID"  // From EmailJS dashboard
    val userId = "YOUR_PUBLIC_KEY"  // From EmailJS dashboard
    
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val client = OkHttpClient()
            
            val json = """
            {
                "service_id": "$serviceId",
                "template_id": "$templateId",
                "user_id": "$userId",
                "template_params": {
                    "complaint_type": "$type",
                    "complaint_details": "$details",
                    "user_email": "${userEmail.ifEmpty { "Anonymous" }}",
                    "complaint_id": "$complaintId",
                    "to_email": "$MAINTAINER_EMAIL"
                }
            }
            """.trimIndent()
            
            val body = RequestBody.create(
                "application/json".toMediaType(),
                json
            )
            
            val request = Request.Builder()
                .url("https://api.emailjs.com/api/v1.0/email/send")
                .post(body)
                .build()
            
            val response = client.newCall(request).execute()
            
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    Log.d("MainActivity", "Email sent successfully!")
                } else {
                    Log.e("MainActivity", "Email failed: ${response.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Email error: ${e.message}")
        }
    }
}
```

**4. Call this function after saving complaint:**

```kotlin
newComplaintRef.setValue(complaint)
    .addOnSuccessListener {
        Toast.makeText(this, "✅ Complaint submitted successfully", Toast.LENGTH_LONG).show()
        
        // Send FCM notification to Firebase
        sendNotificationToMaintainer(complaintId, type, details, userEmail)
        
        // Send actual email to maintainer
        sendEmailToMaintainer(complaintId, type, details, userEmail)
        
        Snackbar.make(binding.root, "Complaint #${complaintId.takeLast(6)} recorded.", Snackbar.LENGTH_LONG)
            .setAction("OK", null)
            .show()
    }
```

---

### **Option 2: Firebase Cloud Functions (Recommended for Production)**

This runs server-side code that sends emails when complaints are added.

#### **Setup Steps:**

**1. Install Firebase CLI**
```bash
npm install -g firebase-tools
firebase login
```

**2. Initialize Cloud Functions**
```bash
cd YourProjectRoot
firebase init functions
```

**3. Install SendGrid (or Nodemailer)**
```bash
cd functions
npm install @sendgrid/mail
```

**4. Create Cloud Function** (`functions/index.js`)

```javascript
const functions = require('firebase-functions');
const admin = require('firebase-admin');
const sgMail = require('@sendgrid/mail');

admin.initializeApp();

// Get SendGrid API key from environment
sgMail.setApiKey(functions.config().sendgrid.key);

exports.sendComplaintEmail = functions.database
    .ref('/complaints/{complaintId}')
    .onCreate(async (snapshot, context) => {
        const complaint = snapshot.val();
        const complaintId = context.params.complaintId;
        
        const msg = {
            to: 'ayushrskiaa@gmail.com',
            from: 'noreply@yourapp.com', // Must be verified in SendGrid
            subject: `🚨 Railway Complaint: ${complaint.type}`,
            text: `
Railway Crossing Safety Alert
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📋 COMPLAINT DETAILS

Type: ${complaint.type}
Time: ${complaint.timestamp}
User Email: ${complaint.userEmail}
User Phone: ${complaint.userPhone}
Device: ${complaint.deviceModel}

Description:
${complaint.details}

Complaint ID: ${complaintId}

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
⚠️ Submitted via Railway Safety App
            `,
            html: `
<div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
    <h2 style="color: #003366;">🚂 Railway Crossing Safety Alert</h2>
    <table style="width: 100%; border-collapse: collapse;">
        <tr style="background: #f5f5f5;">
            <td style="padding: 10px; border: 1px solid #ddd;"><strong>Type</strong></td>
            <td style="padding: 10px; border: 1px solid #ddd;">${complaint.type}</td>
        </tr>
        <tr>
            <td style="padding: 10px; border: 1px solid #ddd;"><strong>Time</strong></td>
            <td style="padding: 10px; border: 1px solid #ddd;">${complaint.timestamp}</td>
        </tr>
        <tr style="background: #f5f5f5;">
            <td style="padding: 10px; border: 1px solid #ddd;"><strong>User Email</strong></td>
            <td style="padding: 10px; border: 1px solid #ddd;">${complaint.userEmail}</td>
        </tr>
        <tr>
            <td style="padding: 10px; border: 1px solid #ddd;"><strong>User Phone</strong></td>
            <td style="padding: 10px; border: 1px solid #ddd;">${complaint.userPhone}</td>
        </tr>
        <tr style="background: #f5f5f5;">
            <td style="padding: 10px; border: 1px solid #ddd;"><strong>Device</strong></td>
            <td style="padding: 10px; border: 1px solid #ddd;">${complaint.deviceModel}</td>
        </tr>
    </table>
    <h3>Description:</h3>
    <p style="background: #f9f9f9; padding: 15px; border-left: 4px solid #FF9933;">${complaint.details}</p>
    <p style="color: #666; font-size: 12px;">Complaint ID: ${complaintId}</p>
</div>
            `
        };
        
        try {
            await sgMail.send(msg);
            console.log('Email sent for complaint:', complaintId);
            
            // Update complaint with email status
            await snapshot.ref.update({
                emailSent: true,
                emailSentAt: admin.database.ServerValue.TIMESTAMP
            });
            
            return null;
        } catch (error) {
            console.error('Email error:', error);
            
            await snapshot.ref.update({
                emailSent: false,
                emailError: error.message
            });
            
            return null;
        }
    });
```

**5. Set SendGrid API Key**
```bash
firebase functions:config:set sendgrid.key="YOUR_SENDGRID_API_KEY"
```

**6. Deploy**
```bash
firebase deploy --only functions
```

**That's it!** Now emails will be sent automatically when complaints are added to Firebase.

---

### **Option 3: Quick Fix - Manual Email Intent**

If you want a simple solution that opens the user's email app:

```kotlin
private fun openEmailApp(complaintId: String, type: String, details: String, userEmail: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "message/rfc822"
        putExtra(Intent.EXTRA_EMAIL, arrayOf(MAINTAINER_EMAIL))
        putExtra(Intent.EXTRA_SUBJECT, "🚨 Railway Complaint: $type")
        putExtra(Intent.EXTRA_TEXT, """
Railway Crossing Safety Alert
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📋 COMPLAINT DETAILS

Type: $type
Time: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}
User Email: ${userEmail.ifEmpty { "Anonymous" }}

Description:
$details

Complaint ID: $complaintId

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
⚠️ Submitted via Railway Safety App
        """.trimIndent())
    }
    
    try {
        startActivity(Intent.createChooser(intent, "Send email via..."))
    } catch (e: Exception) {
        Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show()
    }
}
```

Call after saving complaint:
```kotlin
openEmailApp(complaintId, type, details, userEmail)
```

---

## 🎯 Recommended Solution

**For Your Academic Project:**
- Use **Option 1 (EmailJS)** - Quick setup, no backend needed
- Or **Option 3 (Email Intent)** - Opens user's email app

**For Production:**
- Use **Option 2 (Cloud Functions)** - Professional, automatic, scalable

---

## 📊 Comparison

| Method | Setup Time | Cost | Reliability | User Action |
|--------|-----------|------|-------------|-------------|
| EmailJS | 10 mins | Free (200/mo) | High | None |
| Cloud Functions | 30 mins | Free (2M/mo) | Very High | None |
| Email Intent | 2 mins | Free | Medium | Opens email app |

---

## 🚀 Next Steps

Choose one option and I'll help you implement it! Which would you prefer?

1. **EmailJS** (recommended for quick setup)
2. **Cloud Functions** (recommended for production)
3. **Email Intent** (opens user's email app)

Let me know and I'll add the code to your MainActivity!
