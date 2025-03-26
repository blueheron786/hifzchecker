import React, { useState } from "react";

function AudioRecorder() {
  const [isRecording, setIsRecording] = useState(false);
  const [audioUrl, setAudioUrl] = useState(null);
  const [audioBlob, setAudioBlob] = useState(null);
  const [mediaRecorder, setMediaRecorder] = useState(null);

  const transcriptionDiv = document.getElementById('transcription-output');

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
        transcriptionDiv.textContent = "";
        const formData = new FormData();
        formData.append("file", audioBlob, "audio.wav");

        fetch("http://localhost:8080/web/upload", {
            method: "POST",
            body: formData,  // Don't manually set Content-Type header
        })
        .then(response => response.text())  // Expect plain text response
        .then(transcription => {
            // Find the div where the transcription will go

            if (transcriptionDiv) {
                // Update the div with the transcription
                transcriptionDiv.textContent = transcription;
            } else {
                console.error("Transcription output div not found.");
            }
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

        <div id="transcription-output"></div>

        <br />
      { audioUrl && (
        <div>
          <h2>Preview</h2>
          <audio controls src={audioUrl}></audio>
          <br />
          <button onClick={uploadAudio}>Upload Audio</button>
        </div>
      )}
    </div>
  );
}

export default AudioRecorder;
