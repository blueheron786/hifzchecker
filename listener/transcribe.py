import json
import sys
import whisper

# Load the Whisper model.
# Tiny, base, small, medium, and large
# LARGE: requires more GPU RAM than I have
# SMALL: gives actually pretty decent results
# MEDIUM: is EPIC and pretty fast tbh
model = whisper.load_model("medium")

# Transcribe the audio file passed via command-line argument
audio_file = sys.argv[1]
result = model.transcribe(audio_file, language='ar') # JSON object

# Output the transcription text to stdout as JSON
print(json.dumps(result))