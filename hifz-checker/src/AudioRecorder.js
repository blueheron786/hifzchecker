import React, { useState } from "react";

function AudioRecorder() {
  const [isRecording, setIsRecording] = useState(false);
  const [audioUrl, setAudioUrl] = useState(null);
  const [audioBlob, setAudioBlob] = useState(null);
  const [mediaRecorder, setMediaRecorder] = useState(null);

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
      });
  };

  const stopRecording = () => {
    if (mediaRecorder) {
      mediaRecorder.stop();
      setIsRecording(false);
    }
  };

    const uploadAudio = () => {
      const formData = new FormData();
      formData.append("file", audioBlob, "audio.wav");

      fetch("http://localhost:8080/web/upload", {
        method: "POST",
        body: formData,  // Don't manually set Content-Type header
      })
        .then(response => response.json())
        .then(data => {
          console.log("Upload successful:", data);
        })
        .catch(error => {
          console.error("Error uploading file:", error);
        });
    };

  return (
    <div>
      <h1>Record Audio</h1>
      <button onClick={startRecording} disabled={isRecording}>Start Recording</button>
      <button onClick={stopRecording} disabled={!isRecording}>Stop Recording</button>

      {audioUrl && (
        <div>
          <h2>Preview</h2>
          <audio controls src={audioUrl}></audio>
          <button onClick={uploadAudio}>Upload Audio</button>
        </div>
      )}
    </div>
  );
}

export default AudioRecorder;
