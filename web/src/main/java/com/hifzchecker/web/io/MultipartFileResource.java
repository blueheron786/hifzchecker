package com.hifzchecker.web.io;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class MultipartFileResource extends ByteArrayResource {
    private final String filename;

    public MultipartFileResource(MultipartFile file) throws IOException {
        super(file.getBytes());  // Store file as bytes (allows multiple reads)
        this.filename = file.getOriginalFilename();
    }

    @Override
    public String getFilename() {
        return filename;  // Ensure a valid filename is provided
    }
}

