import time
import adafruit_dht
import board

# DHT11 connected to GPIO4
dht = adafruit_dht.DHT11(board.D4)

while True:
    try:
        temp_c = dht.temperature
        temp_f = temp_c * 9 / 5 + 32
        humidity = dht.humidity
        print(f"Temp: {temp_c:.1f}°C / {temp_f:.1f}°F | Humidity: {humidity:.1f}%")
    except Exception as e:
        # Sensor errors are normal, retry
        print("Read error:", e)

    time.sleep(2)
