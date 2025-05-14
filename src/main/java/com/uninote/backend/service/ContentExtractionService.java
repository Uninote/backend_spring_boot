package com.uninote.backend.service;

import com.uninote.backend.entity.Resource;
import com.uninote.backend.entity.FileResource;
import com.uninote.backend.entity.Note;
import com.uninote.backend.entity.NoteResource;
import com.uninote.backend.entity.YouTubeResource;
import com.uninote.backend.repository.NoteRepository;
import com.uninote.backend.repository.ResourceRepository;
import com.uninote.backend.service.extractor.ContentExtractor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@Service
public class ContentExtractionService {

    private static final Logger logger = LoggerFactory.getLogger(ContentExtractionService.class);

    @Autowired
    private NoteRepository noteRepository;
    
    @Autowired
    private ResourceRepository resourceRepository;
    
    @Autowired
    private List<ContentExtractor> contentExtractors;

    @Autowired
    private LangChainContentService langChainContentService;

    
    // Cache for file extensions to extractors
    private final Map<String, ContentExtractor> extensionCache = new HashMap<>();
    
    // Cache for content types to extractors
    private final Map<String, ContentExtractor> contentTypeCache = new HashMap<>();
    
    /**
     * Extract content from a file and save it to the resource
     * 
     * @param file The file to extract content from
     * @param resource The resource to update
     * @return The updated resource with extracted content
     */
    public Resource extractContent(MultipartFile file, Resource resource) {
        logger.info("Extracting content from file: {}", file.getOriginalFilename());
        if (resource instanceof NoteResource){
            NoteResource nr = (NoteResource) resource;
            nr.setContent(nr.getNote().getContent());
            return resourceRepository.save(nr);
        }
        
        // Find an appropriate extractor
        ContentExtractor extractor = findExtractor(file);
        
        // Extract the content
        String content = extractor.extractContent(file);
        resource.setContent(content);
        
        // Save and return the updated resource
        return resourceRepository.save(resource);
    }
    
    /**
     * Extract content from a URL and save it to the resource
     * 
     * @param fileUrl The URL to extract content from
     * @param filename The filename (for determining file type)
     * @param resourceId The ID of the resource to update
     * @return The updated resource with extracted content
     */
    public Resource extractContentFromUrl(String fileUrl, String filename, Long resourceId) {
        logger.info("Extracting content from URL: {}", fileUrl);
        
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found with ID: " + resourceId));
        
        // Find an appropriate extractor
        ContentExtractor extractor = findExtractorByFilename(filename);
        
        // Extract the content
        String content = extractor.extractContentFromUrl(fileUrl);
        resource.setContent(content);
        
        // Save and return the updated resource
        return resourceRepository.save(resource);
    }
    
    /**
     * Extract content from an already uploaded resource by ID
     * 
     * @param resourceId The ID of the resource
     * @return The updated resource with extracted content
     */
    public Resource extractContentFromExistingResource(Long resourceId) {
        logger.info("Extracting content from existing resource with ID: {}", resourceId);
        
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found with ID: " + resourceId));
        
        // Handle different resource types
        if (resource instanceof FileResource) {
            FileResource fileResource = (FileResource) resource;
            return extractContentFromUrl(fileResource.getFileUrl(), fileResource.getTitle(), resourceId);
        } else if (resource instanceof YouTubeResource) {
            YouTubeResource youTubeResource = (YouTubeResource) resource;
            // Call YouTube transcript extraction (to be implemented)
            return extractContentFromYouTube(youTubeResource.getYoutubeUrl(), resourceId);
        } else {
            throw new UnsupportedOperationException("Content extraction not supported for this resource type");
        }
    }
    
    /**
     * Extract transcript from a YouTube video
     * 
     * @param youtubeUrl The YouTube URL
     * @param resourceId The ID of the resource to update
     * @return The updated resource with extracted content
     */
    private Resource extractContentFromYouTube(String youtubeUrl, Long resourceId) {
        logger.info("Extracting transcript from YouTube URL: {}", youtubeUrl);
        
        // This is a placeholder for YouTube extraction - to be implemented in the future
        // For now, just return a message indicating it's not implemented
        
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found with ID: " + resourceId));
        
        String placeholderContent = "YouTube transcript extraction is not yet implemented. URL: " + youtubeUrl;
        resource.setContent(placeholderContent);
        
        return resourceRepository.save(resource);
    }
    
    /**
     * Find an appropriate extractor for a file
     * 
     * @param file The file to find an extractor for
     * @return The content extractor
     */
    private ContentExtractor findExtractor(MultipartFile file) {
        // Try to find by content type first
        String contentType = file.getContentType();
        if (contentType != null && !contentType.isEmpty()) {
            ContentExtractor extractor = findExtractorByContentType(contentType);
            if (extractor != null) {
                return extractor;
            }
        }
        
        // Fall back to filename
        String filename = file.getOriginalFilename();
        if (filename != null && !filename.isEmpty()) {
            ContentExtractor extractor = findExtractorByFilename(filename);
            if (extractor != null) {
                return extractor;
            }
        }
        
        // If we still don't have an extractor, throw an exception
        throw new UnsupportedOperationException("No suitable content extractor found for file: " + 
                (filename != null ? filename : "unknown") + ", content type: " + 
                (contentType != null ? contentType : "unknown"));
    }
    
    /**
     * Find an extractor by content type
     * 
     * @param contentType The content type to find an extractor for
     * @return The content extractor, or null if none found
     */
    private ContentExtractor findExtractorByContentType(String contentType) {
        // Check cache first
        if (contentTypeCache.containsKey(contentType.toLowerCase())) {
            return contentTypeCache.get(contentType.toLowerCase());
        }
        
        // Find an extractor that can handle this content type
        Optional<ContentExtractor> extractor = contentExtractors.stream()
                .filter(e -> e.canHandleContentType(contentType))
                .findFirst();
        
        if (extractor.isPresent()) {
            // Cache the result
            contentTypeCache.put(contentType.toLowerCase(), extractor.get());
            return extractor.get();
        }
        
        return null;
    }
    
    /**
     * Find an extractor by filename
     * 
     * @param filename The filename to find an extractor for
     * @return The content extractor, or null if none found
     */
    private ContentExtractor findExtractorByFilename(String filename) {
        // Get file extension
        String extension = "";
        if (filename.contains(".")) {
            extension = filename.substring(filename.lastIndexOf(".")).toLowerCase();
        }
        
        // Check cache first
        if (!extension.isEmpty() && extensionCache.containsKey(extension)) {
            return extensionCache.get(extension);
        }
        
        // Find an extractor that can handle this filename
        Optional<ContentExtractor> extractor = contentExtractors.stream()
                .filter(e -> e.canHandle(filename))
                .findFirst();
        
        if (extractor.isPresent()) {
            // Cache the result if we have an extension
            if (!extension.isEmpty()) {
                extensionCache.put(extension, extractor.get());
            }
            return extractor.get();
        }
        
        return null;
    }

    public void extractContentFromNote(Long noteId) {
        logger.info("Extracting content from note with ID: {}", noteId);

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found with ID: " + noteId));

        String url = note.getPdfUrl();
        String filename = note.getFilename();

        if (url == null || filename == null) {
            throw new RuntimeException("Note does not have a valid URL or filename for content extraction");
        }

        
        ContentExtractor extractor = findExtractorByFilename(filename);
        if (extractor == null) {
            throw new UnsupportedOperationException("No suitable extractor found for file: " + filename);
        }

        String content = extractor.extractContentFromUrl(url);

        note.setContent(content);
        noteRepository.save(note);

        logger.info("Content extraction completed for note ID: {}", noteId);
    }

}