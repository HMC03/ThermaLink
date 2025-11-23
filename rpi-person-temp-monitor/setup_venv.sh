#!/bin/bash
# Create venv that can access system packages
python3 -m venv --system-site-packages .venv
source .venv/bin/activate

# Upgrade pip
pip install --upgrade pip

# Install Ultralytics and dependencies manually
pip install "torch>=2.9.1" "torchvision>=0.24.1" "pyyaml>=6.0"
pip install "polars>=0.20.0"
pip install "ultralytics-thop>=2.0.18"
pip install "ultralytics==8.3.230" --no-deps

echo "Setup complete. Activate venv with: source .venv/bin/activate"
