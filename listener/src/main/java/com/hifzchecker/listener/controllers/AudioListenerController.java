package com.hifzchecker.listener.controllers;

import com.hifzchecker.listener.transcription.HuggingFaceTranscriber;
import com.hifzchecker.listener.transcription.LocalWhisperTranscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/listener")
public class AudioListenerController {

    @Autowired
    private LocalWhisperTranscriber localWhisperTranscriber;

    @Autowired
    private HuggingFaceTranscriber huggingFaceTranscriber;

    @Value("${transcription.mode}")  // Read from application.yml
    private String mode;

    public AudioListenerController(LocalWhisperTranscriber localWhisperTranscriber, HuggingFaceTranscriber huggingFaceTranscriber) {
        this.localWhisperTranscriber = localWhisperTranscriber;
        this.huggingFaceTranscriber = huggingFaceTranscriber;
    }

    @PostMapping("/transcribe")
    public String transcribeAudio(@RequestParam("file") MultipartFile file) throws IOException, InterruptedException {
        File audioFile = File.createTempFile("uploaded-audio", ".wav");
        file.transferTo(audioFile);

        if ("huggingface".equalsIgnoreCase(mode)) {
            return huggingFaceTranscriber.transcribe(audioFile.getAbsolutePath());
        } else {
            return localWhisperTranscriber.transcribe(audioFile.getAbsolutePath());
        }
    }
}
