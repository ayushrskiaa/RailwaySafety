# 🔧 Fix: Gmail Authentication Error (412)

## Error Message:
```
412 Gmail_API: Request had insufficient authentication scopes.
```

## What This Means:
EmailJS needs permission to **send emails** on behalf of your Gmail account, but you only granted **"view email"** permission.

---

## ✅ Solution: Grant "Send Email" Permission

### Step 1: Disconnect Current Gmail Connection
1. In the EmailJS "Config Service" dialog (the one showing the error)
2. Click **"Disconnect"** button (next to "Connected as ayushrskiaa@gmail.com")

### Step 2: Reconnect with Proper Permissions
1. Click **"Connect Gmail"** button again
2. You'll see Google's permission screen
3. **IMPORTANT**: When Google asks for permissions, make sure you see:
   - ✅ **"Send email on your behalf"** 
   - ✅ **"View your email messages"**
4. Click **"Allow"** to grant both permissions

### Step 3: Complete Setup
1. The "Service ID" field should auto-fill with `service_5crjwbf` (you already have this!)
2. **Check the box**: ☑️ "Send test email to verify configuration"
3. Click **"Create Service"**
4. Check your inbox (`ayushrskiaa@gmail.com`) for the test email

---

## Alternative: Use App Password (Recommended for Security)

If the above doesn't work, use a Gmail App Password:

### Step 1: Create App Password
1. Go to your Google Account: https://myaccount.google.com/
2. Navigate to **Security** → **2-Step Verification** (enable if not already)
3. Go to **App Passwords**
4. Generate a new app password:
   - App: **Mail**
   - Device: **Other (Custom name)** → Type: "EmailJS Railway Safety"
5. Copy the 16-character password (e.g., `abcd efgh ijkl mnop`)

### Step 2: Configure EmailJS with App Password
1. In EmailJS, disconnect Gmail if connected
2. Instead of "Gmail", select **"Generic SMTP Server"**
3. Configure:
   - **SMTP Server**: `smtp.gmail.com`
   - **Port**: `587`
   - **Username**: `ayushrskiaa@gmail.com`
   - **Password**: [Paste the 16-character app password]
   - **From Email**: `ayushrskiaa@gmail.com`
   - **From Name**: `Railway Safety System`
4. Click "Create Service"

---

## After Successful Connection:

Once you see ✅ **"Service created successfully"**, you'll have your:
- ✅ **Service ID**: `service_5crjwbf` (already in your code!)
- 🔜 **Template ID**: Create next
- 🔜 **Public Key**: Get from Account settings

---

## Next Steps:

### 1. Create Email Template
1. Go to **"Email Templates"** in EmailJS dashboard
2. Click **"Create New Template"**
3. Copy this template:

**Subject:**
```
🚨 Railway Safety Complaint: {{complaint_type}}
```

**Body:**
```html
Railway Safety Alert
====================

New Complaint Received

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
Automated notification from Railway Safety App
Please investigate and respond promptly.
```

4. Click **"Save"**
5. **Copy the Template ID** (e.g., `template_xyz456`)

### 2. Get Public Key
1. Go to **"Account"** → **"General"**
2. Find **"Public Key"** section
3. **Copy the key** (e.g., `user_ABC123xyz`)

### 3. Update MainActivity.kt

Tell me your:
- **Template ID**: (after creating template)
- **Public Key**: (from Account settings)

And I'll update your code immediately!

---

## Why This Happens:
Google requires explicit permission for apps to **send emails**. By default, connecting Gmail only grants **read** permissions. You need to grant **send** permissions for EmailJS to work.
