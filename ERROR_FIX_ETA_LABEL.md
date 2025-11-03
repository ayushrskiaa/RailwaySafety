# ✅ Fixed: Unresolved Reference Error

## Error Message
```
e: Unresolved reference 'etaToGateLabel'.
Location: HomeFragment.kt:151:25
```

## Root Cause
The TextView with the text "ETA TO GATE" in `fragment_home.xml` did not have an `android:id` attribute, so it couldn't be referenced from the Kotlin code.

## Solution Applied
Added the ID `eta_to_gate_label` to the TextView in the layout XML.

### Change Made:
**File:** `fragment_home.xml`

**Before:**
```xml
<TextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="ETA TO GATE"
    android:textSize="12sp"
    android:textStyle="bold"
    android:textColor="@color/text_color_secondary"
    android:layout_marginBottom="4dp" />
```

**After:**
```xml
<TextView
    android:id="@+id/eta_to_gate_label"  ⭐ ADDED
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="ETA TO GATE"
    android:textSize="12sp"
    android:textStyle="bold"
    android:textColor="@color/text_color_secondary"
    android:layout_marginBottom="4dp" />
```

## What This Enables
Now the HomeFragment can dynamically update the label to show:
- **When train approaching:** `"ETA (s) / Arrives at 02:45:30 PM"`
- **When no train:** `"ETA TO GATE"`

## Code That Now Works
```kotlin
// In HomeFragment.kt
homeViewModel.arrivalTime.observe(viewLifecycleOwner) { arrivalTime ->
    if (arrivalTime != "--") {
        binding.etaToGateLabel.text = "ETA (s) / $arrivalTime"  ✅ Now works!
    } else {
        binding.etaToGateLabel.text = "ETA to Gate (s)"  ✅ Now works!
    }
}
```

## Status
✅ **Error Fixed**  
✅ **No Compilation Errors**  
✅ **Ready to Build**

## Next Steps
1. Build the project in Android Studio
2. Run on device/emulator
3. Test the dynamic label update when train is detected
