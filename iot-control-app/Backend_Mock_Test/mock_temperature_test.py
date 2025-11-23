#!/usr/bin/env python3
"""
Mock Temperature Sensor for Testing
Simulates ESP32 temperature sensors without requiring actual hardware
"""

import paho.mqtt.client as mqtt
import json
import time
import random
from datetime import datetime
import argparse


class MockTemperatureSensor:
    def __init__(self, broker_host, broker_port, username, password, 
                 room_type="base", mqtt_topic_base="temperature/status"):
        """Initialize mock temperature sensor"""
        self.broker_host = broker_host
        self.broker_port = broker_port
        self.username = username
        self.password = password
        self.room_type = room_type
        self.mqtt_topic = f"{mqtt_topic_base}/{room_type}"
        
        # Initialize MQTT client
        client_id = f"mock_temp_{room_type}"
        self.mqtt_client = mqtt.Client(client_id=client_id)
        self.mqtt_client.username_pw_set(username, password)
        self.mqtt_client.tls_set()
        
        # Set up callbacks
        self.mqtt_client.on_connect = self.on_connect
        self.mqtt_client.on_disconnect = self.on_disconnect
        self.mqtt_client.on_publish = self.on_publish
        
        self.is_connected = False
        self.current_temp = 70.0  # Start at 70°F
        
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
    
    def publish_temperature(self, temperature):
        """Publish temperature reading to MQTT broker"""
        timestamp = datetime.now().strftime("%Y-%m-%dT%H:%M:%S")
        
        payload = {
            "room": self.room_type,
            "temperature": round(temperature, 1),
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
            print(f"[{timestamp}] 🌡️  Room: {self.room_type:8s} | Temp: {temperature:5.1f}°F")
        else:
            print(f"✗ Failed to publish. Return code: {result.rc}")
    
    def simulate_gradual_change(self, target_temp, steps=10, step_delay=1.0):
        """Gradually change temperature to target"""
        temp_diff = target_temp - self.current_temp
        step_size = temp_diff / steps
        
        for i in range(steps):
            self.current_temp += step_size
            self.publish_temperature(self.current_temp)
            time.sleep(step_delay)
    
    def simulate_stable(self, duration=30, interval=2.0, variation=0.5):
        """Simulate stable temperature with small variations"""
        start_time = time.time()
        
        while (time.time() - start_time) < duration:
            # Add small random variation
            temp_variation = random.uniform(-variation, variation)
            temp = self.current_temp + temp_variation
            
            self.publish_temperature(temp)
            time.sleep(interval)
    
    def cleanup(self):
        """Clean up MQTT connection"""
        self.mqtt_client.loop_stop()
        self.mqtt_client.disconnect()


class DualRoomSimulator:
    """Simulate both base and heater room sensors"""
    
    def __init__(self, broker_host, broker_port, username, password):
        self.base_sensor = MockTemperatureSensor(
            broker_host, broker_port, username, password,
            room_type="base"
        )
        self.heater_sensor = MockTemperatureSensor(
            broker_host, broker_port, username, password,
            room_type="heater"
        )
        
        # Connect both sensors
        print("Connecting base room sensor...")
        self.base_sensor.connect()
        print("Connecting heater room sensor...")
        self.heater_sensor.connect()
        print()
    
    def scenario_normal_operation(self):
        """Simulate normal operation with stable temperatures"""
        print("="*60)
        print("NORMAL OPERATION SCENARIO")
        print("="*60)
        print("Both rooms maintain stable temperatures\n")
        
        try:
            self.base_sensor.current_temp = 70.0
            self.heater_sensor.current_temp = 72.0
            
            for i in range(15):
                # Base room varies slightly around 70°F
                base_temp = 70.0 + random.uniform(-1.0, 1.0)
                self.base_sensor.publish_temperature(base_temp)
                
                # Heater room varies around 72°F
                heater_temp = 72.0 + random.uniform(-0.5, 0.5)
                self.heater_sensor.publish_temperature(heater_temp)
                
                time.sleep(2)
                
        except KeyboardInterrupt:
            print("\n\nTest interrupted by user")
    
    def scenario_heating_cycle(self):
        """Simulate heater turning on and warming the room"""
        print("="*60)
        print("HEATING CYCLE SCENARIO")
        print("="*60)
        print("Heater room temperature increases\n")
        
        try:
            # Initial temperatures
            self.base_sensor.current_temp = 68.0
            self.heater_sensor.current_temp = 68.0
            
            print("Phase 1: Initial state (both rooms cool)")
            for i in range(5):
                self.base_sensor.publish_temperature(68.0 + random.uniform(-0.5, 0.5))
                self.heater_sensor.publish_temperature(68.0 + random.uniform(-0.5, 0.5))
                time.sleep(2)
            
            print("\nPhase 2: Heater turns on")
            # Heater room gradually warms up
            for i in range(15):
                # Base room stays stable
                base_temp = 68.0 + random.uniform(-0.5, 0.5)
                self.base_sensor.publish_temperature(base_temp)
                
                # Heater room gradually increases
                heater_temp = 68.0 + (i * 0.4) + random.uniform(-0.3, 0.3)
                self.heater_sensor.publish_temperature(heater_temp)
                
                time.sleep(2)
            
            print("\nPhase 3: Heater room stabilizes at higher temperature")
            for i in range(5):
                self.base_sensor.publish_temperature(68.0 + random.uniform(-0.5, 0.5))
                self.heater_sensor.publish_temperature(74.0 + random.uniform(-0.5, 0.5))
                time.sleep(2)
                
        except KeyboardInterrupt:
            print("\n\nTest interrupted by user")
    
    def scenario_temperature_drop(self):
        """Simulate outdoor temperature affecting base room"""
        print("="*60)
        print("TEMPERATURE DROP SCENARIO")
        print("="*60)
        print("Base room temperature drops (e.g., cold night)\n")
        
        try:
            self.base_sensor.current_temp = 72.0
            self.heater_sensor.current_temp = 73.0
            
            print("Phase 1: Normal temperatures")
            for i in range(5):
                self.base_sensor.publish_temperature(72.0 + random.uniform(-0.5, 0.5))
                self.heater_sensor.publish_temperature(73.0 + random.uniform(-0.5, 0.5))
                time.sleep(2)
            
            print("\nPhase 2: Base room cools down")
            for i in range(15):
                # Base room gradually decreases
                base_temp = 72.0 - (i * 0.3) + random.uniform(-0.3, 0.3)
                self.base_sensor.publish_temperature(base_temp)
                
                # Heater room stays warmer
                heater_temp = 73.0 + random.uniform(-0.5, 0.5)
                self.heater_sensor.publish_temperature(heater_temp)
                
                time.sleep(2)
            
            print("\nPhase 3: Base room stabilizes at lower temperature")
            for i in range(5):
                self.base_sensor.publish_temperature(67.5 + random.uniform(-0.5, 0.5))
                self.heater_sensor.publish_temperature(73.0 + random.uniform(-0.5, 0.5))
                time.sleep(2)
                
        except KeyboardInterrupt:
            print("\n\nTest interrupted by user")
    
    def scenario_extreme_readings(self):
        """Test edge cases and extreme values"""
        print("="*60)
        print("EXTREME READINGS SCENARIO")
        print("="*60)
        print("Testing various temperature ranges\n")
        
        test_cases = [
            (32.0, 33.0, "Freezing temperatures"),
            (55.0, 56.0, "Cold temperatures"),
            (70.0, 72.0, "Comfortable range"),
            (85.0, 88.0, "Hot temperatures"),
            (95.0, 98.0, "Very hot temperatures"),
        ]
        
        try:
            for base_temp, heater_temp, description in test_cases:
                print(f"\n{description}:")
                for i in range(3):
                    self.base_sensor.publish_temperature(base_temp + random.uniform(-1.0, 1.0))
                    self.heater_sensor.publish_temperature(heater_temp + random.uniform(-1.0, 1.0))
                    time.sleep(2)
                    
        except KeyboardInterrupt:
            print("\n\nTest interrupted by user")
    
    def scenario_continuous_random(self, interval=2.0):
        """Continuous random temperature readings"""
        print("="*60)
        print("CONTINUOUS RANDOM SCENARIO")
        print("="*60)
        print(f"Publishing random readings every {interval}s")
        print("Press Ctrl+C to stop\n")
        
        try:
            self.base_sensor.current_temp = 70.0
            self.heater_sensor.current_temp = 72.0
            
            while True:
                # Random walk - temperature drifts gradually
                self.base_sensor.current_temp += random.uniform(-0.5, 0.5)
                self.heater_sensor.current_temp += random.uniform(-0.3, 0.3)
                
                # Keep within reasonable bounds
                self.base_sensor.current_temp = max(60, min(85, self.base_sensor.current_temp))
                self.heater_sensor.current_temp = max(65, min(90, self.heater_sensor.current_temp))
                
                self.base_sensor.publish_temperature(self.base_sensor.current_temp)
                self.heater_sensor.publish_temperature(self.heater_sensor.current_temp)
                
                time.sleep(interval)
                
        except KeyboardInterrupt:
            print("\n\nStopping continuous simulation")
    
    def cleanup(self):
        """Clean up both sensors"""
        print("\nDisconnecting sensors...")
        self.base_sensor.cleanup()
        self.heater_sensor.cleanup()
        print("✓ Cleanup complete")


def main():
    parser = argparse.ArgumentParser(
        description='Mock Temperature Sensors for Testing Backend'
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
    
    # Scenario selection
    parser.add_argument('--scenario', 
                       choices=['normal', 'heating', 'cooling', 'extreme', 'continuous'],
                       default='normal',
                       help='Test scenario to run (default: normal)')
    parser.add_argument('--interval', type=float, default=2.0,
                       help='Interval for continuous mode (default: 2.0)')
    
    args = parser.parse_args()
    
    try:
        # Initialize dual room simulator
        simulator = DualRoomSimulator(
            broker_host=args.broker_host,
            broker_port=args.broker_port,
            username=args.username,
            password=args.password
        )
        
        # Run selected scenario
        if args.scenario == 'normal':
            simulator.scenario_normal_operation()
        elif args.scenario == 'heating':
            simulator.scenario_heating_cycle()
        elif args.scenario == 'cooling':
            simulator.scenario_temperature_drop()
        elif args.scenario == 'extreme':
            simulator.scenario_extreme_readings()
        elif args.scenario == 'continuous':
            simulator.scenario_continuous_random(interval=args.interval)
        
        # Cleanup
        simulator.cleanup()
        
        print("\n✓ Test completed successfully")
        
    except Exception as e:
        print(f"\n✗ Error: {e}")
        return 1
    
    return 0


if __name__ == '__main__':
    exit(main())
