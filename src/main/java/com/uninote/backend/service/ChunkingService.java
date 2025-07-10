package com.uninote.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

@Service
public class ChunkingService {

    // Configuration constants
    private static final int DEFAULT_CHUNK_SIZE = 1000;
    private static final int DEFAULT_CHUNK_OVERLAP = 200;
    private static final int DEFAULT_MIN_CHUNK_SIZE = 100;
    
    // Patterns for different splitting strategies
    private static final Pattern SENTENCE_PATTERN = Pattern.compile("(?<=[.!?])\\s+");
    private static final Pattern PARAGRAPH_PATTERN = Pattern.compile("\\n\\s*\\n");
    private static final Pattern WORD_PATTERN = Pattern.compile("\\s+");
    
    public ChunkingService() {
        // No NLP initialization needed
    }
    
    /**
     * Fixed-size chunking strategy
     * Simple character-based splitting with overlap
     */
    public List<String> fixedSizeChunking(String text) {
        return fixedSizeChunking(text, DEFAULT_CHUNK_SIZE, DEFAULT_CHUNK_OVERLAP);
    }
    
    public List<String> fixedSizeChunking(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) return chunks;
        
        // For large documents, use character array approach to prevent OutOfMemoryError
        if (text.length() > 20000) {
            return fixedSizeChunkingLarge(text, chunkSize, overlap);
        }
        
        // For smaller documents, use the original approach
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
     * Memory-efficient chunking for large documents using character arrays
     */
    private List<String> fixedSizeChunkingLarge(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        char[] textArray = text.toCharArray();
        int textLength = textArray.length;
        
        int start = 0;
        while (start < textLength) {
            int end = Math.min(start + chunkSize, textLength);
            
            // Create new character array for this chunk to avoid memory references
            char[] chunkArray = new char[end - start];
            System.arraycopy(textArray, start, chunkArray, 0, end - start);
            
            chunks.add(new String(chunkArray));
            
            // Clear the chunk array to help GC
            chunkArray = null;
            
            start = end - overlap;
            if (start < 0) start = 0;
        }
        
        return chunks;
    }
    
    /**
     * Process large documents in batches to prevent memory issues
     */
    public void processLargeDocumentInBatches(String text, int batchSize, Consumer<List<String>> batchProcessor) {
        if (text == null || text.isEmpty()) return;
        
        List<String> chunks = new ArrayList<>();
        char[] textArray = text.toCharArray();
        int textLength = textArray.length;
        
        int start = 0;
        while (start < textLength) {
            int end = Math.min(start + DEFAULT_CHUNK_SIZE, textLength);
            
            // Create new character array for this chunk
            char[] chunkArray = new char[end - start];
            System.arraycopy(textArray, start, chunkArray, 0, end - start);
            
            chunks.add(new String(chunkArray));
            chunkArray = null;
            
            start = end - DEFAULT_CHUNK_OVERLAP;
            if (start < 0) start = 0;
            
            // Process batch when it reaches the batch size
            if (chunks.size() >= batchSize) {
                batchProcessor.accept(new ArrayList<>(chunks));
                chunks.clear();
                System.gc(); // Force garbage collection
            }
        }
        
        // Process remaining chunks
        if (!chunks.isEmpty()) {
            batchProcessor.accept(chunks);
        }
    }
    
    /**
     * Recursive chunking strategy
     * Splits on multiple separators in order of preference
     */
    public List<String> recursiveChunking(String text) {
        return recursiveChunking(text, DEFAULT_CHUNK_SIZE, DEFAULT_CHUNK_OVERLAP);
    }
    
    public List<String> recursiveChunking(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) return chunks;
        
        // If text is small enough, return as single chunk
        if (text.length() <= chunkSize) {
            chunks.add(text);
            return chunks;
        }
        
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
     * Get chunking statistics
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
