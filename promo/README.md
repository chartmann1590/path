# Promo Video Generator

This folder contains the generated promo video for the Path Bible Study App.

## Generated Files

- `path_promo_video.mp4` - The final promo video ready for YouTube upload (generated after running the script)

## Quick Start

### Windows
Double-click `create_video.bat` or run:
```cmd
create_video.bat
```

### Linux/Mac
```bash
chmod +x create_video.sh
./create_video.sh
```

### Manual Method
```bash
# Install dependencies
pip install -r ../requirements.txt

# Create the video
python ../create_promo_video.py
```

## Adding Background Music (Optional)

To add background music to your promo video:

1. Download a royalty-free music track (e.g., from [Pixabay](https://pixabay.com/music/) or [Free Music Archive](https://freemusicarchive.org/))
2. Save it as `background_music.mp3` in this folder
3. Re-run the script

The script will automatically:
- Lower the music volume to 20% so it doesn't overpower the voiceover
- Loop the music if needed to match the video duration
- Mix it with the voiceover audio

## Video Specifications

- **Resolution**: 1080p (height), width auto-adjusted
- **Format**: MP4 (H.264)
- **Frame Rate**: 24 fps
- **Audio**: AAC codec
- **Bitrate**: 5000k
- **Duration**: ~35-45 seconds (depends on narration length)

## YouTube Upload Tips

1. The video is optimized for YouTube's recommended settings
2. Add a compelling thumbnail
3. Write a description highlighting key features
4. Use relevant tags: #BibleStudy #AndroidApp #Devotional #ChristianApp

