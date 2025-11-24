import os
import ssl
import time
import threading
from dotenv import load_dotenv

# MQTT
import paho.mqtt.client as mqtt

# Camera & YOLO
from ultralytics import YOLO
from picamera2 import Picamera2
import cv2

# DHT11
import adafruit_dht
import board

load_dotenv()

# ------------------------------
# MQTT CONFIG
# ------------------------------
BROKER   = os.getenv("MQTT_HOST")
PORT     = int(os.getenv("MQTT_PORT"))
USERNAME = os.getenv("MQTT_USER")
PASSWORD = os.getenv("MQTT_PASS")

TOPIC_PERSON = "roomA/person/status"
TOPIC_TEMP   = "roomA/temperature"
TOPIC_HUMID  = "roomA/humidity"

# ------------------------------
# MQTT SETUP
# ------------------------------
def on_connect(client, userdata, flags, rc, properties=None):
    print("Connected to MQTT:", rc)

client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2)
client.username_pw_set(USERNAME, PASSWORD)
client.tls_set(cert_reqs=ssl.CERT_REQUIRED)
client.on_connect = on_connect
client.connect(BROKER, PORT)
client.loop_start()


# ------------------------------
# PERSON DETECTION THREAD
# ------------------------------
def person_detection_task():
    print("Starting YOLO + Camera...")

    picam2 = Picamera2()
    config = picam2.create_preview_configuration(
        main={"format": "RGB888", "size": (640, 480)}
    )
    picam2.configure(config)
    picam2.start()

    model_path = os.path.join(os.path.dirname(__file__), "../models/yolov8n.pt")
    model = YOLO(model_path)

    try:
        while True:
            frame = picam2.capture_array()

            # Run YOLO (non-blocking in this thread)
            results = model.predict(frame, classes=[0], verbose=False)
            num_people = len(results[0].boxes)

            status = "occupied" if num_people > 0 else "empty"
            client.publish(TOPIC_PERSON, status)

            # For debugging
            print(f"[YOLO] People detected: {num_people} -> {status}")

            # Optional: show frame
            annotated = results[0].plot()
            cv2.imshow("YOLO Person Detection", annotated)
            if cv2.waitKey(1) & 0xFF == ord("q"):
                break

            time.sleep(2)

    except Exception as e:
        print("YOLO thread error:", e)

    finally:
        cv2.destroyAllWindows()
        picam2.close()


# ------------------------------
# TEMPERATURE THREAD
# ------------------------------
def temperature_task():
    print("Starting DHT sensor reading...")

    dht = adafruit_dht.DHT11(board.D4)

    while True:
        try:
            temp_c = dht.temperature
            temp_f = temp_c * 9/5 + 32
            humidity = dht.humidity
            
            # MQTT publishing
            if temp_f is not None:
                client.publish(TOPIC_TEMP, f"{temp_f:.1f}")
            if humidity is not None:
                client.publish(TOPIC_HUMID, f"{humidity:.1f}")

            print(f"[DHT] Temp: {temp_f:.1f}F | Humidity: {humidity:.1f}%")

        except Exception as e:
            print("[DHT] Read error:", e)

        time.sleep(2)


# ------------------------------
# MAIN
# ------------------------------
if __name__ == "__main__":
    print("Starting all threads...")

    t1 = threading.Thread(target=person_detection_task, daemon=True)
    t2 = threading.Thread(target=temperature_task, daemon=True)

    t1.start()
    t2.start()

    # Keep main thread alive
    while True:
        time.sleep(1)
