#!/usr/bin/env python3
"""
Complete ThermaLink System Test Suite
Runs all mock tests in sequence to verify the complete system
"""

import os
import sys
import subprocess
import time
from pathlib import Path

try:
    from dotenv import load_dotenv
except ImportError:
    print("ERROR: python-dotenv not installed")
    print("Installing python-dotenv...")
    subprocess.run([sys.executable, "-m", "pip", "install", "python-dotenv"])
    from dotenv import load_dotenv


class TestSuite:
    def __init__(self):
        self.passed = 0
        self.failed = 0
        self.load_credentials()
    
    def load_credentials(self):
        """Load MQTT credentials from .env"""
        env_path = Path('.env')
        if not env_path.exists():
            print("ERROR: .env file not found!")
            print("Please create a .env file with your MQTT credentials")
            sys.exit(1)
        
        load_dotenv(env_path)
        
        self.broker_host = os.getenv('MQTT_BROKER_HOST')
        self.broker_port = os.getenv('MQTT_BROKER_PORT', '8883')
        self.username = os.getenv('MQTT_USERNAME')
        self.password = os.getenv('MQTT_PASSWORD')
        
        if not self.broker_host or not self.username or not self.password:
            print("ERROR: Missing required environment variables in .env")
            sys.exit(1)
    
    def print_header(self, title):
        """Print formatted header"""
        print("\n" + "="*70)
        print(f"  {title}")
        print("="*70)
    
    def print_section(self, title):
        """Print section title"""
        print(f"\n{'─'*70}")
        print(f"  {title}")
        print(f"{'─'*70}")
    
    def run_test(self, script, args, description):
        """Run a test script and return success/failure"""
        self.print_section(description)
        print(f"Running: {script} {' '.join(args)}")
        print()
        
        try:
            result = subprocess.run(
                [sys.executable, script] + args,
                timeout=60,  # 60 second timeout per test
                check=False
            )
            
            if result.returncode == 0:
                print(f"\n✓ {description} - PASSED")
                self.passed += 1
                return True
            else:
                print(f"\n✗ {description} - FAILED (exit code: {result.returncode})")
                self.failed += 1
                return False
                
        except subprocess.TimeoutExpired:
            print(f"\n✗ {description} - TIMEOUT")
            self.failed += 1
            return False
        except Exception as e:
            print(f"\n✗ {description} - ERROR: {e}")
            self.failed += 1
            return False
    
    def wait_between_tests(self, seconds=3):
        """Wait between tests"""
        print(f"\nWaiting {seconds} seconds before next test...")
        time.sleep(seconds)
    
    def run_all_tests(self):
        """Run complete test suite"""
        self.print_header("ThermaLink Complete System Test Suite")
        
        print(f"\n✓ Loaded credentials from .env")
        print(f"  Broker: {self.broker_host}:{self.broker_port}")
        print(f"  Username: {self.username}")
        print()
        print("🚀 Starting test suite...")
        print()
        print("Make sure your backend is running:")
        print("  cd iot-control-app/Mobile_Backend")
        print("  ./mvnw spring-boot:run")
        print()
        input("Press Enter when backend is ready...")
        
        # Test 1: Temperature Sensors
        self.run_test(
            'mock_temperature_test.py',
            [
                '--broker-host', self.broker_host,
                '--broker-port', self.broker_port,
                '--username', self.username,
                '--password', self.password,
                '--scenario', 'normal'
            ],
            "Test 1: Temperature Sensors (Normal Operation)"
        )
        self.wait_between_tests()
        
        # Test 2: Camera Detection
        self.run_test(
            'mock_camera_test.py',
            [
                '--broker-host', self.broker_host,
                '--broker-port', self.broker_port,
                '--username', self.username,
                '--password', self.password,
                '--scenario', 'realistic'
            ],
            "Test 2: Camera Person Detection (Realistic)"
        )
        self.wait_between_tests()
        
        # Test 3: Heater/Fan Status
        self.run_test(
            'mock_device_status_test.py',
            [
                '--broker-host', self.broker_host,
                '--broker-port', self.broker_port,
                '--username', self.username,
                '--password', self.password,
                '--room', 'roomA',
                '--scenario', 'manual'
            ],
            "Test 3: Heater/Fan Status (Manual Control)"
        )
        self.wait_between_tests()
        
        # Test 4: Heating Cycle
        self.run_test(
            'mock_device_status_test.py',
            [
                '--broker-host', self.broker_host,
                '--broker-port', self.broker_port,
                '--username', self.username,
                '--password', self.password,
                '--room', 'roomA',
                '--scenario', 'heating'
            ],
            "Test 4: Automatic Heating Cycle"
        )
        self.wait_between_tests()
        
        # Test 5: Combined Test (Temperature + Detection + Devices)
        self.print_section("Test 5: Combined System Test")
        print("This test requires manual observation of multiple terminals")
        print()
        print("In separate terminals, run:")
        print("  Terminal 1: python3 run_temperature_test.py continuous")
        print("  Terminal 2: python3 run_mock_test.py continuous")
        print("  Terminal 3: python3 run_device_test.py continuous")
        print()
        print("Let them run for 30 seconds, then stop with Ctrl+C")
        print()
        response = input("Did all systems work together? (y/n): ").lower()
        
        if response == 'y':
            print("✓ Combined System Test - PASSED")
            self.passed += 1
        else:
            print("✗ Combined System Test - FAILED")
            self.failed += 1
        
        # Print final results
        self.print_results()
    
    def run_quick_tests(self):
        """Run quick validation tests"""
        self.print_header("ThermaLink Quick Validation Suite")
        
        print(f"\n✓ Loaded credentials from .env")
        print(f"  Broker: {self.broker_host}:{self.broker_port}")
        print()
        print("🚀 Running quick validation...")
        print()
        
        # Quick temperature test
        self.run_test(
            'mock_temperature_test.py',
            [
                '--broker-host', self.broker_host,
                '--broker-port', self.broker_port,
                '--username', self.username,
                '--password', self.password,
                '--scenario', 'extreme'
            ],
            "Quick Test 1: Temperature Edge Cases"
        )
        self.wait_between_tests(2)
        
        # Quick detection test
        self.run_test(
            'mock_camera_test.py',
            [
                '--broker-host', self.broker_host,
                '--broker-port', self.broker_port,
                '--username', self.username,
                '--password', self.password,
                '--scenario', 'edge'
            ],
            "Quick Test 2: Detection Edge Cases"
        )
        self.wait_between_tests(2)
        
        # Quick device test
        self.run_test(
            'mock_device_status_test.py',
            [
                '--broker-host', self.broker_host,
                '--broker-port', self.broker_port,
                '--username', self.username,
                '--password', self.password,
                '--room', 'roomA',
                '--scenario', 'rapid'
            ],
            "Quick Test 3: Rapid Device Changes"
        )
        
        self.print_results()
    
    def print_results(self):
        """Print final test results"""
        self.print_header("Test Suite Results")
        
        total = self.passed + self.failed
        pass_rate = (self.passed / total * 100) if total > 0 else 0
        
        print()
        print(f"  Total Tests: {total}")
        print(f"  ✓ Passed:    {self.passed}")
        print(f"  ✗ Failed:    {self.failed}")
        print(f"  Pass Rate:   {pass_rate:.1f}%")
        print()
        
        if self.failed == 0:
            print("  🎉 ALL TESTS PASSED! System is working correctly.")
        else:
            print("  ⚠️  Some tests failed. Check backend logs for details.")
        
        print()
        print("Next steps:")
        print("  • Check backend logs: tail -f ../Mobile_Backend/logs/server-*.log")
        print("  • Query database: sqlite3 ../Mobile_Backend/iotdb.db")
        print("  • Test APIs: curl http://localhost:8080/api/temperature/current/all")
        print()


def main():
    if len(sys.argv) > 1 and sys.argv[1] == 'quick':
        print("Running quick validation suite...")
        suite = TestSuite()
        suite.run_quick_tests()
    else:
        print("Running complete test suite...")
        suite = TestSuite()
        suite.run_all_tests()


if __name__ == '__main__':
    main()
