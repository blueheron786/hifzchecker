package com.hifzchecker.listener.controllers;

import com.hifzchecker.listener.whisper.WhisperTranscriber;
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
        return new WhisperTranscriber().transcribe(audioFilePath);
    }
}
