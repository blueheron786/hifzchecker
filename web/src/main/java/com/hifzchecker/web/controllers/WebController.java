package com.hifzchecker.web.controllers;

import com.hifzchecker.web.io.MultipartFileResource;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ui.Model;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.io.IOException;

@Controller
@RequestMapping("/web")
public class WebController {

    private static final String LISTENER_API_URL = "http://localhost:8081/listener/transcribe";  // Listener API URL

    @GetMapping("/upload")
    public String showUploadPage(Model model) {
        // This will return the Thymeleaf upload page
        return "upload"; // Make sure this matches your template name (upload.html)
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadAudio(@RequestParam("file") MultipartFile file) {
        try {
            // Send the file to the Listener API (which runs Whisper)
            String transcription = sendFileToListener(file);
            return ResponseEntity.ok(transcription);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error processing audio file");
        }
    }

    private String sendFileToListener(MultipartFile file) throws IOException {
        // Send the file to the Listener API (which runs Whisper)
        RestTemplate restTemplate = new RestTemplate();

        // Prepare headers and file as a multipart request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Prepare the multipart file in the request body
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new MultipartFileResource(file));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Make the API call to the Listener
        ResponseEntity<String> response = restTemplate.exchange(LISTENER_API_URL, HttpMethod.POST, requestEntity, String.class);
        return response.getBody();
    }
}
