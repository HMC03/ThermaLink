from ultralytics import YOLO
from picamera2 import Picamera2, Preview
import cv2
import time
import os

# Initialize the camera
picam2 = Picamera2()
config = picam2.create_preview_configuration(main={"format": "RGB888", "size": (640, 480)})
picam2.configure(config)
picam2.start()

# Load the YOLOv8 model
model_path = os.path.join(os.path.dirname(__file__), "../models/yolov8n.pt")
model = YOLO(model_path)

try:
    while True:
        frame = picam2.capture_array()

        # Run detection
        results = model.predict(frame, classes=[0], verbose=False)  # class 0 = person

        # Draw results
        annotated_frame = results[0].plot()

        cv2.imshow("Person Detection", annotated_frame)

        # Stop with 'q'
        if cv2.waitKey(1) & 0xFF == ord('q'):
            break

finally:
    cv2.destroyAllWindows()
    picam2.close()
