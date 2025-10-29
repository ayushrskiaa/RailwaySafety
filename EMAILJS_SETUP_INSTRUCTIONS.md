# 📧 EmailJS Setup Instructions

## Step 1: Create EmailJS Account (5 minutes)

1. Go to **https://www.emailjs.com/**
2. Click "Sign Up" and create a free account
3. Verify your email address

---

## Step 2: Add Email Service (2 minutes)

1. In EmailJS dashboard, go to **"Email Services"**
2. Click **"Add New Service"**
3. Select **"Gmail"** (recommended)
4. Connect your Gmail account (ayushrskiaa@gmail.com)
5. Copy the **Service ID** (looks like: `service_abc123`)

---

## Step 3: Create Email Template (3 minutes)

1. Go to **"Email Templates"**
2. Click **"Create New Template"**
3. Set template name: **"Railway Complaint Notification"**
4. Configure the template:

### Template Subject:
```
🚨 New Railway Safety Complaint: {{complaint_type}}
```

### Template Body:
```
Railway Safety Alert
====================

A new complaint has been filed in the Railway Safety App.

Complaint Details:
------------------
Type: {{complaint_type}}
Complaint ID: {{complaint_id}}
Timestamp: {{timestamp}}

User Information:
-----------------
Email: {{user_email}}

Issue Description:
------------------
{{complaint_details}}

------------------
This is an automated notification from the Railway Safety App.
Please investigate and respond promptly.
```

5. Click **"Save"**
6. Copy the **Template ID** (looks like: `template_xyz456`)

---

## Step 4: Get Public Key

1. Go to **"Account"** → **"General"**
2. Find **"Public Key"** (looks like: `user_ABC123xyz`)
3. Copy this key

---

## Step 5: Update MainActivity.kt

Open `MainActivity.kt` and replace these lines (around line 37-39):

```kotlin
// REPLACE THESE WITH YOUR ACTUAL CREDENTIALS:
private val EMAILJS_SERVICE_ID = "YOUR_SERVICE_ID"  // Paste your Service ID here
private val EMAILJS_TEMPLATE_ID = "YOUR_TEMPLATE_ID"  // Paste your Template ID here
private val EMAILJS_PUBLIC_KEY = "YOUR_PUBLIC_KEY"  // Paste your Public Key here
```

**Example (with your actual values):**
```kotlin
private val EMAILJS_SERVICE_ID = "service_abc123"
private val EMAILJS_TEMPLATE_ID = "template_xyz456"
private val EMAILJS_PUBLIC_KEY = "user_ABC123xyz"
```

---

## Step 6: Sync Gradle & Build

1. In Android Studio, click **"Sync Now"** to download OkHttp library
2. Wait for sync to complete (about 30 seconds)
3. Build your app: **Build → Rebuild Project**

---

## Step 7: Test Email Functionality

1. Run the app on your device/emulator
2. Tap the **FAB (+ button)** to file a complaint
3. Fill in:
   - Select complaint type
   - Enter your email (optional)
   - Enter phone number (optional)
   - Describe the issue
4. Click **"Submit"**

**What should happen:**
- ✅ Toast: "Complaint submitted successfully"
- ✅ Toast: "📧 Email sent to maintainer"
- ✅ Check `ayushrskiaa@gmail.com` inbox for the email

---

## Troubleshooting

### ⚠️ "Email credentials not configured" warning
- You forgot to replace `YOUR_SERVICE_ID`, `YOUR_TEMPLATE_ID`, or `YOUR_PUBLIC_KEY`
- Double-check all three credentials are pasted correctly

### ❌ "Email failed to send"
- Check internet connection
- Verify EmailJS service is active (not suspended)
- Check EmailJS dashboard for error logs

### 🔒 Gmail blocking sign-in
- Go to Gmail settings → Security → Allow less secure apps
- Or use an App Password (recommended)

---

## Email Limits (Free Tier)

- **200 emails per month** (free)
- Perfect for academic projects
- Upgrade to paid plan if you need more

---

## Template Variables Reference

These variables are automatically replaced in your email template:

| Variable | Example Value |
|----------|---------------|
| `{{to_email}}` | ayushrskiaa@gmail.com |
| `{{complaint_type}}` | Gate Malfunction |
| `{{complaint_details}}` | The gate is not closing properly... |
| `{{user_email}}` | student@example.com |
| `{{complaint_id}}` | -NxAbC123xyz |
| `{{timestamp}}` | 2025-10-29 14:35:22 |

---

## Success! 🎉

After setup, every complaint filed through the app will:
1. ✅ Save to Firebase Realtime Database
2. ✅ Create a notification in `/maintainer_notifications/`
3. ✅ **Send an actual email to ayushrskiaa@gmail.com**

The maintainer will receive professional email notifications for every complaint!
