#!/usr/bin/env python3
"""
Test script wrapper that loads credentials from .env file
Usage: python3 run_mock_test.py [--scenario realistic|rapid|edge|continuous]
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
    print("ThermaLink - Mock Camera Detection Test")
    print("="*60)
    print()
    print(f"✓ Loaded credentials from .env")
    print(f"  Broker: {broker_host}:{broker_port}")
    print(f"  Username: {username}")
    print()
    
    # Get scenario from command line args
    scenario = 'realistic'
    if len(sys.argv) > 1:
        if sys.argv[1] in ['realistic', 'rapid', 'edge', 'continuous']:
            scenario = sys.argv[1]
        elif sys.argv[1] in ['--scenario']:
            if len(sys.argv) > 2:
                scenario = sys.argv[2]
    
    print(f"Running scenario: {scenario}")
    print()
    print("Make sure your backend is running!")
    print("Press Ctrl+C to stop")
    print()
    
    # Run mock test
    try:
        subprocess.run([
            sys.executable,
            'mock_camera_test.py',
            '--broker-host', broker_host,
            '--broker-port', broker_port,
            '--username', username,
            '--password', password,
            '--scenario', scenario
        ])
    except KeyboardInterrupt:
        print("\n\nTest interrupted by user")
    
    print()
    print("Test complete!")
    print()
    print("Check your backend logs to verify detection data was received")
    print("Query the database: sqlite3 iotdb.db \"SELECT * FROM person_detection;\"")

if __name__ == '__main__':
    main()
