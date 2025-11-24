import ssl
import time
import paho.mqtt.client as mqtt
import os 
from dotenv import load_dotenv

load_dotenv()

BROKER  = os.getenv("MQTT_HOST")
PORT    = int(os.getenv("MQTT_PORT"))
USERNAME = os.getenv("MQTT_USER")
PASSWORD = os.getenv("MQTT_PASS")
TOPIC   = "rpi/test"

def on_connect(client, userdata, flags, rc, properties=None):
    print("Connected:", rc)
    client.subscribe(TOPIC)

def on_message(client, userdata, msg):
    print(f"[RECEIVED] {msg.topic}: {msg.payload.decode()}")

client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2)

client.username_pw_set(USERNAME, PASSWORD)

# Enable TLS
client.tls_set(cert_reqs=ssl.CERT_REQUIRED)

client.on_connect = on_connect
client.on_message = on_message

client.connect(BROKER, PORT)
client.loop_start()

while True:
    msg = "Hello from Raspberry Pi"
    client.publish(TOPIC, msg)
    print(f"[SENT] {msg}")
    time.sleep(5)
