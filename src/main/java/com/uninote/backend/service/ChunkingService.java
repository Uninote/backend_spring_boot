package com.uninote.backend.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

@Service
public class ChunkingService {

    // Configuration constants
    private static final int DEFAULT_CHUNK_SIZE = 1000;
    private static final int DEFAULT_CHUNK_OVERLAP = 200;
    private static final int DEFAULT_MIN_CHUNK_SIZE = 100;
    
    // NLP components
    private SentenceDetectorME sentenceDetector;
    private TokenizerME tokenizer;
    private POSTaggerME posTagger;
    
    // Patterns for different splitting strategies
    private static final Pattern SENTENCE_PATTERN = Pattern.compile("(?<=[.!?])\\s+");
    private static final Pattern PARAGRAPH_PATTERN = Pattern.compile("\\n\\s*\\n");
    private static final Pattern WORD_PATTERN = Pattern.compile("\\s+");
    
    public ChunkingService() {
        initializeNLP();
    }
    
    /**
     * Initialize NLP components
     */
    private void initializeNLP() {
        try {
            // Load sentence detection model
            InputStream sentenceModelStream = getClass().getResourceAsStream("/models/en-sent.bin");
            if (sentenceModelStream != null) {
                SentenceModel sentenceModel = new SentenceModel(sentenceModelStream);
                sentenceDetector = new SentenceDetectorME(sentenceModel);
                sentenceModelStream.close();
                System.out.println("✅ NLP models loaded successfully");
            } else {
                System.out.println("⚠️ NLP models not found. Using fallback semantic chunking.");
            }
            
            // Load tokenizer model
            InputStream tokenizerModelStream = getClass().getResourceAsStream("/models/en-token.bin");
            if (tokenizerModelStream != null) {
                TokenizerModel tokenizerModel = new TokenizerModel(tokenizerModelStream);
                tokenizer = new TokenizerME(tokenizerModel);
                tokenizerModelStream.close();
            }
            
            // Load POS tagger model
            InputStream posModelStream = getClass().getResourceAsStream("/models/en-pos-maxent.bin");
            if (posModelStream != null) {
                POSModel posModel = new POSModel(posModelStream);
                posTagger = new POSTaggerME(posModel);
                posModelStream.close();
            }
        } catch (IOException e) {
            System.err.println("⚠️ Warning: Could not load NLP models. Using fallback semantic chunking.");
        }
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
     * NLP-enhanced semantic chunking strategy
     * Uses proper sentence detection, POS tagging, and semantic analysis
     */
    public List<String> semanticChunking(String text) {
        return semanticChunking(text, DEFAULT_CHUNK_SIZE, DEFAULT_CHUNK_OVERLAP);
    }
    
    public List<String> semanticChunking(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) return chunks;
        
        // Check if NLP models are available
        if (sentenceDetector != null && tokenizer != null && posTagger != null) {
            // Use full NLP pipeline
            return semanticChunkingWithNLP(text, chunkSize, overlap);
        } else {
            // Use fallback semantic chunking without NLP models
            return semanticChunkingFallback(text, chunkSize, overlap);
        }
    }
    
    /**
     * Full NLP-enhanced semantic chunking
     */
    private List<String> semanticChunkingWithNLP(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        
        // Use NLP sentence detection
        String[] sentences = detectSentencesNLP(text);
        
        // Group sentences by semantic similarity
        List<SentenceGroup> sentenceGroups = groupSentencesBySemantics(sentences, chunkSize);
        
        // Create chunks from sentence groups
        for (SentenceGroup group : sentenceGroups) {
            String chunk = group.getText();
            if (chunk.length() > chunkSize) {
                // If group is too large, split it further
                chunks.addAll(splitLargeChunk(chunk, chunkSize, overlap));
            } else {
                chunks.add(chunk);
            }
        }
        
        // Add overlap between chunks
        return addOverlapBetweenChunks(chunks, overlap);
    }
    
    /**
     * Fallback semantic chunking without NLP models
     * Uses advanced regex patterns and concept extraction
     */
    private List<String> semanticChunkingFallback(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        
        // Enhanced sentence detection with regex
        String[] sentences = detectSentencesAdvanced(text);
        
        // Group sentences by semantic similarity using word overlap
        List<SentenceGroup> sentenceGroups = groupSentencesByWordOverlap(sentences, chunkSize);
        
        // Create chunks from sentence groups
        for (SentenceGroup group : sentenceGroups) {
            String chunk = group.getText();
            if (chunk.length() > chunkSize) {
                // If group is too large, split it further
                chunks.addAll(splitLargeChunk(chunk, chunkSize, overlap));
            } else {
                chunks.add(chunk);
            }
        }
        
        // Add overlap between chunks
        return addOverlapBetweenChunks(chunks, overlap);
    }
    
    /**
     * Advanced sentence detection using regex patterns
     */
    private String[] detectSentencesAdvanced(String text) {
        // Enhanced regex for sentence detection
        // Handles abbreviations, quotes, and other edge cases
        String sentencePattern = "(?<=[.!?])\\s+(?=[A-Z])";
        return text.split(sentencePattern);
    }
    
    /**
     * Group sentences by word overlap similarity
     */
    private List<SentenceGroup> groupSentencesByWordOverlap(String[] sentences, int maxChunkSize) {
        List<SentenceGroup> groups = new ArrayList<>();
        SentenceGroup currentGroup = new SentenceGroup();
        
        for (String sentence : sentences) {
            sentence = sentence.trim();
            if (sentence.isEmpty()) continue;
            
            // Check if adding this sentence would exceed chunk size
            if (currentGroup.getLength() + sentence.length() + 1 <= maxChunkSize) {
                // Check word overlap similarity with current group
                if (currentGroup.isEmpty() || hasWordOverlap(currentGroup.getText(), sentence)) {
                    currentGroup.addSentence(sentence);
                } else {
                    // Start a new group
                    if (!currentGroup.isEmpty()) {
                        groups.add(currentGroup);
                    }
                    currentGroup = new SentenceGroup();
                    currentGroup.addSentence(sentence);
                }
            } else {
                // Current group is full, save it and start new one
                if (!currentGroup.isEmpty()) {
                    groups.add(currentGroup);
                }
                currentGroup = new SentenceGroup();
                currentGroup.addSentence(sentence);
            }
        }
        
        // Add the last group
        if (!currentGroup.isEmpty()) {
            groups.add(currentGroup);
        }
        
        return groups;
    }
    
    /**
     * Check if two texts have significant word overlap
     */
    private boolean hasWordOverlap(String text1, String text2) {
        List<String> words1 = extractWords(text1);
        List<String> words2 = extractWords(text2);
        
        if (words1.isEmpty() || words2.isEmpty()) return false;
        
        // Count overlapping words
        long overlap = words1.stream()
                            .filter(words2::contains)
                            .count();
        
        // Calculate overlap ratio
        double overlapRatio = (double) overlap / Math.min(words1.size(), words2.size());
        
        return overlapRatio > 0.2; // 20% word overlap threshold
    }
    
    /**
     * Extract meaningful words from text
     */
    private List<String> extractWords(String text) {
        return Pattern.compile("\\b\\w{3,}\\b")
                     .matcher(text.toLowerCase())
                     .results()
                     .map(match -> match.group())
                     .filter(word -> !isStopWord(word))
                     .collect(Collectors.toList());
    }
    
    /**
     * Simple stop word filter
     */
    private boolean isStopWord(String word) {
        String[] stopWords = {"the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by", "is", "are", "was", "were", "be", "been", "have", "has", "had", "do", "does", "did", "will", "would", "could", "should", "may", "might", "can", "this", "that", "these", "those"};
        return java.util.Arrays.asList(stopWords).contains(word);
    }
    
    /**
     * Detect sentences using NLP or fall back to regex
     */
    private String[] detectSentencesNLP(String text) {
        if (sentenceDetector != null) {
            try {
                return sentenceDetector.sentDetect(text);
            } catch (Exception e) {
                // Fall back to regex
            }
        }
        return SENTENCE_PATTERN.split(text);
    }
    
    /**
     * Group sentences by semantic similarity and chunk size constraints
     */
    private List<SentenceGroup> groupSentencesBySemantics(String[] sentences, int maxChunkSize) {
        List<SentenceGroup> groups = new ArrayList<>();
        SentenceGroup currentGroup = new SentenceGroup();
        
        for (String sentence : sentences) {
            sentence = sentence.trim();
            if (sentence.isEmpty()) continue;
            
            // Check if adding this sentence would exceed chunk size
            if (currentGroup.getLength() + sentence.length() + 1 <= maxChunkSize) {
                // Check semantic similarity with current group
                if (currentGroup.isEmpty() || isSemanticallySimilar(currentGroup, sentence)) {
                    currentGroup.addSentence(sentence);
                } else {
                    // Start a new group
                    if (!currentGroup.isEmpty()) {
                        groups.add(currentGroup);
                    }
                    currentGroup = new SentenceGroup();
                    currentGroup.addSentence(sentence);
                }
            } else {
                // Current group is full, save it and start new one
                if (!currentGroup.isEmpty()) {
                    groups.add(currentGroup);
                }
                currentGroup = new SentenceGroup();
                currentGroup.addSentence(sentence);
            }
        }
        
        // Add the last group
        if (!currentGroup.isEmpty()) {
            groups.add(currentGroup);
        }
        
        return groups;
    }
    
    /**
     * Check if a sentence is semantically similar to a sentence group
     */
    private boolean isSemanticallySimilar(SentenceGroup group, String newSentence) {
        if (group.isEmpty()) return true;
        
        // Extract key concepts from the group and new sentence
        List<String> groupConcepts = extractKeyConcepts(group.getText());
        List<String> sentenceConcepts = extractKeyConcepts(newSentence);
        
        // Calculate semantic similarity
        double similarity = calculateSemanticSimilarity(groupConcepts, sentenceConcepts);
        
        // Also check POS patterns for structural similarity
        boolean structuralSimilar = checkStructuralSimilarity(group.getText(), newSentence);
        
        return similarity > 0.3 || structuralSimilar; // Threshold for semantic similarity
    }
    
    /**
     * Extract key concepts from text using NLP
     */
    private List<String> extractKeyConcepts(String text) {
        List<String> concepts = new ArrayList<>();
        
        if (tokenizer != null && posTagger != null) {
            try {
                // Tokenize the text
                String[] tokens = tokenizer.tokenize(text);
                String[] posTags = posTagger.tag(tokens);
                
                // Extract nouns, verbs, and adjectives as key concepts
                for (int i = 0; i < tokens.length; i++) {
                    String pos = posTags[i];
                    String token = tokens[i].toLowerCase();
                    
                    // Focus on content words (nouns, verbs, adjectives)
                    if (pos.startsWith("NN") || pos.startsWith("VB") || pos.startsWith("JJ")) {
                        if (token.length() > 2) { // Filter out very short words
                            concepts.add(token);
                        }
                    }
                }
            } catch (Exception e) {
                // Fall back to simple word extraction
                concepts = extractSimpleConcepts(text);
            }
        } else {
            concepts = extractSimpleConcepts(text);
        }
        
        return concepts;
    }
    
    /**
     * Simple concept extraction as fallback
     */
    private List<String> extractSimpleConcepts(String text) {
        return Pattern.compile("\\b\\w{4,}\\b")
                     .matcher(text.toLowerCase())
                     .results()
                     .map(match -> match.group())
                     .collect(Collectors.toList());
    }
    
    /**
     * Calculate semantic similarity between two concept lists
     */
    private double calculateSemanticSimilarity(List<String> concepts1, List<String> concepts2) {
        if (concepts1.isEmpty() || concepts2.isEmpty()) return 0.0;
        
        // Count overlapping concepts
        long overlap = concepts1.stream()
                               .filter(concepts2::contains)
                               .count();
        
        // Jaccard similarity
        double union = concepts1.size() + concepts2.size() - overlap;
        return union > 0 ? overlap / union : 0.0;
    }
    
    /**
     * Check structural similarity using POS patterns
     */
    private boolean checkStructuralSimilarity(String text1, String text2) {
        if (tokenizer == null || posTagger == null) return false;
        
        try {
            String[] tokens1 = tokenizer.tokenize(text1);
            String[] tokens2 = tokenizer.tokenize(text2);
            String[] pos1 = posTagger.tag(tokens1);
            String[] pos2 = posTagger.tag(tokens2);
            
            // Compare POS patterns (simplified)
            if (pos1.length > 0 && pos2.length > 0) {
                // Check if both sentences start with similar POS patterns
                String firstPos1 = pos1[0];
                String firstPos2 = pos2[0];
                
                return firstPos1.equals(firstPos2) || 
                       (firstPos1.startsWith("NN") && firstPos2.startsWith("NN")) ||
                       (firstPos1.startsWith("VB") && firstPos2.startsWith("VB"));
            }
        } catch (Exception e) {
            // Ignore errors and return false
        }
        
        return false;
    }
    
    /**
     * Split a large chunk into smaller pieces
     */
    private List<String> splitLargeChunk(String chunk, int maxSize, int overlap) {
        List<String> subChunks = new ArrayList<>();
        int start = 0;
        
        while (start < chunk.length()) {
            int end = Math.min(start + maxSize, chunk.length());
            
            // Try to find a good break point
            int breakPoint = findGoodBreakPoint(chunk, start, end);
            if (breakPoint > start) {
                end = breakPoint;
            }
            
            subChunks.add(chunk.substring(start, end));
            start = end - overlap;
            if (start < 0) start = 0;
        }
        
        return subChunks;
    }
    
    /**
     * Find a good break point (sentence or word boundary)
     */
    private int findGoodBreakPoint(String text, int start, int end) {
        // Look for sentence boundaries first
        for (int i = end - 1; i >= start; i--) {
            if (text.charAt(i) == '.' || text.charAt(i) == '!' || text.charAt(i) == '?') {
                if (i + 1 < text.length() && Character.isWhitespace(text.charAt(i + 1))) {
                    return i + 1;
                }
            }
        }
        
        // Look for word boundaries
        for (int i = end - 1; i >= start; i--) {
            if (Character.isWhitespace(text.charAt(i))) {
                return i + 1;
            }
        }
        
        return end;
    }
    
    /**
     * Add overlap between chunks
     */
    private List<String> addOverlapBetweenChunks(List<String> chunks, int overlap) {
        if (chunks.size() <= 1) return chunks;
        
        List<String> overlappedChunks = new ArrayList<>();
        
        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            
            if (i > 0) {
                // Add overlap from previous chunk
                String previousChunk = chunks.get(i - 1);
                String overlapText = getOverlapText(previousChunk, overlap);
                chunk = overlapText + " " + chunk;
            }
            
            overlappedChunks.add(chunk);
        }
        
        return overlappedChunks;
    }
    
    /**
     * Sentence group for semantic chunking
     */
    private static class SentenceGroup {
        private final List<String> sentences = new ArrayList<>();
        
        public void addSentence(String sentence) {
            sentences.add(sentence);
        }
        
        public String getText() {
            return String.join(" ", sentences);
        }
        
        public int getLength() {
            return getText().length();
        }
        
        public boolean isEmpty() {
            return sentences.isEmpty();
        }
    }
    
    /**
     * Hybrid chunking strategy
     * Combines semantic and recursive approaches
     */
    public List<String> hybridChunking(String text) {
        return hybridChunking(text, DEFAULT_CHUNK_SIZE, DEFAULT_CHUNK_OVERLAP);
    }
    
    public List<String> hybridChunking(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) return chunks;
        
        // First, try semantic chunking
        List<String> semanticChunks = semanticChunking(text, chunkSize, overlap);
        
        // For chunks that are too small, try to merge them
        List<String> optimizedChunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();
        
        for (String chunk : semanticChunks) {
            if (currentChunk.length() + chunk.length() <= chunkSize) {
                currentChunk.append(chunk).append(" ");
            } else {
                if (currentChunk.length() > 0) {
                    optimizedChunks.add(currentChunk.toString().trim());
                    // Add overlap
                    String overlapText = getOverlapText(currentChunk.toString(), overlap);
                    currentChunk = new StringBuilder(overlapText);
                }
                currentChunk.append(chunk).append(" ");
            }
        }
        
        if (currentChunk.length() > 0) {
            optimizedChunks.add(currentChunk.toString().trim());
        }
        
        return optimizedChunks;
    }
    
    /**
     * Content-aware chunking strategy
     * Adapts chunking strategy based on content type
     */
    public List<String> contentAwareChunking(String text) {
        return contentAwareChunking(text, DEFAULT_CHUNK_SIZE, DEFAULT_CHUNK_OVERLAP);
    }
    
    public List<String> contentAwareChunking(String text, int chunkSize, int overlap) {
        if (text == null || text.isEmpty()) return new ArrayList<>();
        
        // Analyze content type
        boolean hasCode = text.contains("```") || text.contains("def ") || text.contains("function ");
        boolean hasLists = text.contains("\n- ") || text.contains("\n* ") || text.contains("\n1. ");
        boolean hasTables = text.contains("|") && text.contains("\n");
        boolean isTechnical = text.contains("algorithm") || text.contains("function") || text.contains("method");
        
        if (hasCode) {
            // For code, use recursive chunking to preserve code blocks
            return recursiveChunking(text, chunkSize, overlap);
        } else if (hasLists || hasTables) {
            // For structured content, use semantic chunking
            return semanticChunking(text, chunkSize, overlap);
        } else if (isTechnical) {
            // For technical content, use hybrid approach
            return hybridChunking(text, chunkSize, overlap);
        } else {
            // For general content, use semantic chunking
            return semanticChunking(text, chunkSize, overlap);
        }
    }
    
    /**
     * Legacy method for backward compatibility
     */
    public List<String> splitIntoChunks(String text) {
        return fixedSizeChunking(text);
    }
    
    /**
     * Legacy semantic method for backward compatibility
     */
    public List<String> splitIntoChunksSemanticWithOverlap(String text) {
        return semanticChunking(text);
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
