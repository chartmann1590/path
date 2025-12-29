"""
Helper script to download royalty-free background music for the promo video
"""

import os
import urllib.request
from pathlib import Path

# Royalty-free music options
# These are example URLs - you can replace with your own choice
MUSIC_OPTIONS = {
    "calm": {
        "name": "Calm Background Music",
        "url": "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
        "description": "Gentle, calm instrumental music"
    },
    "inspiring": {
        "name": "Inspiring Background Music", 
        "url": "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
        "description": "Uplifting instrumental music"
    }
}

def download_music(choice="calm"):
    """Download background music"""
    promo_dir = Path(__file__).parent
    output_path = promo_dir / "background_music.mp3"
    
    if choice not in MUSIC_OPTIONS:
        print(f"Invalid choice. Available options: {', '.join(MUSIC_OPTIONS.keys())}")
        return False
    
    music_info = MUSIC_OPTIONS[choice]
    print(f"Downloading: {music_info['name']}")
    print(f"Description: {music_info['description']}")
    print(f"URL: {music_info['url']}")
    
    try:
        urllib.request.urlretrieve(music_info['url'], output_path)
        print(f"\n✅ Music downloaded successfully!")
        print(f"   Location: {output_path}")
        return True
    except Exception as e:
        print(f"\n❌ Error downloading music: {e}")
        print("\nAlternative: Download music manually from:")
        print("  - Pixabay: https://pixabay.com/music/")
        print("  - Free Music Archive: https://freemusicarchive.org/")
        print("  - YouTube Audio Library: https://www.youtube.com/audiolibrary")
        print(f"\nSave the file as: {output_path}")
        return False

if __name__ == "__main__":
    print("Background Music Downloader for Path Promo Video")
    print("=" * 50)
    print("\nAvailable options:")
    for key, info in MUSIC_OPTIONS.items():
        print(f"  {key}: {info['name']} - {info['description']}")
    
    print("\nNote: The example URLs may not work.")
    print("For best results, download royalty-free music manually from:")
    print("  - Pixabay: https://pixabay.com/music/")
    print("  - Free Music Archive: https://freemusicarchive.org/")
    print("  - YouTube Audio Library: https://www.youtube.com/audiolibrary")
    print("\nRecommended search terms: 'calm', 'peaceful', 'meditation', 'ambient'")
    print(f"\nSave the downloaded file as: {Path(__file__).parent / 'background_music.mp3'}")

