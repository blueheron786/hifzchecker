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
        const formData = new FormData();
        formData.append("file", audioBlob, "audio.wav");

        // Get the div for output (where both transcription and processing time will be displayed)
        const outputDiv = document.getElementById('transcription-output');
        if (outputDiv) {
            outputDiv.textContent = "Processing...";  // Show "Processing..."
        }

        const startTime = Date.now();  // Record start time before the request

        fetch("http://localhost:8080/web/upload", {
            method: "POST",
            body: formData,  // Don't manually set Content-Type header
        })
        .then(response => response.text())  // Expect plain text response (transcription)
        .then(transcription => {
            const endTime = Date.now();  // Record end time after the response is received
            const generationTime = ((endTime - startTime) / 1000).toFixed(1); // Time in seconds (1 decimal place)

            // Update the output div with the transcription and processing time
            if (outputDiv) {
                outputDiv.innerHTML = `${transcription}<br><br>Processed in ${generationTime} seconds`;  // Display both transcription and time
            } else {
                console.error("Output div not found.");
            }
        })
        .catch(error => {
            console.error("Error uploading file:", error);

            // Handle the error in case of failure
            if (outputDiv) {
                outputDiv.textContent = "Error processing file.";  // Show error message in the output div
            }
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
          <button onClick={uploadAudio}>Transcribe Audio</button>
        </div>
      )}
    </div>
  );
}

export default AudioRecorder;
