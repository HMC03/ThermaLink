#!/usr/bin/env python3
"""
Device status test wrapper that loads credentials from .env file
Usage: python3 run_device_test.py [--scenario manual|heating|fan|rapid|continuous] [--room roomA]
"""

import os
import sys
import subprocess
from pathlib import Path

try:
    from dotenv import load_dotenv
except ImportError:
    print("ERROR: python-dotenv not installed")
    print("Installing python-dotenv...")
    subprocess.run([sys.executable, "-m", "pip", "install", "python-dotenv"])
    from dotenv import load_dotenv

def main():
    # Load .env file
    env_path = Path('.env')
    if not env_path.exists():
        print("ERROR: .env file not found!")
        print()
        print("Please create a .env file with your MQTT credentials:")
        print()
        print("MQTT_BROKER_HOST=your-broker.hivemq.cloud")
        print("MQTT_BROKER_PORT=8883")
        print("MQTT_USERNAME=your-username")
        print("MQTT_PASSWORD=your-password")
        print()
        sys.exit(1)
    
    load_dotenv(env_path)
    
    # Get credentials from environment
    broker_host = os.getenv('MQTT_BROKER_HOST')
    broker_port = os.getenv('MQTT_BROKER_PORT', '8883')
    username = os.getenv('MQTT_USERNAME')
    password = os.getenv('MQTT_PASSWORD')
    
    # Validate required variables
    if not broker_host or not username or not password:
        print("ERROR: Missing required environment variables in .env")
        print("Required: MQTT_BROKER_HOST, MQTT_USERNAME, MQTT_PASSWORD")
        sys.exit(1)
    
    print("="*60)
    print("ThermaLink - Mock Heater/Fan Status Test")
    print("="*60)
    print()
    print(f"✓ Loaded credentials from .env")
    print(f"  Broker: {broker_host}:{broker_port}")
    print(f"  Username: {username}")
    print()
    
    # Get scenario and room from command line args
    scenario = 'manual'
    room = 'roomA'
    
    i = 1
    while i < len(sys.argv):
        if sys.argv[i] in ['--scenario']:
            if i + 1 < len(sys.argv):
                scenario = sys.argv[i + 1]
                i += 2
        elif sys.argv[i] in ['--room']:
            if i + 1 < len(sys.argv):
                room = sys.argv[i + 1]
                i += 2
        elif sys.argv[i] in ['manual', 'heating', 'fan', 'rapid', 'continuous']:
            scenario = sys.argv[i]
            i += 1
        else:
            i += 1
    
    print(f"Running scenario: {scenario}")
    print(f"Room: {room}")
    print()
    
    scenario_descriptions = {
        'manual': 'User manually controls heater and fan',
        'heating': 'Automatic heating cycle (thermostat mode)',
        'fan': 'Fan running for air circulation only',
        'rapid': 'Quick on/off state changes',
        'continuous': 'Continuous random status updates'
    }
    
    print(f"Scenario: {scenario_descriptions.get(scenario, 'Unknown')}")
    print()
    print("Simulating devices:")
    print(f"  • Heater ({room}/heater/status)")
    print(f"  • Fan ({room}/fan/status)")
    print()
    print("Make sure your backend is running!")
    print("Press Ctrl+C to stop")
    print()
    
    # Run mock test
    try:
        subprocess.run([
            sys.executable,
            'mock_device_status_test.py',
            '--broker-host', broker_host,
            '--broker-port', broker_port,
            '--username', username,
            '--password', password,
            '--room', room,
            '--scenario', scenario
        ])
    except KeyboardInterrupt:
        print("\n\nTest interrupted by user")
    
    print()
    print("Test complete!")
    print()
    print("Verify backend received device status data:")
    print("  • Check backend logs for 'Received latest heater/fan sensor activity'")
    print("  • Query database: sqlite3 iotdb.db \"SELECT * FROM heater_activity ORDER BY recording_time DESC LIMIT 5;\"")
    print("  • Query database: sqlite3 iotdb.db \"SELECT * FROM fan_activity ORDER BY recording_time DESC LIMIT 5;\"")
    print("  • Test API: curl http://localhost:8080/api/heater/status/all")
    print("  • Test API: curl http://localhost:8080/api/fan/status/all")

if __name__ == '__main__':
    main()
