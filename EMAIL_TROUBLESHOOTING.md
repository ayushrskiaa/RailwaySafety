# 🔧 Email Sending Troubleshooting Guide

## Common Reasons Why Email Failed:

### 1. Template Variable Mismatch ⚠️
**Most Common Issue**: Your EmailJS template might not have the correct variable names.

Go to your EmailJS template and make sure it has these **exact** variables:
- `{{to_email}}`
- `{{complaint_type}}`
- `{{complaint_details}}`
- `{{user_email}}`
- `{{complaint_id}}`
- `{{timestamp}}`

### 2. Gmail Service Not Connected
- Check EmailJS dashboard → Email Services
- Make sure Gmail service is "Active" and "Connected"

### 3. Internet Connection
- Make sure your device/emulator has internet access
- Try opening a browser to verify

### 4. EmailJS Free Tier Limit
- Free tier: 200 emails/month
- Check EmailJS dashboard to see remaining quota

---

## 🔍 How to Debug:

### Check Logcat in Android Studio:
1. Open **Logcat** (bottom panel in Android Studio)
2. Filter by: `MainActivity`
3. Look for error messages with `❌ Email failed:` or `❌ Email error:`
4. Share the error code and message with me

### Common Error Codes:
- **400**: Template variables don't match
- **401**: Invalid Public Key
- **403**: Service not connected properly
- **429**: Rate limit exceeded (too many emails sent)
- **500**: EmailJS server error

---

## ✅ Quick Fix - Updated Email Function

I'll update your email function to include better error logging so we can see exactly what's wrong.
