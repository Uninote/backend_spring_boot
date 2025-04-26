package com.uninote.backend.service.extractor;

import com.uninote.backend.entity.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface ContentExtractor {

    /**
     * Check if this extractor can handle the given file type
     * 
     * @param filename The filename to check
     * @return True if this extractor can handle the file, false otherwise
     */
    boolean canHandle(String filename);
    
    /**
     * Check if this extractor can handle the given content type
     * 
     * @param contentType The content type to check (e.g., "application/pdf")
     * @return True if this extractor can handle the content type, false otherwise
     */
    boolean canHandleContentType(String contentType);
    
    /**
     * Extract content from a file
     * 
     * @param file The file to extract content from
     * @return The extracted content
     */
    String extractContent(MultipartFile file);
    
    /**
     * Extract content from a URL
     * 
     * @param fileUrl The URL of the file to extract content from
     * @return The extracted content
     */
    String extractContentFromUrl(String fileUrl);
    
    /**
     * Get the name of this extractor (for logging and identification)
     * 
     * @return The extractor name
     */
    String getExtractorName();
}