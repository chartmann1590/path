@echo off
REM Batch script to create promo video on Windows
echo Creating Path Bible Study App Promo Video...
echo.

REM Check if Python is installed
python --version >nul 2>&1
if errorlevel 1 (
    echo Error: Python is not installed or not in PATH
    echo Please install Python 3.7+ and try again
    pause
    exit /b 1
)

REM Install dependencies if needed
echo Installing dependencies...
pip install -q moviepy gtts Pillow

REM Run the video creation script
echo.
echo Starting video creation...
python ..\create_promo_video.py

echo.
echo Done!
pause

