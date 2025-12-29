"""
Script to create a promo video for Path Bible Study App
Uses screenshots, adds voiceover, and background music
"""

import os
import sys
from pathlib import Path
from moviepy.editor import (
    ImageClip, 
    AudioFileClip, 
    CompositeAudioClip,
    concatenate_videoclips
)
from gtts import gTTS
import tempfile
import shutil

# Configuration
SCREENSHOTS_DIR = Path("screenshots")
PROMO_DIR = Path("promo")
PROMO_DIR.mkdir(exist_ok=True)

# Screenshot order and descriptions for voiceover
SCREENSHOT_SEQUENCE = [
    ("home.png", "Welcome to Path, your daily companion for Bible study. Start each day with a verse of the day and your personalized study plan."),
    ("reader.png", "Read through passages with a clean, distraction-free interface. Take your time to reflect on God's word."),
    ("progress.png", "Track your reading progress and build consistency with daily streaks. See how far you've come in your journey."),
    ("search.png", "Search through the entire Bible to find verses and passages that speak to you."),
    ("favorites.png", "Save your favorite verses and access them anytime for encouragement and reflection."),
    ("ai_summary.png", "Get deeper insights with optional AI-powered explanations, powered by your own self-hosted server for complete privacy."),
    ("settings.png", "Customize your experience with your preferred translation, reading pace, and study reminders."),
]

# Final message
FINAL_MESSAGE = "Path - Building consistent Bible study habits, one day at a time. Download now and start your journey."

def create_voiceover(text, output_path, lang='en', slow=False):
    """Generate voiceover using gTTS"""
    print(f"Generating voiceover: {text[:50]}...")
    tts = gTTS(text=text, lang=lang, slow=slow)
    tts.save(output_path)
    return output_path

def create_promo_video():
    """Main function to create the promo video"""
    print("Creating promo video for Path Bible Study App...")
    
    # Create temporary directory for audio files
    temp_dir = tempfile.mkdtemp()
    
    # Step 1: Create video clips from screenshots
    print("\n1. Processing screenshots...")
    video_clips = []
    audio_clips = []
    
    for i, (screenshot, narration) in enumerate(SCREENSHOT_SEQUENCE):
        screenshot_path = SCREENSHOTS_DIR / screenshot
        if not screenshot_path.exists():
            print(f"Warning: {screenshot_path} not found, skipping...")
            continue
        
        print(f"  Processing {screenshot}...")
        
        # Create image clip (5 seconds per screenshot)
        img_clip = ImageClip(str(screenshot_path))
        img_clip = img_clip.set_duration(5)
        # Resize to 1080p height, maintain aspect ratio
        # Calculate width to maintain aspect ratio (assuming 9:16 for mobile screenshots)
        img_clip = img_clip.resize(height=1080)
        
        # Generate voiceover
        audio_path = os.path.join(temp_dir, f"voiceover_{i}.mp3")
        create_voiceover(narration, audio_path)
        audio_clip = AudioFileClip(audio_path)
        
        # Adjust image duration to match audio (with padding)
        audio_duration = audio_clip.duration
        img_clip = img_clip.set_duration(max(5, audio_duration + 1))
        
        # Set audio to image clip
        img_clip = img_clip.set_audio(audio_clip)
        video_clips.append(img_clip)
    
    # Step 2: Add final message
    print("\n2. Adding final message...")
    final_audio_path = os.path.join(temp_dir, "final_message.mp3")
    create_voiceover(FINAL_MESSAGE, final_audio_path)
    final_audio = AudioFileClip(final_audio_path)
    
    # Create final text/image clip
    final_img = video_clips[-1] if video_clips else ImageClip(str(SCREENSHOTS_DIR / "home.png"))
    final_clip = final_img.set_duration(max(5, final_audio.duration + 1))
    final_clip = final_clip.set_audio(final_audio)
    video_clips.append(final_clip)
    
    # Step 3: Concatenate all clips
    print("\n3. Combining video clips...")
    final_video = concatenate_videoclips(video_clips, method="compose")
    
    # Step 4: Add background music (if available)
    print("\n4. Adding background music...")
    music_path = PROMO_DIR / "background_music.mp3"
    
    if music_path.exists():
        print(f"  Found background music: {music_path}")
        music = AudioFileClip(str(music_path))
        # Loop music to match video duration
        music_duration = final_video.duration
        if music.duration < music_duration:
            music = music.loop(duration=music_duration)
        else:
            music = music.subclip(0, music_duration)
        
        # Lower volume (20% of original) so it doesn't overpower voiceover
        music = music.volumex(0.2)
        
        # Composite audio - voiceover on top, music in background
        final_audio_composite = CompositeAudioClip([final_video.audio, music])
        final_video = final_video.set_audio(final_audio_composite)
        print("  Background music added successfully!")
    else:
        print("  No background music found. Video will use voiceover only.")
        print(f"  To add music, place 'background_music.mp3' in the {PROMO_DIR} folder.")
        print("  Recommended: Download royalty-free music from Pixabay or Free Music Archive")
    
    # Step 5: Export video
    output_path = PROMO_DIR / "path_promo_video.mp4"
    print(f"\n5. Exporting video to {output_path}...")
    print("  This may take a few minutes...")
    
    final_video.write_videofile(
        str(output_path),
        fps=24,
        codec='libx264',
        audio_codec='aac',
        bitrate='5000k',
        preset='medium'
    )
    
    # Cleanup
    print("\n6. Cleaning up temporary files...")
    final_video.close()
    for clip in video_clips:
        clip.close()
    
    # Clean up temp directory
    try:
        shutil.rmtree(temp_dir)
    except:
        pass
    
    print(f"\n[SUCCESS] Promo video created successfully!")
    print(f"   Location: {output_path}")
    print(f"   File size: {os.path.getsize(output_path) / (1024*1024):.2f} MB")
    print(f"\nVideo specs:")
    print(f"   Resolution: 1080p (height)")
    print(f"   Format: MP4 (H.264)")
    print(f"   Duration: {final_video.duration:.1f} seconds")
    print(f"   Ready for YouTube upload")

if __name__ == "__main__":
    try:
        create_promo_video()
    except Exception as e:
        print(f"\n[ERROR] Error creating video: {e}")
        import traceback
        traceback.print_exc()

