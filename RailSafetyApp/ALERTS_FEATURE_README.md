# Railway Safety App - Alerts Feature

## Overview
The Alerts tab now displays a comprehensive history of:
- **Gate Events**: All gate open/close events
- **Complaints**: All filed complaints with their status
- **System Alerts**: Other safety and maintenance alerts

## Features

### 1. Gate Events Tracking
- Automatically logs when the railway crossing gate opens or closes
- Shows timestamp and status for each event
- High priority for "Gate Closed" events (train approaching)
- Medium priority for "Gate Opened" events

### 2. Complaints History
- All user-submitted complaints are displayed
- Shows complaint type, details, and timestamp
- Status-based priority:
  - **Pending**: Medium priority (yellow)
  - **In Progress**: High priority (orange)
  - **Resolved**: Low priority (green)

### 3. Filtering Options
Three tabs to filter alerts:
- **Active**: Shows unread/unresolved alerts
- **History**: Shows resolved/read alerts
- **All**: Shows all alerts

## Firebase Structure

### Gate Events
```
gate_events/
  ├── {unique_id}/
  │   ├── event: "opened" or "closed"
  │   ├── timestamp: "2025-10-29 12:30:45"
  │   └── status: "active" or "resolved"
```

### Complaints
```
complaints/
  ├── {unique_id}/
  │   ├── type: "Gate Malfunction" etc.
  │   ├── details: "Description of complaint"
  │   ├── timestamp: "2025-10-29 12:30:45"
  │   └── status: "pending", "in_progress", or "resolved"
```

## How to Test

### 1. Add Sample Gate Events
You can manually add gate events to Firebase:
```json
{
  "gate_events": {
    "event1": {
      "event": "closed",
      "timestamp": "2025-10-29 09:30:00",
      "status": "active"
    },
    "event2": {
      "event": "opened",
      "timestamp": "2025-10-29 09:35:00",
      "status": "active"
    }
  }
}
```

### 2. File a Complaint
1. Open the app
2. Tap on the menu (hamburger icon)
3. Select "File Complaint"
4. Choose complaint type
5. Enter details
6. Submit

The complaint will automatically appear in the Alerts tab!

### 3. Using the LogGateEvent Function
In your code, you can programmatically log gate events:
```kotlin
val repository = FirebaseRepository.getInstance()
repository.logGateEvent("opened")  // When gate opens
repository.logGateEvent("closed")  // When gate closes
```

## UI Features

- **Color-coded priorities**:
  - 🔴 Red: Critical
  - 🟠 Orange: High
  - 🟡 Yellow: Medium
  - 🟢 Green: Low

- **Icons in titles**:
  - 🚧 Gate Opened
  - 🔒 Gate Closed
  - 📝 Complaint

- **Timestamp formatting**:
  - "Just now" for recent events
  - "X mins ago", "X hours ago", "X days ago"
  - Full date for older events

## Implementation Details

### Key Components Modified:
1. **FirebaseRepository.kt**
   - Added `gateEventsRef` and `complaintsRef`
   - Added listeners for gate events and complaints
   - Added `logGateEvent()` function
   - Combined all alerts into `allAlerts` LiveData

2. **SlideshowViewModel.kt**
   - Now observes Firebase data instead of hardcoded alerts
   - Real-time updates when new events/complaints are added

3. **SlideshowFragment.kt**
   - Displays combined alerts from all sources
   - Filtering functionality for Active/History/All

## Future Enhancements
- Mark alerts as read
- Delete old alerts
- Filter by alert type
- Search functionality
- Notification for new alerts
- Export alerts to PDF/CSV
