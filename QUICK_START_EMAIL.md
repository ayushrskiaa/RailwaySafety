# 🚀 Quick Start - Email Complaint Feature

## ⚡ 5-Minute Setup

### Step 1: Verify Dependencies (build.gradle)
```gradle
dependencies {
    implementation 'com.sun.mail:android-mail:1.6.7'
    implementation 'com.sun.mail:android-activation:1.6.7'
}
```

### Step 2: Check Internet Permission (AndroidManifest.xml)
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

### Step 3: Configure Gmail App Password

1. **Enable 2-Step Verification:**
   - Go to: https://myaccount.google.com/security
   - Turn on 2-Step Verification

2. **Generate App Password:**
   - Go to: https://myaccount.google.com/apppasswords
   - App: Select "Mail"
   - Device: Select your device type
   - Click "Generate"
   - **IMPORTANT:** Copy the 16-character password WITHOUT spaces

3. **Update MainActivity.kt:**
   ```kotlin
   private val SENDER_EMAIL = "your-email@gmail.com"
   private val SENDER_PASSWORD = "abcdefghijklmnop"  // 16 chars, no spaces
   private val MAINTAINER_EMAIL = "maintainer@gmail.com"
   ```

### Step 4: Build & Run
```bash
# In Android Studio
Build → Rebuild Project
Run → Run 'app'
```

### Step 5: Test the Feature

1. **Open Logcat** (View → Tool Windows → Logcat)
2. **Filter by:** `EmailSMTP`
3. **Tap FAB** button in the app
4. **Fill the form:**
   - Email: test@example.com (optional)
   - Phone: 1234567890 (optional)
   - Details: "Test complaint" (required)
   - Type: Select any option
5. **Tap Submit**
6. **Watch Logcat** for:
   ```
   D/EmailSMTP: 📤 Starting SMTP email send...
   D/EmailSMTP: ✅ Email sent successfully!
   ```

## 🎯 What You Get

✅ **User-friendly dialog** with 3 input fields  
✅ **Firebase storage** of all complaints  
✅ **SMTP email** sent to maintainer  
✅ **Comprehensive logging** for debugging  
✅ **Error handling** with helpful messages  
✅ **Async processing** (non-blocking UI)  

## 🐛 Quick Troubleshooting

| Problem | Quick Fix |
|---------|-----------|
| Authentication failed | Re-generate App Password, remove ALL spaces |
| Network error | Check internet connection |
| Timeout | Switch WiFi/Mobile Data, or increase timeout in code |
| Email not received | Check spam folder, verify maintainer email |

## 📧 Email Preview

```
Subject: 🚂 Railway Crossing Complaint - Gate Malfunction

Railway Crossing Safety Alert
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📋 COMPLAINT DETAILS

Type: Gate Malfunction
Time: 2025-10-29 14:30:45

👤 User Contact:
📧 Email: user@example.com
📱 Phone: +1234567890

📝 Description:
Gate not closing properly when train approaches.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
⚠️ This complaint was submitted via Railway Safety Android App
Please take immediate action if required.

Maintainer: ayushrskiaa@gmail.com
```

## 📚 Full Documentation

For detailed information, see:
- `EMAIL_COMPLAINT_FIXED.md` - Complete feature documentation
- `SMTP_EMAIL_FIX_GUIDE.md` - In-depth troubleshooting guide

## ✨ That's It!

Your email complaint feature is ready to use! 🎉
