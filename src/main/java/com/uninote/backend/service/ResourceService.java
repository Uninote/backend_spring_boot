package com.uninote.backend.service;

import com.uninote.backend.entity.*;
import com.uninote.backend.repository.FileResourceRepository;
import com.uninote.backend.repository.YouTubeResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.cloud.StorageClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.UUID;
import java.net.URLEncoder;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




@Service
public class ResourceService {
    
    private static final Logger logger = LoggerFactory.getLogger(ResourceService.class);

    @Autowired
    private FileResourceRepository fileResourceRepository;

    @Autowired
    private YouTubeResourceRepository youTubeResourceRepository;

    @Autowired
    private LangChainContentService langChainContentService;

    @Autowired
    private ContentExtractionService contentExtractionService;

    private static String baseUrl = "https://uninote-python-scripts-7d4abe41edb3.herokuapp.com";


    public FileResource createFileResource(MultipartFile file) {
        logger.info("=== STARTING FILE UPLOAD ===");
        String fileId = UUID.randomUUID().toString();
        String originalFilename = file != null ? file.getOriginalFilename() : "null";
        logger.info("File upload: ID={}, Name={}", fileId, originalFilename);
        
        try {
            // Validate file
            if (file == null) {
                logger.error("ERROR: File is null");
                throw new IllegalArgumentException("File cannot be null");
            }
            
            // Get file extension
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            // Align with frontend structure - use "notes" directory like frontend
            String storagePath = "notes/" + fileId + extension;
            logger.info("Storage path: {}", storagePath);
            
            // Get bucket directly from Firebase App
            logger.info("Getting Firebase bucket");
            Bucket bucket = null;
            
            try {
                // Get Firebase bucket the correct way - directly from StorageClient
                bucket = StorageClient.getInstance().bucket();
                String bucketName = bucket.getName();
                logger.info("Successfully got bucket: {}", bucketName);
                
                // Upload file directly using the Bucket object
                logger.info("Uploading file...");
                com.google.cloud.storage.Blob blob = bucket.create(
                    storagePath, 
                    file.getBytes(), 
                    file.getContentType() != null ? file.getContentType() : "application/octet-stream"
                );
                
                logger.info("File uploaded successfully. Name: {}, Size: {}", 
                    blob.getName(), blob.getSize());
                
                // Generate the download URL
                String downloadUrl = String.format(
                    "https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media", 
                    bucketName, 
                    storagePath.replace("/", "%2F")
                );
                logger.info("Download URL: {}", downloadUrl);
                
                // Save to database
                FileResource fr = new FileResource();
                fr.setFileUrl(downloadUrl);
                fr.setTitle(originalFilename);
                fr.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                
                FileResource savedResource = fileResourceRepository.save(fr);
                Resource updatedResource = contentExtractionService.extractContent(file, savedResource);

                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        langChainContentService.generateAllContentAsync(updatedResource.getId());
                    }
                });
                logger.info("=== FILE UPLOAD COMPLETE: ID={} ===", savedResource.getId());
                return savedResource;
                
            } catch (Exception e) {
                logger.error("Firebase upload error: {}", e.getMessage());
                // Try alternative method if primary method fails
                return createFileResourceAlternative(file, fileId, originalFilename, extension);
            }
            
        } catch (Exception e) {
            logger.error("ERROR: File upload failed - {}", e.getMessage());
            throw new RuntimeException("File upload failed: " + e.getMessage(), e);
        }
    }

    // Fallback method if the primary method fails
    private FileResource createFileResourceAlternative(MultipartFile file, String fileId, 
                                                      String originalFilename, String extension) {
        logger.info("=== TRYING ALTERNATIVE UPLOAD METHOD ===");
        
        try {
            String storagePath = "notes/" + fileId + extension;
            
            // Get the default bucket name from Firebase App
            String bucketName = FirebaseApp.getInstance().getOptions().getStorageBucket();
            if (bucketName == null || bucketName.isEmpty()) {
                throw new IllegalStateException("Firebase Storage bucket not configured");
            }
            logger.info("Using bucket name from Firebase App options: {}", bucketName);
            
            // Use Google Cloud Storage directly
            Storage storage = StorageOptions.getDefaultInstance().getService();
            BlobId blobId = BlobId.of(bucketName, storagePath);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();
            
            logger.info("Uploading with Google Cloud Storage API...");
            storage.create(blobInfo, file.getBytes());
            logger.info("File uploaded successfully with alternative method");
            
            String downloadUrl = String.format(
                "https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media", 
                bucketName, 
                storagePath.replace("/", "%2F")
            );
            
            FileResource fr = new FileResource();
            fr.setFileUrl(downloadUrl);
            fr.setTitle(originalFilename);
            fr.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            
            FileResource savedResource = fileResourceRepository.save(fr);
            logger.info("=== ALTERNATIVE UPLOAD COMPLETE: ID={} ===", savedResource.getId());
            return savedResource;
            
        } catch (Exception e) {
            logger.error("Alternative upload also failed: {}", e.getMessage());
            throw new RuntimeException("All file upload methods failed: " + e.getMessage(), e);
        }
    }

    public YouTubeResource createYouTubeResource(String youtubeUrl) {
        logger.info("=== STARTING YOUTUBE RESOURCE CREATION ===");
        logger.info("YouTube URL: {}", youtubeUrl);
        
        try {
            // Validate URL
            if (youtubeUrl == null || youtubeUrl.trim().isEmpty()) {
                logger.error("ERROR: YouTube URL is null or empty");
                throw new IllegalArgumentException("YouTube URL cannot be null or empty");
            }
            
            // Extract video ID
            String videoId = extractVideoId(youtubeUrl);
            logger.info("Video ID: {}", videoId);
            
            // Save to database
                YouTubeResource yt = new YouTubeResource();
                yt.setYoutubeUrl(youtubeUrl);
                yt.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                String fastApiUrl = baseUrl + "/api/transcript?url=" + videoId;
                String fullText = "";
                logger.info(fastApiUrl);

                RestTemplate restTemplate = new RestTemplate();
                try {
                ResponseEntity<String> response = restTemplate.getForEntity(fastApiUrl, String.class);
                logger.info("FastAPI Response Status: {}", response.getStatusCode());
                logger.info("FastAPI Response Body: {}", response.getBody());
                if (response.getStatusCode().is2xxSuccessful()) {
                    String transcriptJson = response.getBody();
                    logger.info("Fetched transcript from FastAPI: {}", transcriptJson);
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode rootNode = objectMapper.readTree(transcriptJson);
                    fullText = rootNode.path("full_text").asText();

                } else {
                    logger.error("Failed to fetch transcript. Status code: {}", response.getStatusCodeValue());
                    throw new RuntimeException("Failed to fetch transcript");
                }
                } catch(Exception e) {
                    logger.error("here"+ e.getMessage());
                }
                
            yt.setContent(fullText);  
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    langChainContentService.generateAllContentAsync(yt.getId());
                }
            });
            
            
            YouTubeResource savedResource = youTubeResourceRepository.save(yt);
            logger.info("=== YOUTUBE RESOURCE CREATION COMPLETE: ID={} ===", savedResource.getId());
            return savedResource;
        } catch (Exception e) {
            logger.error("ERROR: YouTube resource creation failed - {}", e.getMessage());
            throw new RuntimeException("YouTube resource creation failed: " + e.getMessage(), e);
        }
    }

    private String extractVideoId(String url) {
        if (url == null) {
            throw new IllegalArgumentException("YouTube URL cannot be null");
        }
        
        if (url.contains("v=")) {
            String[] splitByV = url.split("v=");
            if (splitByV.length < 2) {
                throw new IllegalArgumentException("Malformed YouTube URL");
            }
            
            String videoId = splitByV[1];
            if (videoId.contains("&")) {
                videoId = videoId.split("&")[0];
            }
            
            if (videoId.isEmpty()) {
                throw new IllegalArgumentException("Empty video ID");
            }
            
            return videoId;
        }
        
        if (url.contains("youtu.be")) {
            int lastSlashIndex = url.lastIndexOf("/");
            if (lastSlashIndex == -1 || lastSlashIndex == url.length() - 1) {
                throw new IllegalArgumentException("Malformed youtu.be URL");
            }
            
            String videoId = url.substring(lastSlashIndex + 1);
            if (videoId.contains("?")) {
                videoId = videoId.split("\\?")[0];
            }
            
            if (videoId.isEmpty()) {
                throw new IllegalArgumentException("Empty video ID");
            }
            
            return videoId;
        }
        
        throw new IllegalArgumentException("Invalid YouTube URL format");
    }
}