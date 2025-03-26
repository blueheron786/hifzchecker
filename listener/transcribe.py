import whisper
import sys

# Load the Whisper model.
# Tiny, base, small, medium, and large (requires more RAM than I have).
model = whisper.load_model("tiny")

# Transcribe the audio file passed via command-line argument
audio_file = sys.argv[1]
result = model.transcribe(audio_file)

# Output the transcription text to stdout
print(result["text"])
