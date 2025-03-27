import React, { useState } from "react";

function AudioRecorder() {
  const [isRecording, setIsRecording] = useState(false);
  const [audioUrl, setAudioUrl] = useState(null);
  const [audioBlob, setAudioBlob] = useState(null);
  const [mediaRecorder, setMediaRecorder] = useState(null);
  const [isProcessing, setIsProcessing] = useState(false); // Track processing state
  const [error, setError] = useState(null); // Error state

  const startRecording = () => {
    navigator.mediaDevices.getUserMedia({ audio: true })
      .then(stream => {
        const recorder = new MediaRecorder(stream);
        setMediaRecorder(recorder);

        const chunks = [];
        recorder.ondataavailable = (e) => {
          chunks.push(e.data);
        };

        recorder.onstop = () => {
          const audioBlob = new Blob(chunks, { type: 'audio/wav' });
          setAudioBlob(audioBlob);
          const audioUrl = URL.createObjectURL(audioBlob);
          setAudioUrl(audioUrl);
        };

        recorder.start();
        setIsRecording(true);
        setError(null);  // Reset error on new recording
      })
      .catch((err) => {
        console.error("Error accessing audio:", err);
        setError("Unable to access audio. Please check your device settings.");
      });
  };

  const stopRecording = () => {
    if (mediaRecorder) {
      mediaRecorder.stop();
      setIsRecording(false);
    }
  };

  const uploadAudio = () => {
    if (!audioBlob) {
      setError("No audio to upload.");
      return;
    }

    setIsProcessing(true);
    const formData = new FormData();
    formData.append("file", audioBlob, "audio.wav");

    // Reset error state before starting the upload
    setError(null);

    const startTime = Date.now();

    fetch("http://localhost:8080/web/upload", {
      method: "POST",
      body: formData,  // Don't manually set Content-Type header
    })
    .then(response => response.json())
    .then(transcription => {
      const endTime = Date.now();
      const generationTime = ((endTime - startTime) / 1000).toFixed(1);

      setIsProcessing(false);
      setAudioUrl(null); // Reset the audio URL after processing
      setAudioBlob(null); // Clear the blob
      setError(null); // Clear previous error state

      // Display transcription and processing time
      alert(`${transcription["text"]}\nProcessed in ${generationTime} seconds`);
    })
    .catch(error => {
      setIsProcessing(false);
      setError("Error processing file.");
      console.error("Error uploading file:", error);
    });
  };

  return (
    <div>
      <h1>Record Audio</h1>
      <button onClick={startRecording} disabled={isRecording || isProcessing}>
        Start Recording
      </button>
      <button onClick={stopRecording} disabled={!isRecording || isProcessing}>
        Stop Recording
      </button>

      {error && <div style={{ color: "red" }}>{error}</div>}

      <br />

      {audioUrl && (
        <div>
          <h2>Preview</h2>
          <audio controls src={audioUrl}></audio>
          <br />
          <button onClick={uploadAudio} disabled={isProcessing}>
            {isProcessing ? "Processing..." : "Transcribe Audio"}
          </button>
        </div>
      )}
    </div>
  );
}

export default AudioRecorder;
