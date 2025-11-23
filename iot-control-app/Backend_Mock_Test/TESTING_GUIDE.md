# Complete System Testing Guide

## 🎯 Overview

Test your entire ThermaLink backend without any hardware! This guide covers testing both:
- **Temperature Sensors** (ESP32 devices in base and heater rooms)
- **Camera Detection** (Raspberry Pi with YOLO person detection)

---

## 🚀 Quick Start - Test Everything

### 1. Set Up Once
```bash
# Copy environment template
cp .env.example .env

# Edit with your MQTT credentials
nano .env
```

Add your credentials:
```env
MQTT_BROKER_HOST=your-broker.hivemq.cloud
MQTT_BROKER_PORT=8883
MQTT_USERNAME=your-username
MQTT_PASSWORD=your-password
```

### 2. Install Dependencies
```bash
pip3 install paho-mqtt python-dotenv
```

### 3. Start Your Backend
```bash
cd iot-control-app/Mobile_Backend
./mvnw spring-boot:run
```

### 4. Test Temperature Sensors
```bash
# In another terminal
python3 run_temperature_test.py
```

### 5. Test Camera Detection
```bash
# In another terminal (or after temperature test)
python3 run_mock_test.py
```

---

## 🌡️ Temperature Sensor Testing

### Available Scenarios

#### Normal Operation (Default)
Both rooms maintain stable temperatures:
```bash
python3 run_temperature_test.py normal
```

**What it does:**
- Base room: ~70°F with small variations
- Heater room: ~72°F with small variations
- Duration: ~30 readings

#### Heating Cycle
Simulates heater turning on:
```bash
python3 run_temperature_test.py heating
```

**What it does:**
- Phase 1: Both rooms at 68°F
- Phase 2: Heater room gradually warms to 74°F
- Phase 3: Stabilizes at new temperature

#### Cooling/Temperature Drop
Simulates outdoor temperature affecting base room:
```bash
python3 run_temperature_test.py cooling
```

**What it does:**
- Phase 1: Both rooms comfortable (~72-73°F)
- Phase 2: Base room cools down to 67.5°F
- Phase 3: Stabilizes with temperature difference

#### Extreme Readings
Tests various temperature ranges:
```bash
python3 run_temperature_test.py extreme
```

**What it does:**
- Freezing (32°F)
- Cold (55°F)
- Comfortable (70°F)
- Hot (85°F)
- Very hot (95°F)

#### Continuous Random
Extended testing with random walk:
```bash
python3 run_temperature_test.py continuous
```

**What it does:**
- Temperatures drift gradually
- Press Ctrl+C to stop
- Good for long-term testing

---

## 📸 Camera Detection Testing

### Available Scenarios

#### Realistic (Default)
Normal occupancy pattern:
```bash
python3 run_mock_test.py realistic
```

**What it does:**
1. Room empty (3 readings)
2. Person enters
3. Person stays (8 readings)
4. Person leaves
5. Room empty again (3 readings)

#### Rapid Changes
Quick movements in/out:
```bash
python3 run_mock_test.py rapid
```

**What it does:**
- 5 cycles of person entering and leaving
- 3-second intervals
- Tests state change handling

#### Edge Cases
Confidence threshold testing:
```bash
python3 run_mock_test.py edge
```

**What it does:**
- Tests various confidence levels (0.99 to 0.05)
- Tests threshold boundary (0.65)
- Validates backend filtering logic

#### Continuous Random
Extended random testing:
```bash
python3 run_mock_test.py continuous
```

**What it does:**
- Random state changes (20% chance per cycle)
- Press Ctrl+C to stop
- Good for long-term testing

---

## 🔄 Combined Testing

### Test Both Systems Simultaneously

**Terminal 1 - Backend:**
```bash
cd iot-control-app/Mobile_Backend
./mvnw spring-boot:run
```

**Terminal 2 - Temperature:**
```bash
python3 run_temperature_test.py continuous
```

**Terminal 3 - Camera:**
```bash
python3 run_mock_test.py continuous
```

This simulates the full system in operation!

---

## ✅ Verification Checklist

### Temperature System
- [ ] Backend receives temperature messages
- [ ] Logs show: "Received temperature reading"
- [ ] Both rooms (base & heater) appear in logs
- [ ] Data saved to `temperature_activity` table
- [ ] API endpoint works: `/api/temperature/current/all`
- [ ] API endpoint works: `/api/temperature/current/base`
- [ ] API endpoint works: `/api/temperature/current/heater`

### Camera Detection System
- [ ] Backend receives detection messages
- [ ] Logs show: "Received person detection status"
- [ ] Confidence threshold logic works (≥ 0.65)
- [ ] Data saved to `person_detection` table
- [ ] API endpoint works: `/api/person-detect/status`
- [ ] True positives logged as "Person entered"
- [ ] False positives filtered by confidence

---

## 🔍 Verify Backend Integration

### Check Logs
Backend should show:
```
Received temperature reading: {"room":"base","temperature":70.5,...}
Added temperature: 70.5°F for the room, base.

Received temperature reading: {"room":"heater","temperature":72.3,...}
Added temperature: 72.3°F for the room, heater.

Received person detection status: {"person_detected":true,...}
Person entered the room at: 2024-11-20T14:30:15.
```

### Query Database

**Temperature data:**
```bash
sqlite3 iotdb.db "SELECT * FROM temperature_activity ORDER BY recording_time DESC LIMIT 10;"
```

**Detection data:**
```bash
sqlite3 iotdb.db "SELECT * FROM person_detection ORDER BY detection_time DESC LIMIT 10;"
```

### Test REST APIs

**Get all room temperatures:**
```bash
curl http://localhost:8080/api/temperature/current/all
```

**Get specific room temperature:**
```bash
curl http://localhost:8080/api/temperature/current/base
curl http://localhost:8080/api/temperature/current/heater
```

**Get latest person detection:**
```bash
curl http://localhost:8080/api/person-detect/status
```

---

## 📊 Expected Output Examples

### Temperature Test Output
```
============================================================
ThermaLink - Mock Temperature Sensor Test
============================================================

✓ Loaded credentials from .env
  Broker: your-broker.hivemq.cloud:8883
  Username: your-username

Running scenario: heating

============================================================
HEATING CYCLE SCENARIO
============================================================
Heater room temperature increases

Phase 1: Initial state (both rooms cool)
[2024-11-20T14:30:15] 🌡️  Room: base     | Temp:  68.2°F
[2024-11-20T14:30:15] 🌡️  Room: heater   | Temp:  68.1°F
...

Phase 2: Heater turns on
[2024-11-20T14:30:25] 🌡️  Room: base     | Temp:  68.3°F
[2024-11-20T14:30:25] 🌡️  Room: heater   | Temp:  69.5°F
...
```

### Camera Test Output
```
============================================================
ThermaLink - Mock Camera Detection Test
============================================================

✓ Loaded credentials from .env
  Broker: your-broker.hivemq.cloud:8883
  Username: your-username

Running scenario: realistic

============================================================
REALISTIC SCENARIO
============================================================
Simulating: Person enters room → stays → leaves

Phase 1: Room is empty
[2024-11-20T14:30:15]   No person (confidence: 0.25)
[2024-11-20T14:30:17]   No person (confidence: 0.18)

Phase 2: Person enters room
[2024-11-20T14:30:19] 👤 Person detected (confidence: 0.88)
...
```

---

## 🐛 Troubleshooting

### Temperature Test Issues

**"Failed to connect to MQTT broker"**
- Check `.env` credentials
- Verify HiveMQ Cloud broker status
- Test connection: `ping your-broker.hivemq.cloud`

**Backend not receiving temperature data**
```bash
# Check backend is running
ps aux | grep java

# Check backend logs
tail -f iot-control-app/Mobile_Backend/logs/server-*.log

# Verify MQTT topics match: temperature/status/base and temperature/status/heater
```

**Database not updating**
```bash
# Check table exists
sqlite3 iotdb.db ".schema temperature_activity"

# Check for errors in backend logs
grep -i error iot-control-app/Mobile_Backend/logs/server-*.log
```

### Camera Test Issues

**"No person detection data found"**
- Make sure test ran successfully
- Check backend received messages
- Verify topic: `camera/status`

**Confidence threshold not working**
- Backend filters detections < 0.65
- Check `PersonDetectService.recordPersonDetection()`
- Low confidence detections still saved to DB

---

## 🎓 Testing Best Practices

### Test Order (Recommended)

1. **Individual Components First:**
   - Test temperature sensors alone
   - Test camera detection alone
   - Verify backend for each

2. **Combined Testing:**
   - Run both simultaneously
   - Check for any conflicts
   - Monitor system resources

3. **Long-Running Tests:**
   - Use `continuous` scenarios
   - Let run for 30+ minutes
   - Check for memory leaks or issues

### What to Monitor

**Backend Logs:**
- Connection status
- Message reception
- Database operations
- Any errors or warnings

**Database Size:**
```bash
# Check database file size
ls -lh iot-control-app/Mobile_Backend/iotdb.db

# Count records
sqlite3 iotdb.db "SELECT COUNT(*) FROM temperature_activity;"
sqlite3 iotdb.db "SELECT COUNT(*) FROM person_detection;"
```

**System Resources:**
```bash
# Monitor backend memory/CPU
top -p $(pgrep -f spring-boot)
```

---

## 📝 Test Report Template

```markdown
## Test Session: [Date]

### Configuration
- Backend Version: 0.0.1-SNAPSHOT
- MQTT Broker: [your-broker].hivemq.cloud
- Database: SQLite (iotdb.db)

### Temperature Sensor Tests
- [x] Normal operation
- [x] Heating cycle
- [x] Cooling/temperature drop
- [x] Extreme readings
- [x] Continuous random (30 min)

Results: ✅ All tests passed
Issues: None

### Camera Detection Tests
- [x] Realistic scenario
- [x] Rapid changes
- [x] Edge cases
- [x] Continuous random (30 min)

Results: ✅ All tests passed
Issues: None

### Combined System Test
- [x] Both systems running simultaneously
- [x] No message loss observed
- [x] Database updated correctly
- [x] APIs responsive

Results: ✅ System stable
Performance: Good (CPU < 20%, Memory < 500MB)

### Database Statistics
- Temperature records: 1,234
- Detection records: 567
- Database size: 128 KB

### Notes
- All MQTT messages received successfully
- No errors in backend logs
- API response times < 100ms
```

---

## 🎯 Next Steps

After successful testing:

1. ✅ **Document results** - Fill out test report
2. ✅ **Share findings** - Discuss with team
3. ✅ **Plan hardware deployment** - When Pi/ESP32 available
4. ⏳ **Tune parameters** - Adjust thresholds based on real data
5. ⏳ **Performance testing** - Stress test with higher message rates

---

## 📞 Quick Reference

### Temperature Commands
```bash
python3 run_temperature_test.py normal      # Default
python3 run_temperature_test.py heating     # Heater on
python3 run_temperature_test.py cooling     # Cool down
python3 run_temperature_test.py extreme     # Edge cases
python3 run_temperature_test.py continuous  # Long test
```

### Camera Commands
```bash
python3 run_mock_test.py realistic    # Default
python3 run_mock_test.py rapid        # Quick changes
python3 run_mock_test.py edge         # Thresholds
python3 run_mock_test.py continuous   # Long test
```

### Backend Commands
```bash
./mvnw spring-boot:run                # Start
./mvnw clean package                  # Build
tail -f logs/server-*.log             # View logs
```

### Database Commands
```bash
sqlite3 iotdb.db ".tables"            # List tables
sqlite3 iotdb.db ".schema"            # Show schema
sqlite3 iotdb.db "SELECT COUNT(*) FROM temperature_activity;"
sqlite3 iotdb.db "SELECT COUNT(*) FROM person_detection;"
```

---

**Ready to test?** Start with `python3 run_temperature_test.py` and `python3 run_mock_test.py`! 🚀
