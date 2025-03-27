package com.hifzchecker.listener.transcription;

import org.springframework.stereotype.Service;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class HuggingFaceTranscriber {

    private final String HUGGING_FACE_API_ENDPOINT =
            "https://router.huggingface.co/hf-inference/models/tarteel-ai/whisper-base-ar-quran";

    private static final int REQUIRED_SAMPLE_RATE = 16000; // Whisper Tiny requires 16kHz sample rate.

    public String transcribe(String audioFilePath) throws IOException {
        var hfApiKey = loadApiKey();

        // Resample audio if necessary
        File audioFile = new File(audioFilePath);
        File processedAudioFile = ensureCorrectSampleRate(audioFile);

        byte[] audioBytes = Files.readAllBytes(processedAudioFile.toPath());

        // Prepare the API URL and connection
        String apiUrl = HUGGING_FACE_API_ENDPOINT;

        HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + hfApiKey);
        connection.setRequestProperty("Content-Type", "application/octet-stream");
        connection.setDoOutput(true); // Enable output stream

        try (OutputStream os = connection.getOutputStream()) {
            os.write(audioBytes);  // Send raw bytes of the audio file
            os.flush();
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == 504) {
            throw new IOException("Hugging Face API call timed out!");
        } else if (responseCode != 200) {
            throw new IOException("Hugging Face API error: " + responseCode);
        }

        // Read the response
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        return response.toString();
    }

    private File ensureCorrectSampleRate(File audioFile) throws IOException {
        // Get audio info using ffmpeg
        ProcessBuilder processBuilder = new ProcessBuilder("ffmpeg", "-i", audioFile.getAbsolutePath());
        Process process = processBuilder.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        // Check if the sample rate is correct
        String ffmpegOutput = output.toString();
        String pattern = "(?<=, )([0-9]+) Hz"; // Regex to find the sample rate
        Matcher matcher = Pattern.compile(pattern).matcher(ffmpegOutput);

        int currentSampleRate = REQUIRED_SAMPLE_RATE; // Default to required sample rate
        if (matcher.find()) {
            currentSampleRate = Integer.parseInt(matcher.group(1));
        }

        // If the sample rate is already correct, return the original file
        if (currentSampleRate == REQUIRED_SAMPLE_RATE) {
            return audioFile;
        }

        // Otherwise, resample the audio
        return resampleAudio(audioFile);
    }


    private static File resampleAudio(File audioFile) throws IOException {
        // Resample audio to 16kHz using ffmpeg
        String outputFileName = audioFile.getName().replace(".wav", "_16kHz.wav");
        File resampledFile = new File(audioFile.getParent(), outputFileName);

        ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-i", audioFile.getAbsolutePath(), "-ar", String.valueOf(REQUIRED_SAMPLE_RATE), resampledFile.getAbsolutePath());
        Process process = pb.start();
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            throw new IOException("Error while resampling audio", e);
        }

        return resampledFile;
    }

    private static String loadApiKey() {
        try {
            return new String(Files.readAllBytes(Paths.get("token.txt"))).trim();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load HuggingFace token from token.txt", e);
        }
    }
}
