# rpi-person-temp-monitor

The raspberry pi module running a camera with computer vision for person detection and a temperature sensor to publish over mqtt network for other modules in the system to use


## Hardware
* Raspberry pi 5
* Arducam 5MP OV5647 w/ Motorized IR Cut Filter + MakerFocus case + 2 IR LED modules  
  Amazon listing used: https://www.amazon.com/dp/B07BK1QZ2L
* DHT-11 temperature sensor


## Strucure
```bash
- media/        # Saved images and documentation
- models/       # yolo model
- src/          # Source code
- tests/        # Test scripts for individual component validation
- setup_venv.sh # Creates virtual environment
```

## Setup
1. Camera stack
```bash
sudo apt update
sudo apt install -y rpicam-apps python3-libcamera python3-picamera2 libcamera-tools libcamera-v4l2
```
2. Virtual Environment
```bash
chmod +x setup_venv.sh
./setup_venv.sh
source .venv/bin/activate
```


