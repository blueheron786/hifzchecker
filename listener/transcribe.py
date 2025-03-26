import whisper
import sys

# Load the Whisper model
model = whisper.load_model("base")  # You can use different models like "small", "medium", etc.

# Transcribe the audio file passed via command-line argument
audio_file = sys.argv[1]
result = model.transcribe(audio_file)

# Output the transcription text to stdout
print(result["text"])
