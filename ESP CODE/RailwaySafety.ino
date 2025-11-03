/*
 ==============================================================================
  RAILWAY GATE MONITORING SYSTEM (3 SENSORS)
  - BIDIRECTIONAL, OPTIMIZED, & SMOOTH SWEEP -
 ==============================================================================
  This version includes:
  - Bidirectional train detection.
  - Optimized Firebase uploads (updates 'current' fast, logs 'history' once).
  - Smooth "sweep" motion for the servo gate.
  - Fixed servo initialization issue.

  HARDWARE CONFIGURATION (ESP8266):
  ---------------------------------
  - Sensor 1 (End Point A): TRIG=D5, ECHO=D6
  - Sensor 2 (Middle Point):  TRIG=D7, ECHO=D8
  - Sensor 3 (Gate Location): TRIG=D1, ECHO=D2
  - Servo Motor Signal Pin:   D0 (GPIO16) - A safe boot pin
*/

// =============================================================================
//  LIBRARY INCLUSIONS
// =============================================================================
#include "secret.h"
#include <Firebase.h>
#include <ArduinoJson.h>
#include <time.h>
#include <Servo.h>

// =============================================================================
//  GLOBAL OBJECTS AND DEFINITIONS
// =============================================================================
Firebase fb(REFERENCE_URL);
Servo gateServo;
#define SERVO_PIN D4

#define TRIG1_PIN D5
#define ECHO1_PIN D6
#define TRIG2_PIN D7
#define ECHO2_PIN D8
#define TRIG3_PIN D1
#define ECHO3_PIN D2

// =============================================================================
//  SYSTEM CONFIGURATION CONSTANTS
// =============================================================================
const int GATE_OPEN_ANGLE   = 90;
const int GATE_CLOSED_ANGLE = 0;

const float SENSOR_GAP_1_2    = 100.0;
const float SENSOR_GAP_2_3    = 300.0;
const float THRESHOLD         = 20.0;

const char* ntpServer = "pool.ntp.org";
const long  gmtOffset_sec = 19800;
const int   daylightOffset_sec = 0;

// =============================================================================
//  GLOBAL STATE VARIABLES
// =============================================================================
int   stage          = 0;
int   trainDirection = 0;
unsigned long startTime      = 0;
unsigned long endTime        = 0;
float trainSpeed     = 0.0;
float eta            = 0.0;
bool  crossed        = false;

// =============================================================================
//  CORE FUNCTIONS
// =============================================================================

float getDistance(int trigPin, int echoPin) {
    digitalWrite(trigPin, LOW);
    delayMicroseconds(2);
    digitalWrite(trigPin, HIGH);
    delayMicroseconds(10);
    digitalWrite(trigPin, LOW);
    long duration = pulseIn(echoPin, HIGH, 30000);
    if (duration == 0) return -1;
    return (duration / 2.0) / 29.1;
}

String getCurrentTime() {
    struct tm timeinfo;
    if (!getLocalTime(&timeinfo)) return "Time N/A";
    char timeString[64];
    strftime(timeString, sizeof(timeString), "%Y-%m-%d %H:%M:%S", &timeinfo);
    return String(timeString);
}

unsigned long getUnixTimestamp() {
    time_t now;
    time(&now);
    return (unsigned long)now;
}

void updateCurrentStatus(JsonDocument& data) {
    String output;
    serializeJson(data, output);
    Serial.print("📤 Updating current status: ");
    Serial.println(output);
    int responseCode = fb.setJson("RailwayGate/current", output);
    if (responseCode == 200) {
        delay(10); // Prevents serial buffer corruption
        Serial.println("✅ Current status updated successfully");
    } else {
        delay(10);
        Serial.println("❌ Failed to update status. Response: " + String(responseCode));
    }
}

void logJourneyToHistory() {
    JsonDocument historyData;
    historyData["event"] = "journey_completed";
    historyData["speed_kmh"] = trainSpeed;
    historyData["gate_status"] = "opened";
    historyData["direction"] = (trainDirection == 1) ? "S1->S3" : "S3->S1";
    
    unsigned long finalTime = millis();
    float totalDurationSec = (finalTime - startTime) / 1000.0;
    historyData["total_duration_sec"] = totalDurationSec;

    historyData["datetime"] = getCurrentTime();
    historyData["timestamp"] = getUnixTimestamp();
    
    String output;
    serializeJson(historyData, output);
    Serial.print("📜 Logging journey to history: ");
    Serial.println(output);
    int responseCode = fb.pushJson("RailwayGate/history", output);
    if (responseCode == 200) {
        delay(10);
        Serial.println("✅ Journey logged successfully");
    } else {
        delay(10);
        Serial.println("❌ Failed to log journey. Response: " + String(responseCode));
    }
}

void gateClose() {
    Serial.println("🚪 Closing gate (sweeping)...");
    for (int pos = GATE_OPEN_ANGLE; pos >= GATE_CLOSED_ANGLE; pos--) {
        gateServo.write(pos);
        delay(5);
    }
}

void gateOpen() {
    Serial.println("🚪 Opening gate (sweeping)...");
    for (int pos = GATE_CLOSED_ANGLE; pos <= GATE_OPEN_ANGLE; pos++) {
        gateServo.write(pos);
        delay(15);
    }
}

// =============================================================================
//  SETUP: Runs once at startup
// =============================================================================
void setup() {
    Serial.begin(115200);
    delay(1000);
    
    Serial.println("\n========================================");
    Serial.println("  RAILWAY GATE MONITORING SYSTEM");
    Serial.println("  (BIDIRECTIONAL, OPTIMIZED, SWEEP)");
    Serial.println("========================================\n");
    
    pinMode(TRIG1_PIN, OUTPUT); pinMode(ECHO1_PIN, INPUT);
    pinMode(TRIG2_PIN, OUTPUT); pinMode(ECHO2_PIN, INPUT);
    pinMode(TRIG3_PIN, OUTPUT); pinMode(ECHO3_PIN, INPUT);
    Serial.println("✓ Ultrasonic Sensors Initialized");

    // --- FIXED SERVO INITIALIZATION ---
    gateServo.attach(SERVO_PIN);
    delay(500);  // Allow servo to attach properly
    gateServo.write(GATE_CLOSED_ANGLE);  // Set initial position directly
    delay(500);  // Allow servo to reach position
    Serial.println("✓ Servo Motor Initialized:");
    Serial.println("  Servo Pin: D0 (GPIO16)");
    Serial.println("  Gate Status: CLOSED\n");
    
    WiFi.mode(WIFI_STA);
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
    Serial.print("Connecting to WiFi ");
    while (WiFi.status() != WL_CONNECTED) {
        Serial.print(".");
        delay(500);
    }
    Serial.println("\n✓ WiFi Connected!\n");
    
    configTime(gmtOffset_sec, daylightOffset_sec, ntpServer);
    Serial.print("Synchronizing time ");
    struct tm timeinfo;
    while (!getLocalTime(&timeinfo)) {
        Serial.print(".");
        delay(500);
    }
    Serial.println("\n✓ Time synchronized!");
    Serial.print("Current time: ");
    Serial.println(getCurrentTime());
    Serial.println();

    Serial.println("🚂 System Ready! Waiting for train...\n");
}

// =============================================================================
//  LOOP: Main program logic, runs repeatedly
// =============================================================================
void loop() {
    if (WiFi.status() != WL_CONNECTED) {
        Serial.println("⚠️ WiFi disconnected! Reconnecting...");
        WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
        while (WiFi.status() != WL_CONNECTED) { delay(500); }
        Serial.println("✓ WiFi reconnected!");
    }

    float d1 = getDistance(TRIG1_PIN, ECHO1_PIN);
    float d2 = getDistance(TRIG2_PIN, ECHO2_PIN);
    float d3 = getDistance(TRIG3_PIN, ECHO3_PIN);
    
    if (stage == 0) {
        bool trainDetected = false;
        JsonDocument data;
        data["event"] = "train_detected";
        data["gate_status"] = "closing";

        if (d1 > 0 && d1 < THRESHOLD) {
            trainDirection = 1;
            data["direction"] = "S1->S3";
            data["sensor"] = 1;
            Serial.println("\n🚨 ===== TRAIN DETECTED AT SENSOR 1 (DIR: S1->S3) =====");
            trainDetected = true;
        } 
        else if (d3 > 0 && d3 < THRESHOLD) {
            trainDirection = -1;
            data["direction"] = "S3->S1";
            data["sensor"] = 3;
            Serial.println("\n🚨 ===== TRAIN DETECTED AT SENSOR 3 (DIR: S3->S1) =====");
            trainDetected = true;
        }

        if (trainDetected) {
            stage = 1;
            startTime = millis();
            gateClose();
            data["datetime"] = getCurrentTime();
            data["timestamp"] = getUnixTimestamp();
            updateCurrentStatus(data);
        }
    }
    else if (stage == 1 && d2 > 0 && d2 < THRESHOLD) {
        endTime = millis();
        float elapsedTimeSec = (endTime - startTime) / 1000.0;
        stage = 2;

        JsonDocument data;
        data["event"] = "speed_calculated";

        if (trainDirection == 1) {
            trainSpeed = (SENSOR_GAP_1_2 / elapsedTimeSec) * 3.6;
            eta = (trainSpeed > 0) ? (SENSOR_GAP_2_3 / (trainSpeed / 3.6)) : 0;
            Serial.println("SPEED (S1->S2): " + String(trainSpeed, 2) + " km/h, ETA to Gate: " + String(eta, 2) + "s");
            data["direction"] = "S1->S3";
        } 
        else {
            trainSpeed = (SENSOR_GAP_2_3 / elapsedTimeSec) * 3.6;
            eta = (trainSpeed > 0) ? (SENSOR_GAP_1_2 / (trainSpeed / 3.6)) : 0;
            Serial.println("SPEED (S3->S2): " + String(trainSpeed, 2) + " km/h, ETA to Exit (S1): " + String(eta, 2) + "s");
            data["direction"] = "S3->S1";
        }
        
        data["speed_kmh"] = trainSpeed;
        data["eta_sec"] = eta;
        data["gate_status"] = "closed";
        data["datetime"] = getCurrentTime();
        data["timestamp"] = getUnixTimestamp();
        updateCurrentStatus(data);
    }
    else if (stage == 2 && !crossed) {
        bool trainCrossed = false;
        JsonDocument data;
        data["event"] = "train_crossed";
        data["gate_status"] = "opened";

        if (trainDirection == 1 && d3 > 0 && d3 < THRESHOLD) {
            data["sensor"] = 3;
            data["direction"] = "S1->S3";
            Serial.println("\n🎉 ===== TRAIN CROSSED GATE AT SENSOR 3 =====");
            trainCrossed = true;
        }
        else if (trainDirection == -1 && d1 > 0 && d1 < THRESHOLD) {
            data["sensor"] = 1;
            data["direction"] = "S3->S1";
            Serial.println("\n🎉 ===== TRAIN EXITED AT SENSOR 1 =====");
            trainCrossed = true;
        }

        if (trainCrossed) {
            crossed = true;
            stage = 3;
            gateOpen();
            data["datetime"] = getCurrentTime();
            data["timestamp"] = getUnixTimestamp();
            updateCurrentStatus(data);
            logJourneyToHistory();
        }
    }
    else if (stage == 3 && d1 > THRESHOLD && d2 > THRESHOLD && d3 > THRESHOLD) {
        Serial.println("\n🔄 ===== SYSTEM RESET: All sensors clear. Ready for next train. =====");
        gateClose();
        
        JsonDocument data;
        data["event"] = "system_reset";
        data["gate_status"] = "closed";
        data["datetime"] = getCurrentTime();
        data["timestamp"] = getUnixTimestamp();
        updateCurrentStatus(data); 

        stage = 0;
        trainDirection = 0;
        startTime = 0;
        endTime = 0;
        trainSpeed = 0.0;
        eta = 0.0;
        crossed = false;
        
        delay(1000);
    }
    
    delay(50);
}
