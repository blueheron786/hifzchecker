package com.hifzchecker.listener.controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/listener")
public class AudioListenerController {

    @PostMapping("/transcribe")
    public String transcribeAudio(@RequestParam("file") MultipartFile file) throws IOException, InterruptedException {
        // Save the uploaded file to disk
        File audioFile = new File("uploaded-audio-" + UUID.randomUUID().toString() + ".wav");
        file.transferTo(audioFile);

        // Call Python script to process the file
        String transcription = runWhisperScript(audioFile.getAbsolutePath());

        // Return the transcription result
        return transcription;
    }

    private String runWhisperScript(String audioFilePath) throws IOException, InterruptedException {
        // Execute the Python script using ProcessBuilder
        ProcessBuilder processBuilder = new ProcessBuilder("python3", "transcribe.py", audioFilePath);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        process.waitFor();

        // Capture the output (transcription text) from the Python script
        StringBuilder output = new StringBuilder();
        try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
        }

        return output.toString();
    }
}
