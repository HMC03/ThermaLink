#!/usr/bin/env python3
"""
Mock Heater/Fan Status for Testing
Simulates ESP32 publishing heater and fan status without requiring actual hardware
"""

import paho.mqtt.client as mqtt
import json
import time
import random
from datetime import datetime
import argparse


class MockDeviceStatus:
    def __init__(self, broker_host, broker_port, username, password, 
                 room_type="roomA", device_type="heater"):
        """Initialize mock device status publisher"""
        self.broker_host = broker_host
        self.broker_port = broker_port
        self.username = username
        self.password = password
        self.room_type = room_type
        self.device_type = device_type  # "heater" or "fan"
        self.mqtt_topic = f"{room_type}/{device_type}/status"
        
        # Initialize MQTT client
        client_id = f"mock_{device_type}_{room_type}"
        self.mqtt_client = mqtt.Client(client_id=client_id)
        self.mqtt_client.username_pw_set(username, password)
        self.mqtt_client.tls_set()
        
        # Set up callbacks
        self.mqtt_client.on_connect = self.on_connect
        self.mqtt_client.on_disconnect = self.on_disconnect
        self.mqtt_client.on_publish = self.on_publish
        
        self.is_connected = False
        self.current_status = False  # Start with device OFF
        
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
    
    def publish_status(self, status):
        """Publish device status to MQTT broker"""
        timestamp = datetime.now().strftime("%Y-%m-%dT%H:%M:%S")
        
        payload = {
            "status": status,
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
            icon = "🔥" if self.device_type == "heater" else "🌀"
            state = "ON " if status else "OFF"
            print(f"[{timestamp}] {icon} {self.device_type.capitalize():6s} | Room: {self.room_type:8s} | Status: {state}")
        else:
            print(f"✗ Failed to publish. Return code: {result.rc}")
    
    def cleanup(self):
        """Clean up MQTT connection"""
        self.mqtt_client.loop_stop()
        self.mqtt_client.disconnect()


class DualDeviceSimulator:
    """Simulate both heater and fan status for a room"""
    
    def __init__(self, broker_host, broker_port, username, password, room_type="roomA"):
        self.heater = MockDeviceStatus(
            broker_host, broker_port, username, password,
            room_type=room_type, device_type="heater"
        )
        self.fan = MockDeviceStatus(
            broker_host, broker_port, username, password,
            room_type=room_type, device_type="fan"
        )
        
        # Connect both devices
        print(f"Connecting {room_type} heater status publisher...")
        self.heater.connect()
        print(f"Connecting {room_type} fan status publisher...")
        self.fan.connect()
        print()
    
    def scenario_manual_control(self):
        """Simulate manual control - user turning devices on/off"""
        print("="*60)
        print("MANUAL CONTROL SCENARIO")
        print("="*60)
        print("Simulating user manually controlling heater and fan\n")
        
        try:
            # Initial state - both OFF
            print("Phase 1: Initial state (both devices OFF)")
            self.heater.publish_status(False)
            self.fan.publish_status(False)
            time.sleep(3)
            
            # Turn heater ON
            print("\nPhase 2: User turns heater ON")
            self.heater.publish_status(True)
            time.sleep(3)
            
            # Turn fan ON
            print("\nPhase 3: User turns fan ON")
            self.fan.publish_status(True)
            time.sleep(3)
            
            # Both running
            print("\nPhase 4: Both devices running")
            for i in range(5):
                self.heater.publish_status(True)
                self.fan.publish_status(True)
                time.sleep(2)
            
            # Turn fan OFF
            print("\nPhase 5: User turns fan OFF")
            self.fan.publish_status(False)
            time.sleep(3)
            
            # Turn heater OFF
            print("\nPhase 6: User turns heater OFF")
            self.heater.publish_status(False)
            time.sleep(2)
            
        except KeyboardInterrupt:
            print("\n\nTest interrupted by user")
    
    def scenario_heating_cycle(self):
        """Simulate automatic heating cycle"""
        print("="*60)
        print("AUTOMATIC HEATING CYCLE SCENARIO")
        print("="*60)
        print("Simulating automatic temperature control\n")
        
        try:
            # Room too cold - heater turns on
            print("Phase 1: Room cold (68°F), heater activates")
            self.heater.publish_status(True)
            self.fan.publish_status(False)
            time.sleep(2)
            
            # Heating up
            print("\nPhase 2: Heating up...")
            for i in range(8):
                self.heater.publish_status(True)
                self.fan.publish_status(False)
                time.sleep(2)
            
            # Target reached, heater cycles off
            print("\nPhase 3: Target temperature reached (72°F), heater OFF")
            self.heater.publish_status(False)
            self.fan.publish_status(False)
            time.sleep(3)
            
            # Temperature drops slightly
            print("\nPhase 4: Temperature drops (71°F), waiting...")
            for i in range(3):
                self.heater.publish_status(False)
                self.fan.publish_status(False)
                time.sleep(2)
            
            # Heater turns back on
            print("\nPhase 5: Too cold again (70°F), heater ON")
            self.heater.publish_status(True)
            self.fan.publish_status(False)
            time.sleep(3)
            
        except KeyboardInterrupt:
            print("\n\nTest interrupted by user")
    
    def scenario_fan_only(self):
        """Simulate fan running without heater (circulation mode)"""
        print("="*60)
        print("FAN CIRCULATION SCENARIO")
        print("="*60)
        print("Simulating fan running for air circulation\n")
        
        try:
            # Turn fan on for circulation
            print("Phase 1: Fan ON for air circulation")
            self.heater.publish_status(False)
            self.fan.publish_status(True)
            time.sleep(2)
            
            # Run for a while
            print("\nPhase 2: Circulating air...")
            for i in range(10):
                self.heater.publish_status(False)
                self.fan.publish_status(True)
                time.sleep(2)
            
            # Turn fan off
            print("\nPhase 3: Circulation complete, fan OFF")
            self.heater.publish_status(False)
            self.fan.publish_status(False)
            time.sleep(2)
            
        except KeyboardInterrupt:
            print("\n\nTest interrupted by user")
    
    def scenario_rapid_changes(self):
        """Simulate rapid on/off changes"""
        print("="*60)
        print("RAPID CHANGES SCENARIO")
        print("="*60)
        print("Simulating quick device state changes\n")
        
        try:
            for cycle in range(5):
                print(f"\nCycle {cycle + 1}/5:")
                
                # Both ON
                print("  Both ON")
                self.heater.publish_status(True)
                self.fan.publish_status(True)
                time.sleep(2)
                
                # Both OFF
                print("  Both OFF")
                self.heater.publish_status(False)
                self.fan.publish_status(False)
                time.sleep(2)
                
        except KeyboardInterrupt:
            print("\n\nTest interrupted by user")
    
    def scenario_continuous(self, interval=2.0):
        """Continuous status updates"""
        print("="*60)
        print("CONTINUOUS STATUS UPDATES")
        print("="*60)
        print(f"Publishing status updates every {interval}s")
        print("Press Ctrl+C to stop\n")
        
        try:
            heater_on = False
            fan_on = False
            
            while True:
                # Random state changes (10% chance each cycle)
                if random.random() < 0.1:
                    heater_on = not heater_on
                if random.random() < 0.1:
                    fan_on = not fan_on
                
                self.heater.publish_status(heater_on)
                self.fan.publish_status(fan_on)
                
                time.sleep(interval)
                
        except KeyboardInterrupt:
            print("\n\nStopping continuous simulation")
    
    def cleanup(self):
        """Clean up both devices"""
        print("\nDisconnecting devices...")
        self.heater.cleanup()
        self.fan.cleanup()
        print("✓ Cleanup complete")


def main():
    parser = argparse.ArgumentParser(
        description='Mock Heater/Fan Status for Testing Backend'
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
    parser.add_argument('--room', default='roomA',
                       help='Room type (default: roomA)')
    
    # Scenario selection
    parser.add_argument('--scenario', 
                       choices=['manual', 'heating', 'fan', 'rapid', 'continuous'],
                       default='manual',
                       help='Test scenario to run (default: manual)')
    parser.add_argument('--interval', type=float, default=2.0,
                       help='Interval for continuous mode (default: 2.0)')
    
    args = parser.parse_args()
    
    try:
        # Initialize dual device simulator
        simulator = DualDeviceSimulator(
            broker_host=args.broker_host,
            broker_port=args.broker_port,
            username=args.username,
            password=args.password,
            room_type=args.room
        )
        
        # Run selected scenario
        if args.scenario == 'manual':
            simulator.scenario_manual_control()
        elif args.scenario == 'heating':
            simulator.scenario_heating_cycle()
        elif args.scenario == 'fan':
            simulator.scenario_fan_only()
        elif args.scenario == 'rapid':
            simulator.scenario_rapid_changes()
        elif args.scenario == 'continuous':
            simulator.scenario_continuous(interval=args.interval)
        
        # Cleanup
        simulator.cleanup()
        
        print("\n✓ Test completed successfully")
        
    except Exception as e:
        print(f"\n✗ Error: {e}")
        return 1
    
    return 0


if __name__ == '__main__':
    exit(main())
