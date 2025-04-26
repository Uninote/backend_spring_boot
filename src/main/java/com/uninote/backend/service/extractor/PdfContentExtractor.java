package com.uninote.backend.service.extractor;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class PdfContentExtractor implements ContentExtractor {

    private static final Logger logger = LoggerFactory.getLogger(PdfContentExtractor.class);
    
    private static final Set<String> SUPPORTED_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".pdf"
    ));
    
    private static final Set<String> SUPPORTED_CONTENT_TYPES = new HashSet<>(Arrays.asList(
            "application/pdf"
    ));
    
    @Override
    public boolean canHandle(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }
        
        String lowerFilename = filename.toLowerCase();
        for (String extension : SUPPORTED_EXTENSIONS) {
            if (lowerFilename.endsWith(extension)) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean canHandleContentType(String contentType) {
        if (contentType == null || contentType.isEmpty()) {
            return false;
        }
        
        return SUPPORTED_CONTENT_TYPES.contains(contentType.toLowerCase());
    }
    
    @Override
    public String extractContent(MultipartFile file) {
        logger.info("Extracting content from PDF file: {}", file.getOriginalFilename());
        
        try (InputStream inputStream = file.getInputStream();
             PDDocument document = PDDocument.load(inputStream)) {
            
            return extractText(document);
            
        } catch (IOException e) {
            logger.error("Failed to extract text from PDF: {}", e.getMessage());
            throw new RuntimeException("PDF text extraction failed", e);
        }
    }
    
    @Override
    public String extractContentFromUrl(String fileUrl) {
        logger.info("Extracting content from PDF URL: {}", fileUrl);
        
        try {
            URL url = new URL(fileUrl);
            URLConnection connection = url.openConnection();
            
            try (InputStream inputStream = connection.getInputStream();
                 PDDocument document = PDDocument.load(inputStream)) {
                
                return extractText(document);
            }
            
        } catch (IOException e) {
            logger.error("Failed to extract text from PDF URL: {}", e.getMessage());
            throw new RuntimeException("PDF text extraction from URL failed", e);
        }
    }
    
    @Override
    public String getExtractorName() {
        return "PDF Extractor";
    }
    
    /**
     * Helper method to extract text from a PDDocument
     */
    private String extractText(PDDocument document) throws IOException {
        PDFTextStripper textStripper = new PDFTextStripper();
        
        // Extract text from the entire document
        String text = textStripper.getText(document);
        
        logger.info("Successfully extracted {} characters of text", text.length());
        return text;
    }
}