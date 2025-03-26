package com.hifzchecker.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@SpringBootApplication
public class WebApplication {
	public static void main(String[] args) {
		SpringApplication.run(WebApplication.class, args);
	}
}

@RestController
@RequestMapping("/api")
class AudioController {

	@PostMapping("/upload")
	public ResponseEntity<String> uploadAudio(@RequestParam("file") MultipartFile file) {
		try {
			// Save the uploaded file temporarily
			Path tempFile = Files.createTempFile("quran_audio_", ".wav");
			Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

			// TODO: Integrate ASR processing here
			String transcript = "[Transcription will go here]";

			return ResponseEntity.ok(transcript);
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing file");
		}
	}
}