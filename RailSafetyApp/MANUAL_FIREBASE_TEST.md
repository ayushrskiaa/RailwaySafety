# 🧪 Manual Firebase Test

## Test 1: Add Test Data to Firebase Console

1. Go to: https://console.firebase.google.com/
2. Select project: **iot-implementation-e7fcd**
3. Click: **Realtime Database**
4. Click the **+** icon next to the database root
5. Add this data manually:

```
logs/
  └── test123
      ├── event: "train_detected"
      ├── gate_status: "closing"
      ├── speed: 45.5
      ├── eta: 25.3
      └── timestamp: 1698345678
```

**Steps:**
- Name: `logs`
- Click **+** to add child
- Name: `test123`
- Click **+** inside test123
- Add: `event` = `train_detected`
- Add: `gate_status` = `closing`
- Add: `speed` = `45.5` (number)
- Add: `eta` = `25.3` (number)
- Add: `timestamp` = `1698345678` (number)

6. Click **Add**

7. **Restart your Android app**

8. **Check Logcat** - You should see:
```
D/RailwayCrossingRepo: onDataChange called - logs exist: true, count: 1
D/RailwayCrossingRepo: Log entry found: test123
D/RailwayCrossingRepo:   event = train_detected
D/RailwayCrossingRepo:   gate_status = closing
```

---

## Test 2: Check Firebase Rules

**If still no data, check rules:**

1. Firebase Console → **Realtime Database** → **Rules** tab
2. Should see:

```json
{
  "rules": {
    ".read": true,
    ".write": true
  }
}
```

**If different, change to above and click Publish**

---

## Test 3: Add Test Code to MainActivity

Add this to `MainActivity.onCreate()` to test Firebase write:

```kotlin
// Test Firebase Write
FirebaseDatabase.getInstance().getReference("logs/testFromApp")
    .setValue(mapOf(
        "event" to "train_detected",
        "gate_status" to "closing",
        "speed" to 45.5,
        "eta" to 25.3,
        "timestamp" to System.currentTimeMillis()
    ))
    .addOnSuccessListener {
        Log.d("FirebaseTest", "✅ Test data written successfully!")
        Toast.makeText(this, "Firebase write OK!", Toast.LENGTH_LONG).show()
    }
    .addOnFailureListener { e ->
        Log.e("FirebaseTest", "❌ Write failed: ${e.message}")
        Toast.makeText(this, "Firebase error: ${e.message}", Toast.LENGTH_LONG).show()
    }
```

**Run app and check:**
- Toast appears with "Firebase write OK!"?
- Firebase Console shows new data?

---

## What Your Logcat Tells Us:

✅ **App is running correctly**
✅ **Firebase SDK initialized**
✅ **Repository created and listening**
❌ **No data in Firebase database**
❌ **ESP32 not sending data OR sending to wrong database**

## Next Steps:

1. **Check Firebase Console** - Does `logs/` exist?
2. **If NO** - Add test data manually (see Test 1 above)
3. **Restart app** - Should see "onDataChange called - logs exist: true"
4. **If YES** - Check Firebase Rules allow read access

**Tell me what you see in Firebase Console!** 🔍
