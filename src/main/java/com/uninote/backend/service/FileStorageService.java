package com.uninote.backend.service;

import com.google.firebase.cloud.StorageClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class FileStorageService {

    public String uploadFile(MultipartFile file, String fileName, Long id, String pathPrefix) {
        try {
            // Build path inside Firebase Storage
            String fullPath = pathPrefix + "/" + id + "/" + fileName;

            // Upload the file to Firebase Storage
            InputStream inputStream = file.getInputStream();
            StorageClient.getInstance().bucket().create(fullPath, inputStream, file.getContentType());

            // Generate download URL
            String bucketName = StorageClient.getInstance().bucket().getName();
            String encodedPath = URLEncoder.encode(fullPath, StandardCharsets.UTF_8.toString());

            return String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media", bucketName, encodedPath);

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to Firebase Storage", e);
        }
    }
}
