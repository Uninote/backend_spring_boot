package com.uninote.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ChunkingService {
    
    private static final Logger logger = LoggerFactory.getLogger(ChunkingService.class);

    // Configuration constants
    private static final int DEFAULT_CHUNK_SIZE = 1000;
    private static final int DEFAULT_CHUNK_OVERLAP = 200;
    private static final int DEFAULT_MIN_CHUNK_SIZE = 100;
    private static final int LARGE_DOCUMENT_THRESHOLD = 50000; // 50KB threshold
    private static final int CRITICAL_DOCUMENT_THRESHOLD = 200000; // 200KB threshold
    
    // Patterns for different splitting strategies
    private static final Pattern SENTENCE_PATTERN = Pattern.compile("(?<=[.!?])\\s+");
    private static final Pattern PARAGRAPH_PATTERN = Pattern.compile("\\n\\s*\\n");
    private static final Pattern WORD_PATTERN = Pattern.compile("\\s+");
    
    public ChunkingService() {
        // No NLP initialization needed
    }
    
    /**
     * Fixed-size chunking strategy with memory optimization
     * Simple character-based splitting with overlap
     */
    public List<String> fixedSizeChunking(String text) {
        return fixedSizeChunking(text, DEFAULT_CHUNK_SIZE, DEFAULT_CHUNK_OVERLAP);
    }
    
    public List<String> fixedSizeChunking(String text, int chunkSize, int overlap) {
        if (text == null || text.isEmpty()) return new ArrayList<>();
        
        // For very large documents, use streaming approach
        if (text.length() > CRITICAL_DOCUMENT_THRESHOLD) {
            logger.warn("Processing very large document ({} chars) - using streaming chunking", text.length());
            return fixedSizeChunkingStreaming(text, chunkSize, overlap);
        }
        
        // For large documents, use optimized approach
        if (text.length() > LARGE_DOCUMENT_THRESHOLD) {
            logger.info("Processing large document ({} chars) - using optimized chunking", text.length());
            return fixedSizeChunkingOptimized(text, chunkSize, overlap);
        }
        
        // For smaller documents, use the original approach
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            chunks.add(text.substring(start, end));
            start = end - overlap;
            if (start < 0) start = 0;
        }
        
        return chunks;
    }
    
    /**
     * Memory-efficient streaming chunking for very large documents
     * Uses substring directly without character array copying
     */
    private List<String> fixedSizeChunkingStreaming(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        int textLength = text.length();
        
        logger.debug("Starting streaming chunking for {} chars", textLength);
        
        int start = 0;
        int chunkCount = 0;
        
        while (start < textLength) {
            int end = Math.min(start + chunkSize, textLength);
            
            // Use substring directly - most memory efficient for large texts
            String chunk = text.substring(start, end);
            chunks.add(chunk);
            
            chunkCount++;
            start = end - overlap;
            if (start < 0) start = 0;
            
            // Log progress and force GC every 50 chunks for very large documents
            if (chunkCount % 50 == 0) {
                logger.debug("Streaming chunking progress: {} chunks processed", chunkCount);
                System.gc();
            }
            
            // Safety check - prevent infinite loops
            if (chunkCount > 10000) {
                logger.error("Too many chunks generated ({}), stopping to prevent memory issues", chunkCount);
                break;
            }
        }
        
        logger.debug("Streaming chunking completed: {} chunks", chunkCount);
        return chunks;
    }
    
    /**
     * Optimized chunking for large documents - NO character array copying
     * Uses substring with memory monitoring
     */
    private List<String> fixedSizeChunkingOptimized(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        int textLength = text.length();
        
        logger.debug("Starting optimized chunking for {} chars", textLength);
        
        int start = 0;
        int chunkCount = 0;
        
        while (start < textLength) {
            int end = Math.min(start + chunkSize, textLength);
            
            // Use substring directly - avoid character array copying
            String chunk = text.substring(start, end);
            chunks.add(chunk);
            
            chunkCount++;
            start = end - overlap;
            if (start < 0) start = 0;
            
            // Log progress and force GC every 100 chunks
            if (chunkCount % 100 == 0) {
                logger.debug("Optimized chunking progress: {} chunks processed", chunkCount);
                System.gc();
            }
            
            // Safety check - prevent infinite loops
            if (chunkCount > 5000) {
                logger.error("Too many chunks generated ({}), stopping to prevent memory issues", chunkCount);
                break;
            }
        }
        
        logger.debug("Optimized chunking completed: {} chunks", chunkCount);
        return chunks;
    }
    
    /**
     * Process large documents in batches to prevent memory issues
     * Enhanced with better memory management and progress tracking
     */
    public void processLargeDocumentInBatches(String text, int batchSize, Consumer<List<String>> batchProcessor) {
        if (text == null || text.isEmpty()) return;
        
        logger.info("Processing large document in batches: {} chars, batch size: {}", text.length(), batchSize);
        
        List<String> chunks = new ArrayList<>();
        int textLength = text.length();
        
        int start = 0;
        int processedChunks = 0;
        int batchCount = 0;
        
        while (start < textLength) {
            int end = Math.min(start + DEFAULT_CHUNK_SIZE, textLength);
            
            // Use substring directly for better memory efficiency
            String chunk = text.substring(start, end);
            chunks.add(chunk);
            
            start = end - DEFAULT_CHUNK_OVERLAP;
            if (start < 0) start = 0;
            
            // Process batch when it reaches the batch size
            if (chunks.size() >= batchSize) {
                batchProcessor.accept(new ArrayList<>(chunks));
                processedChunks += chunks.size();
                batchCount++;
                chunks.clear();
                
                logger.debug("Processed batch {}: {} chunks total", batchCount, processedChunks);
                
                // Force garbage collection every few batches
                if (batchCount % 3 == 0) {
                    System.gc();
                }
            }
        }
        
        // Process remaining chunks
        if (!chunks.isEmpty()) {
            batchProcessor.accept(chunks);
            processedChunks += chunks.size();
            logger.debug("Processed final batch: {} chunks total", processedChunks);
        }
        
        logger.info("Batch processing completed: {} total chunks in {} batches", processedChunks, batchCount + 1);
    }
    
    /**
     * Memory-efficient recursive chunking strategy
     * Splits on multiple separators in order of preference
     */
    public List<String> recursiveChunking(String text) {
        return recursiveChunking(text, DEFAULT_CHUNK_SIZE, DEFAULT_CHUNK_OVERLAP);
    }
    
    public List<String> recursiveChunking(String text, int chunkSize, int overlap) {
        if (text == null || text.isEmpty()) return new ArrayList<>();
        
        // If text is small enough, return as single chunk
        if (text.length() <= chunkSize) {
            List<String> chunks = new ArrayList<>();
            chunks.add(text);
            return chunks;
        }
        
        // For very large documents, fall back to fixed-size chunking
        if (text.length() > LARGE_DOCUMENT_THRESHOLD) {
            logger.info("Large document detected in recursive chunking, falling back to fixed-size chunking");
            return fixedSizeChunking(text, chunkSize, overlap);
        }
        
        List<String> chunks = new ArrayList<>();
        
        // Try to split on different separators in order of preference
        String[] separators = {"\n\n", "\n", ". ", "! ", "? ", " ", ""};
        
        for (String separator : separators) {
            if (separator.isEmpty()) {
                // Last resort: force split
                chunks.addAll(fixedSizeChunking(text, chunkSize, overlap));
                break;
            }
            
            String[] parts = text.split(Pattern.quote(separator));
            if (parts.length > 1) {
                List<String> tempChunks = new ArrayList<>();
                StringBuilder currentChunk = new StringBuilder();
                
                for (String part : parts) {
                    if (currentChunk.length() + part.length() + separator.length() <= chunkSize) {
                        currentChunk.append(part).append(separator);
                    } else {
                        if (currentChunk.length() > 0) {
                            tempChunks.add(currentChunk.toString().trim());
                            // Add overlap
                            String overlapText = getOverlapText(currentChunk.toString(), overlap);
                            currentChunk = new StringBuilder(overlapText);
                        }
                        currentChunk.append(part).append(separator);
                    }
                }
                
                if (currentChunk.length() > 0) {
                    tempChunks.add(currentChunk.toString().trim());
                }
                
                // If we got reasonable chunks, use them
                if (tempChunks.size() > 1) {
                    chunks.addAll(tempChunks);
                    break;
                }
            }
        }
        
        return chunks;
    }
    
    /**
     * Legacy method for backward compatibility
     */
    public List<String> splitIntoChunks(String text) {
        return fixedSizeChunking(text);
    }
    
    /**
     * Legacy semantic method for backward compatibility - now uses simple chunking
     */
    public List<String> splitIntoChunksSemanticWithOverlap(String text) {
        return fixedSizeChunking(text);
    }
    
    /**
     * Helper method to get overlap text from a chunk
     */
    private String getOverlapText(String chunk, int overlap) {
        if (chunk.length() <= overlap) {
            return chunk;
        }
        
        // Try to find a good break point for overlap
        int start = chunk.length() - overlap;
        
        // Look for sentence boundaries in the overlap area
        for (int i = start; i < chunk.length(); i++) {
            if (chunk.charAt(i) == '.' || chunk.charAt(i) == '!' || chunk.charAt(i) == '?') {
                if (i + 1 < chunk.length() && Character.isWhitespace(chunk.charAt(i + 1))) {
                    return chunk.substring(i + 1);
                }
            }
        }
        
        // Look for word boundaries
        for (int i = start; i < chunk.length(); i++) {
            if (Character.isWhitespace(chunk.charAt(i))) {
                return chunk.substring(i + 1);
            }
        }
        
        // Fallback to character-based overlap
        return chunk.substring(start);
    }
    
    /**
     * Get chunking statistics with memory-efficient implementation
     */
    public ChunkingStats getChunkingStats(List<String> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return new ChunkingStats(0, 0, 0, 0);
        }
        
        int totalChunks = chunks.size();
        int totalChars = chunks.stream().mapToInt(String::length).sum();
        int avgChunkSize = totalChars / totalChunks;
        int minChunkSize = chunks.stream().mapToInt(String::length).min().orElse(0);
        int maxChunkSize = chunks.stream().mapToInt(String::length).max().orElse(0);
        
        return new ChunkingStats(totalChunks, avgChunkSize, minChunkSize, maxChunkSize);
    }
    
    /**
     * Statistics class for chunking results
     */
    public static class ChunkingStats {
        private final int totalChunks;
        private final int avgChunkSize;
        private final int minChunkSize;
        private final int maxChunkSize;
        
        public ChunkingStats(int totalChunks, int avgChunkSize, int minChunkSize, int maxChunkSize) {
            this.totalChunks = totalChunks;
            this.avgChunkSize = avgChunkSize;
            this.minChunkSize = minChunkSize;
            this.maxChunkSize = maxChunkSize;
        }
        
        // Getters
        public int getTotalChunks() { return totalChunks; }
        public int getAvgChunkSize() { return avgChunkSize; }
        public int getMinChunkSize() { return minChunkSize; }
        public int getMaxChunkSize() { return maxChunkSize; }
        
        @Override
        public String toString() {
            return String.format("Chunks: %d, Avg: %d chars, Min: %d chars, Max: %d chars", 
                               totalChunks, avgChunkSize, minChunkSize, maxChunkSize);
        }
    }
}
