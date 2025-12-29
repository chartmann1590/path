#!/bin/bash
# Shell script to create promo video on Linux/Mac

echo "Creating Path Bible Study App Promo Video..."
echo ""

# Check if Python is installed
if ! command -v python3 &> /dev/null; then
    echo "Error: Python 3 is not installed"
    echo "Please install Python 3.7+ and try again"
    exit 1
fi

# Install dependencies if needed
echo "Installing dependencies..."
pip3 install -q moviepy gtts Pillow

# Run the video creation script
echo ""
echo "Starting video creation..."
python3 ../create_promo_video.py

echo ""
echo "Done!"

