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

    public void deleteFile(String fileUrl) {
        try {
            String bucketName = StorageClient.getInstance().bucket().getName();
            
            String baseUrl = String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/", bucketName);
            if (!fileUrl.startsWith(baseUrl)) {
                throw new IllegalArgumentException("Invalid file URL for this storage bucket.");
            }
            
            String encodedPath = fileUrl.substring(baseUrl.length(), fileUrl.indexOf("?alt=media"));
            String filePath = java.net.URLDecoder.decode(encodedPath, StandardCharsets.UTF_8.name());
            
            boolean deleted = StorageClient.getInstance().bucket().get(filePath).delete();
            
            if (!deleted) {
                throw new RuntimeException("Failed to delete file from Firebase Storage: " + filePath);
            }
    
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from Firebase Storage", e);
        }
    }
    
}
