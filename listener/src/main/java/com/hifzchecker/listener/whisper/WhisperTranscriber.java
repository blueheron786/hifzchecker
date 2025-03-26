package com.hifzchecker.listener.whisper;

import java.io.*;

public class WhisperTranscriber {
    public static String transcribe(String audioFilePath) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "/bin/bash", "-c", "source ~/whisperenv/bin/activate && python3 transcribe.py " + audioFilePath
        );

        processBuilder.redirectErrorStream(true);  // Redirect stderr to stdout
        Process process = processBuilder.start();

        // Capture output
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Whisper transcription failed: " + output);
        }

        return output.toString().trim();  // Return the transcription
    }
}
