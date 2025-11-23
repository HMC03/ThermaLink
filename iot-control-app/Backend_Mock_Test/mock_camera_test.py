#!/usr/bin/env python3
"""
Mock Camera Detection for Testing
Simulates person detection events without requiring actual camera hardware
"""

import paho.mqtt.client as mqtt
import json
import time
import random
from datetime import datetime
import argparse


class MockCameraDetection:
    def __init__(self, broker_host, broker_port, username, password, mqtt_topic="camera/status"):
        """Initialize mock camera detection"""
        self.broker_host = broker_host
        self.broker_port = broker_port
        self.username = username
        self.password = password
        self.mqtt_topic = mqtt_topic
        
        # Initialize MQTT client
        self.mqtt_client = mqtt.Client(client_id="mock_camera_test")
        self.mqtt_client.username_pw_set(username, password)
        self.mqtt_client.tls_set()
        
        # Set up callbacks
        self.mqtt_client.on_connect = self.on_connect
        self.mqtt_client.on_disconnect = self.on_disconnect
        self.mqtt_client.on_publish = self.on_publish
        
        self.is_connected = False
        
    def on_connect(self, client, userdata, flags, rc):
        """MQTT connection callback"""
        if rc == 0:
            print(f"✓ Connected to MQTT broker at {self.broker_host}:{self.broker_port}")
            self.is_connected = True
        else:
            print(f"✗ Failed to connect. Return code: {rc}")
            self.is_connected = False
    
    def on_disconnect(self, client, userdata, rc):
        """MQTT disconnection callback"""
        print(f"Disconnected from MQTT broker")
        self.is_connected = False
        
    def on_publish(self, client, userdata, mid):
        """MQTT publish callback"""
        pass  # Silent success
    
    def connect(self):
        """Connect to MQTT broker"""
        print(f"Connecting to MQTT broker at {self.broker_host}:{self.broker_port}...")
        self.mqtt_client.connect(self.broker_host, self.broker_port, keepalive=60)
        self.mqtt_client.loop_start()
        
        # Wait for connection
        timeout = 10
        start_time = time.time()
        while not self.is_connected and (time.time() - start_time) < timeout:
            time.sleep(0.1)
        
        if not self.is_connected:
            raise RuntimeError("Failed to connect to MQTT broker")
    
    def publish_detection(self, person_detected, confidence):
        """Publish mock detection result"""
        timestamp = datetime.now().strftime("%Y-%m-%dT%H:%M:%S")
        
        payload = {
            "person_detected": person_detected,
            "confidence": round(confidence, 2),
            "timestamp": timestamp
        }
        
        json_payload = json.dumps(payload)
        
        result = self.mqtt_client.publish(
            self.mqtt_topic,
            json_payload,
            qos=1,
            retain=False
        )
        
        if result.rc == mqtt.MQTT_ERR_SUCCESS:
            status = "👤 Person detected" if person_detected else "  No person"
            print(f"[{timestamp}] {status} (confidence: {confidence:.2f})")
        else:
            print(f"✗ Failed to publish. Return code: {result.rc}")
    
    def run_scenario_realistic(self):
        """
        Realistic scenario: Person enters, stays for a while, then leaves
        """
        print("\n" + "="*60)
        print("REALISTIC SCENARIO")
        print("="*60)
        print("Simulating: Person enters room → stays → leaves\n")
        
        try:
            # Room empty
            print("Phase 1: Room is empty")
            for i in range(3):
                self.publish_detection(False, random.uniform(0.10, 0.30))
                time.sleep(2)
            
            # Person enters
            print("\nPhase 2: Person enters room")
            self.publish_detection(True, 0.88)
            time.sleep(2)
            
            # Person present
            print("\nPhase 3: Person present in room")
            for i in range(8):
                confidence = random.uniform(0.75, 0.95)
                self.publish_detection(True, confidence)
                time.sleep(2)
            
            # Person leaves
            print("\nPhase 4: Person leaves room")
            self.publish_detection(False, 0.25)
            time.sleep(2)
            
            # Room empty again
            print("\nPhase 5: Room empty again")
            for i in range(3):
                self.publish_detection(False, random.uniform(0.10, 0.30))
                time.sleep(2)
                
        except KeyboardInterrupt:
            print("\n\nTest interrupted by user")
    
    def run_scenario_rapid_changes(self):
        """
        Scenario with rapid state changes (person walking in/out)
        """
        print("\n" + "="*60)
        print("RAPID CHANGES SCENARIO")
        print("="*60)
        print("Simulating: Quick movements in/out of frame\n")
        
        try:
            for cycle in range(5):
                print(f"\nCycle {cycle + 1}/5:")
                
                # Person enters
                self.publish_detection(True, random.uniform(0.70, 0.90))
                time.sleep(3)
                
                # Person leaves
                self.publish_detection(False, random.uniform(0.15, 0.35))
                time.sleep(3)
                
        except KeyboardInterrupt:
            print("\n\nTest interrupted by user")
    
    def run_scenario_edge_cases(self):
        """
        Test edge cases and confidence thresholds
        """
        print("\n" + "="*60)
        print("EDGE CASES SCENARIO")
        print("="*60)
        print("Testing various confidence levels\n")
        
        test_cases = [
            (True, 0.99, "Very high confidence detection"),
            (True, 0.65, "Just above threshold (0.65)"),
            (True, 0.64, "Just below threshold"),
            (True, 0.50, "Medium-low confidence"),
            (False, 0.30, "No person, low confidence"),
            (False, 0.05, "No person, very low confidence"),
            (True, 0.85, "Clear detection"),
        ]
        
        try:
            for person_detected, confidence, description in test_cases:
                print(f"\n{description}:")
                self.publish_detection(person_detected, confidence)
                time.sleep(3)
                
        except KeyboardInterrupt:
            print("\n\nTest interrupted by user")
    
    def run_continuous(self, interval=2.0):
        """
        Continuous random detection simulation
        """
        print("\n" + "="*60)
        print("CONTINUOUS RANDOM SIMULATION")
        print("="*60)
        print(f"Publishing random detections every {interval}s")
        print("Press Ctrl+C to stop\n")
        
        try:
            person_present = False
            
            while True:
                # Random state change (20% chance each cycle)
                if random.random() < 0.2:
                    person_present = not person_present
                
                if person_present:
                    confidence = random.uniform(0.65, 0.95)
                    self.publish_detection(True, confidence)
                else:
                    confidence = random.uniform(0.05, 0.40)
                    self.publish_detection(False, confidence)
                
                time.sleep(interval)
                
        except KeyboardInterrupt:
            print("\n\nStopping continuous simulation")
    
    def cleanup(self):
        """Clean up MQTT connection"""
        print("\nDisconnecting from MQTT broker...")
        self.mqtt_client.loop_stop()
        self.mqtt_client.disconnect()
        print("✓ Cleanup complete")


def main():
    parser = argparse.ArgumentParser(
        description='Mock Camera Detection for Testing Backend'
    )
    
    # MQTT settings
    parser.add_argument('--broker-host', required=True,
                       help='MQTT broker hostname')
    parser.add_argument('--broker-port', type=int, default=8883,
                       help='MQTT broker port (default: 8883)')
    parser.add_argument('--username', required=True,
                       help='MQTT username')
    parser.add_argument('--password', required=True,
                       help='MQTT password')
    parser.add_argument('--topic', default='camera/status',
                       help='MQTT topic (default: camera/status)')
    
    # Scenario selection
    parser.add_argument('--scenario', 
                       choices=['realistic', 'rapid', 'edge', 'continuous'],
                       default='realistic',
                       help='Test scenario to run (default: realistic)')
    parser.add_argument('--interval', type=float, default=2.0,
                       help='Interval for continuous mode (default: 2.0)')
    
    args = parser.parse_args()
    
    try:
        # Initialize mock detector
        detector = MockCameraDetection(
            broker_host=args.broker_host,
            broker_port=args.broker_port,
            username=args.username,
            password=args.password,
            mqtt_topic=args.topic
        )
        
        # Connect to broker
        detector.connect()
        
        # Run selected scenario
        if args.scenario == 'realistic':
            detector.run_scenario_realistic()
        elif args.scenario == 'rapid':
            detector.run_scenario_rapid_changes()
        elif args.scenario == 'edge':
            detector.run_scenario_edge_cases()
        elif args.scenario == 'continuous':
            detector.run_continuous(interval=args.interval)
        
        # Cleanup
        detector.cleanup()
        
        print("\n✓ Test completed successfully")
        
    except Exception as e:
        print(f"\n✗ Error: {e}")
        return 1
    
    return 0


if __name__ == '__main__':
    exit(main())
