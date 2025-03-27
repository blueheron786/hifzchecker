package com.hifzchecker.listener.transcription;

import org.springframework.stereotype.Service;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class HuggingFaceTranscriber {

    public String transcribe(String audioFilePath) throws IOException {
        var hfApiKey = loadApiKey();
        File audioFile = new File(audioFilePath);
        byte[] audioBytes = Files.readAllBytes(audioFile.toPath());

        String apiUrl = "https://api-inference.huggingface.co/models/openai/whisper-large";

        HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + hfApiKey);
        connection.setRequestProperty("Content-Type", "audio/wav");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(audioBytes);
            os.flush();
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("Hugging Face API error: " + responseCode);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        return response.toString();
    }

    private static String loadApiKey() {
        try {
            return new String(Files.readAllBytes(Paths.get("huggingface_api_key.txt"))).trim();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Hugging Face API key from huggingface_api_key.txt", e);
        }
    }
}
