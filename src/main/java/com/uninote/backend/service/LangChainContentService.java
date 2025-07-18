package com.uninote.backend.service;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.uninote.backend.config.AzureOpenAiConfig;
import com.uninote.backend.entity.Resource;
import com.uninote.backend.repository.ResourceRepository;
import com.uninote.backend.service.embedding.EmbeddingService;
import com.uninote.backend.service.embedding.PineconeVector;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import org.springframework.transaction.support.TransactionTemplate;


@Service
public class LangChainContentService {

    private static final Logger logger = LoggerFactory.getLogger(LangChainContentService.class);
    
    @Autowired
    private PromptService promptService;

    @Autowired
    private AzureOpenAiConfig azureConfig;
    
    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ChunkingService chunkingService;

    @Autowired
    private EmbeddingService embeddingService;
    
    @Autowired
    @Qualifier("contentGenerationExecutor")
    private Executor contentGenerationExecutor;
    
    // Memory optimization constants
    private static final int MEMORY_CHECK_INTERVAL = 10; // Check memory every N chunks
    private static final double MEMORY_THRESHOLD = 0.85; // 85% memory usage threshold
    private static final int ULTRA_SMALL_BATCH_SIZE = 3; // Ultra-small batch for memory-constrained environments
    private static final int MICRO_BATCH_SIZE = 1; // Process one chunk at a time
    private static final long GC_INTERVAL_MS = 5000; // Force GC every 5 seconds during heavy processing
    
    private static final int SUMMARY_CONTEXT_CHAR_LIMIT = 100_000;
    private static final int FLASHCARDS_CONTEXT_CHAR_LIMIT = 1_500_000;
    private static final int QUIZ_CONTEXT_CHAR_LIMIT = 1_500_000;
    private static final int CHAPTERS_CONTEXT_CHAR_LIMIT = 1_500_000;
    
    // Memory monitoring
    private final AtomicLong lastGCTime = new AtomicLong(0);
    private final AtomicInteger processedChunks = new AtomicInteger(0);
    private final Runtime runtime = Runtime.getRuntime();
    
    @Autowired
    private TransactionTemplate transactionTemplate;
    
    // All-in-one content generator
    @SystemMessage("You are an AI assistant that generates educational content in JSON format.")
    private interface UnifiedContentGenerator {
        String generateContent(String prompt);
    }
    
    // Keep individual interfaces for backward compatibility
    @SystemMessage("You are an educational assistant that creates concise, comprehensive summaries.")
    private interface SummaryGenerator {
        String generateSummary(String content);
    }
    
    @SystemMessage("You are an educational assistant that creates effective flashcards for learning.")
    private interface FlashcardGenerator {
        String generateFlashcards(String content);
    }
    
    @SystemMessage("You are an educational assistant that creates challenging but fair quiz questions.")
    private interface QuizGenerator {
        String generateQuiz(String content);
    }
    
    @SystemMessage("You are an educational assistant that organizes content into logical chapters or sections.")
    private interface ChapterGenerator {
        String generateChapters(String content);
    }
    
    /**
     * Initialize a chat model using Azure OpenAI
     */
    private ChatLanguageModel getChatModel() {
        return AzureOpenAiChatModel.builder()
                .endpoint(azureConfig.getAzureEndpoint())
                .apiKey(azureConfig.getAzureApiKey())
                .deploymentName(azureConfig.getChatDeployment())
                .temperature(0.4)
                .timeout(Duration.ofMinutes(2))
                .build();
    }
    
    /**
     * Generate all content types at once for a resource using a unified prompt
     */
    public Resource generateAllContent(Long resourceId) {
        long startTime = System.currentTimeMillis();  // start timing

        logger.info("Generating all content for resource: {}", resourceId);
        
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found with ID: " + resourceId));
        
        if (resource.getContent() == null || resource.getContent().isEmpty()) {
            throw new IllegalStateException("Resource content is empty. Extract content first.");
        }
        
        int contentLength = resource.getContent().length();
        logger.info("Document size: {} characters", contentLength);
        
        try {
            // Always use the unified/parallel approach, regardless of document size
            String content = handleLargeContent(resource.getContent(), 4000);
            ChatLanguageModel chatModel = getChatModel();
            UnifiedContentGenerator generator = AiServices.builder(UnifiedContentGenerator.class)
                    .chatLanguageModel(chatModel)
                    .build();
            
            String unifiedPrompt = createUnifiedPrompt(resource, content);
            logger.info("Sending unified content generation request");
            String generatedJson = generator.generateContent(unifiedPrompt);
            logger.info("Received response from unified content generation");
            
            String extractedJson = extractJsonObject(generatedJson);
            JSONObject allContent = new JSONObject(extractedJson);
            
            if (allContent.has("summary")) {
                String summary = allContent.getString("summary");
                logger.debug("Summary response from unified generation for resource {}: {}", resourceId, summary);
                logger.debug("Summary response length from unified generation for resource {}: {} characters", resourceId, summary.length());
                resource.setSummary(summary);
            }
            
            if (allContent.has("flashcards")) {
                JSONArray flashcardsArray = allContent.getJSONArray("flashcards");
                resource.setFlashcards(flashcardsArray.toString());
            }
            
            if (allContent.has("quiz")) {
                JSONArray quizArray = allContent.getJSONArray("quiz");
                resource.setQuiz(quizArray.toString());
            }
            
            if (allContent.has("chapters")) {
                JSONArray chaptersArray = allContent.getJSONArray("chapters");
                resource.setChapters(chaptersArray.toString());
            }
            
            resource.setGeneratedContent(allContent.toString());
            resourceRepository.save(resource);
            long contentEndTime = System.currentTimeMillis();

            logger.info("Successfully generated all content types in {} ms" ,(contentEndTime - startTime));
            try {
                processEmbeddings(resource);
            } catch (Exception e) {
                logger.error("Embedding processing failed: {}", e.getMessage(), e);
            }

            Resource savedResource = resourceRepository.save(resource);

            long endTime = System.currentTimeMillis();
            logger.info("Content generation completed in {} ms", (endTime - startTime));
            return savedResource;
        } catch (Exception e) {
            logger.error("Error in content generation: {}", e.getMessage(), e);
            logger.info("Falling back to individual content generation");

            long endTime = System.currentTimeMillis();
            logger.info("Content generation failed after {} ms, switching to fallback", (endTime - startTime));

            return generateAllContentFallback(resourceId);
        }
    }
    
    /**
     * Fallback method to generate content individually if unified approach fails
     */
    private Resource generateAllContentFallback(Long resourceId) {
        long startTime = System.currentTimeMillis();
        logger.info("Using parallel content generation for resource: {}", resourceId);
        logger.debug("🕐 Fallback content generation started at: {}", startTime);
        
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found with ID: " + resourceId));
        
        String content = resource.getContent();
        ChatLanguageModel chatModel = getChatModel();
        
        try {
            // Generate all content types in parallel
            CompletableFuture<String> summaryFuture = CompletableFuture.supplyAsync(() -> {
                long taskStartTime = System.currentTimeMillis();
                logger.info("Generating summary in parallel...");
                try {
                    SummaryGenerator summaryGenerator = AiServices.builder(SummaryGenerator.class)
                            .chatLanguageModel(chatModel)
                            .build();
                    String result = summaryGenerator.generateSummary(prepareSummaryPrompt(content, resource.getTitle(), resource.getClass().getSimpleName()));
                    long taskEndTime = System.currentTimeMillis();
                    logger.debug("📊 Summary generation took: {} ms", (taskEndTime - taskStartTime));
                    return result;
                } catch (Exception e) {
                    logger.error("Error generating summary: {}", e.getMessage());
                    long taskEndTime = System.currentTimeMillis();
                    logger.debug("📊 Summary generation failed after: {} ms", (taskEndTime - taskStartTime));
                    return "Error generating summary: " + e.getMessage();
                }
            }, contentGenerationExecutor);
            
            CompletableFuture<String> flashcardsFuture = CompletableFuture.supplyAsync(() -> {
                long taskStartTime = System.currentTimeMillis();
                logger.info("Generating flashcards in parallel...");
                try {
                    FlashcardGenerator flashcardGenerator = AiServices.builder(FlashcardGenerator.class)
                            .chatLanguageModel(chatModel)
                            .build();
                    String flashcardsJson = flashcardGenerator.generateFlashcards(prepareFlashcardsPrompt(content, resource.getTitle(), resource.getClass().getSimpleName()));
                    String result = validateAndCleanJson(flashcardsJson, "flashcards");
                    long taskEndTime = System.currentTimeMillis();
                    logger.debug("📊 Flashcards generation took: {} ms", (taskEndTime - taskStartTime));
                    return result;
                } catch (Exception e) {
                    logger.error("Error generating flashcards: {}", e.getMessage());
                    long taskEndTime = System.currentTimeMillis();
                    logger.debug("📊 Flashcards generation failed after: {} ms", (taskEndTime - taskStartTime));
                    return "[]";
                }
            }, contentGenerationExecutor);
            
            CompletableFuture<String> quizFuture = CompletableFuture.supplyAsync(() -> {
                long taskStartTime = System.currentTimeMillis();
                logger.info("Generating quiz in parallel...");
                try {
                    QuizGenerator quizGenerator = AiServices.builder(QuizGenerator.class)
                            .chatLanguageModel(chatModel)
                            .build();
                    String quizJson = quizGenerator.generateQuiz(prepareQuizPrompt(content, resource.getTitle(), resource.getClass().getSimpleName()));
                    String result = validateAndCleanJson(quizJson, "quiz");
                    long taskEndTime = System.currentTimeMillis();
                    logger.debug("📊 Quiz generation took: {} ms", (taskEndTime - taskStartTime));
                    return result;
                } catch (Exception e) {
                    logger.error("Error generating quiz: {}", e.getMessage());
                    long taskEndTime = System.currentTimeMillis();
                    logger.debug("📊 Quiz generation failed after: {} ms", (taskEndTime - taskStartTime));
                    return "[]";
                }
            }, contentGenerationExecutor);
            
            CompletableFuture<String> chaptersFuture = CompletableFuture.supplyAsync(() -> {
                long taskStartTime = System.currentTimeMillis();
                logger.info("Generating chapters in parallel...");
                try {
                    ChapterGenerator chapterGenerator = AiServices.builder(ChapterGenerator.class)
                            .chatLanguageModel(chatModel)
                            .build();
                    String chaptersJson = chapterGenerator.generateChapters(prepareChaptersPrompt(content, resource.getTitle(), resource.getClass().getSimpleName()));
                    String result = validateAndCleanJson(chaptersJson, "chapters");
                    long taskEndTime = System.currentTimeMillis();
                    logger.debug("📊 Chapters generation took: {} ms", (taskEndTime - taskStartTime));
                    return result;
                } catch (Exception e) {
                    logger.error("Error generating chapters: {}", e.getMessage());
                    long taskEndTime = System.currentTimeMillis();
                    logger.debug("📊 Chapters generation failed after: {} ms", (taskEndTime - taskStartTime));
                    return "[]";
                }
            }, contentGenerationExecutor);
            
            // Wait for all parallel tasks to complete
            logger.info("Waiting for all parallel content generation tasks to complete...");
            CompletableFuture<Void> allTasks = CompletableFuture.allOf(
                summaryFuture, flashcardsFuture, quizFuture, chaptersFuture
            );
            
            // Wait for completion with timeout
            try {
                allTasks.get(5, TimeUnit.MINUTES); // 5 minute timeout
            } catch (TimeoutException e) {
                logger.error("Content generation timed out after 5 minutes");
                throw new RuntimeException("Content generation timed out");
            } catch (Exception e) {
                logger.error("Error waiting for parallel content generation: {}", e.getMessage());
                throw new RuntimeException("Error in parallel content generation", e);
            }
            
            // Get results from all futures
            String summary = summaryFuture.get();
            String flashcards = flashcardsFuture.get();
            String quiz = quizFuture.get();
            String chapters = chaptersFuture.get();
            
            // Add debug logging for summary response
            logger.debug("Summary response from fallback generation for resource {}: {}", resourceId, summary);
            logger.debug("Summary response length from fallback generation for resource {}: {} characters", resourceId, summary.length());
            
            logger.info("All parallel content generation tasks completed successfully");
            
            // Set the generated content on the resource
            resource.setSummary(summary);
            resource.setFlashcards(flashcards);
            resource.setQuiz(quiz);
            resource.setChapters(chapters);
            
            // Create combined generated content
            JSONObject generatedContent = new JSONObject();
            generatedContent.put("summary", summary);
            
            try {
                generatedContent.put("flashcards", new JSONArray(flashcards));
            } catch (Exception e) {
                logger.warn("Error parsing flashcards as JSON array: {}", e.getMessage());
                generatedContent.put("flashcards", flashcards);
            }
            
            try {
                generatedContent.put("quiz", new JSONArray(quiz));
            } catch (Exception e) {
                logger.warn("Error parsing quiz as JSON array: {}", e.getMessage());
                generatedContent.put("quiz", quiz);
            }
            
            try {
                generatedContent.put("chapters", new JSONArray(chapters));
            } catch (Exception e) {
                logger.warn("Error parsing chapters as JSON array: {}", e.getMessage());
                generatedContent.put("chapters", chapters);
            }
            
            resource.setGeneratedContent(generatedContent.toString());
            
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            
            logger.info("Parallel content generation completed successfully for resource: {}", resourceId);
            logger.debug("⏱️ Total fallback content generation time: {} ms ({} seconds)", totalTime, totalTime / 1000.0);
            
            return resourceRepository.save(resource);
            
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            logger.error("Error in parallel content generation for resource {}: {}", resourceId, e.getMessage(), e);
            logger.debug("⏱️ Fallback content generation failed after: {} ms ({} seconds)", totalTime, totalTime / 1000.0);
            throw new RuntimeException("Failed to generate content in parallel", e);
        }
    }
    
    /**
     * Create a unified prompt for generating all content types at once
     */
    private String createUnifiedPrompt(Resource resource, String content) {
        String resourceType = resource.getClass().getSimpleName();
        String resourceTitle = resource.getTitle() != null ? resource.getTitle() : "Untitled Resource";
        String prompt;

        long startTime = System.currentTimeMillis();
        try {
            prompt = promptService.createContentGenerationPrompt(resourceTitle, resourceType, content);
            long endTime = System.currentTimeMillis();
            logger.info("Prompt generated successfully in {} ms", (endTime - startTime));
        } catch (IOException e) {
            long endTime = System.currentTimeMillis();
            logger.error("Error generating resource prompt after {} ms", (endTime - startTime), e);
            prompt = "Σφάλμα κατά τη δημιουργία του prompt. Παρακαλώ επικοινωνήστε με τον διαχειριστή.";
        }
        return prompt;
        /*return "You are an AI assistant. Your job is to generate four types of educational content from the given resource:\n\n" +
               "### QUIZ\n" +
               "- Generate a high-quality multiple-choice quiz from the provided content.\n" +
               "- Each question should test key concepts and ensure varying difficulty levels.\n" +
               "- Provide exactly **four** answer choices per question, with **only one correct answer**.\n" +
               "- The distractors should be **plausible but incorrect**.\n\n" +
               
               "### FLASHCARDS\n" +
               "- Generate educational flashcards based on the provided content.\n" +
               "- Each flashcard should focus on a key term, concept, or question from the material.\n" +
               "- Provide a **concise but clear answer**, and include a **helpful short hint**.\n\n" +
               
               "### SUMMARY\n" +
               "- Provide a ** extremely detailed and well-structured summary** of the content.\n" +
               "- The summary should be **rich in information**, capturing key concepts, important details, and examples where applicable.\n" +
               "- The point of this summary is not to describe waht the resource contains but to compress all the required information into a cohesive summary.\n"+
               "- Important information like formulas or definitions should be included. The students should be able to get the entire knowledge of the resource by this summary.\n"+
               "- Aim for **at least 20 sentences**, ensuring completeness without excessive verbosity.\n" +
               "- Format the summary in **Markdown**, using bullet points, headings, and emphasis where necessary.\n\n" +
               
               "### CHAPTER SPLITTING\n" +
               "- If the content appears to contain natural divisions (e.g., chapters, sections, headings), **split the resource into chapters**.\n" +
               "- Use any obvious headings or breakpoints in the text (e.g., 'Chapter 1', 'Section 2', or heading-like formatting) to determine splits.\n" +
               "- If the text already has chapters use them.\n" +
               "- If no headings exist, attempt to split the content into **logical parts of roughly equal length** (e.g., every 1,000–1,500 words).\n" +
               "- For each chapter, include:\n" +
               "  - A **title** (use the heading if available, or create one based on the content).\n" +
               "  - A **short_description** (7–10 sentences summarizing what the chapter is about).\n" +
               "  - The **character index** corresponding to the start of the chapter (start_index).\n" +
               "  - The **character index** corresponding to the end of the chapter (end_index).\n\n" +
               
               "### RELATIONS\n" +
               "- Extract **key** relations between concepts in the text. These will be used to generate a mind map.\n" +
               "- Each relation should show a meaningful conceptual link between two key ideas.\n" +
               "- Ensure the set of relations covers the **entire content**.\n\n" +
               
               "---\n\n" +
               "The user has given you a resource of type: **" + resourceType + "**.\n" +
               "The title of the resource is: **" + resourceTitle + "**.\n" +
               "The content of the resource is:\n\n" + content + "\n\n\n" +
               
               "### Output Format\n" +
               "- Return your response in **strict JSON format** with the following structure:\n" +
               "{\n" +
               "  \"quiz\": [{ \"question\": \"\", \"options\": [\"\", \"\", \"\", \"\"], \"answer\": \"\" }],\n" +
               "  \"flashcards\": [{ \"question\": \"\", \"answer\": \"\", \"hint\": \"\" }],\n" +
               "  \"summary\": \"\",\n" +
               "  \"chapters\": [\n" +
               "    { \"title\": \"\", \"short_description\": \"\", \"start_index\": 0, \"end_index\": 1000 },\n" +
               "    { \"title\": \"\", \"short_description\": \"\", \"start_index\": 1001, \"end_index\": 2500 }\n" +
               "  ],\n" +
               "  \"relations\": [\n" +
               "    { \"concept1\": \"\", \"concept2\": \"\" },\n" +
               "    { \"concept1\": \"\", \"concept2\": \"\" }\n" +
               "  ]\n" +
               "}\n" +
               "\n" +
               "- **Do not include any extra text, markdown, or explanations. Only return a valid JSON object. Make sure to respond in greek.**";*/
    }
    
    /**
     * Extract a JSON object from text that might contain markdown or other formatting
     */
    private String extractJsonObject(String text) {
        // First check if the text is already a valid JSON object
        try {
            new JSONObject(text);
            return text; // It's already valid JSON
        } catch (Exception e) {
            logger.info("Response is not directly parseable as JSON, attempting extraction");
        }
        
        // Look for JSON object pattern
        int startIndex = text.indexOf('{');
        int endIndex = text.lastIndexOf('}');
        
        if (startIndex >= 0 && endIndex > startIndex) {
            String extracted = text.substring(startIndex, endIndex + 1);
            try {
                // Validate the extracted text as JSON
                new JSONObject(extracted);
                return extracted;
            } catch (Exception e) {
                logger.warn("Extracted text is not valid JSON: {}", e.getMessage());
            }
        }
        
        // Look for JSON in code blocks
        if (text.contains("```json")) {
            startIndex = text.indexOf("```json") + 7;
            endIndex = text.indexOf("```", startIndex);
            if (endIndex > startIndex) {
                String extracted = text.substring(startIndex, endIndex).trim();
                try {
                    // Validate the extracted text as JSON
                    new JSONObject(extracted);
                    return extracted;
                } catch (Exception e) {
                    logger.warn("JSON from code block is not valid: {}", e.getMessage());
                }
            }
        } else if (text.contains("```")) {
            startIndex = text.indexOf("```") + 3;
            // Skip the first line if it doesn't start with {
            if (!text.substring(startIndex).trim().startsWith("{")) {
                startIndex = text.indexOf("\n", startIndex) + 1;
            }
            endIndex = text.indexOf("```", startIndex);
            if (endIndex > startIndex) {
                String extracted = text.substring(startIndex, endIndex).trim();
                try {
                    // Validate the extracted text as JSON
                    new JSONObject(extracted);
                    return extracted;
                } catch (Exception e) {
                    logger.warn("Code block is not valid JSON: {}", e.getMessage());
                }
            }
        }
        
        throw new IllegalArgumentException("Could not extract a valid JSON object from the response");
    }
    
    /**
     * Validate and clean JSON strings returned from LLM
     */
    private String validateAndCleanJson(String jsonString, String contentType) {
        try {
            // Try to parse directly first
            new JSONArray(jsonString);
            return jsonString; // If it parses correctly, return as is
        } catch (Exception initialParseException) {
            logger.warn("{} content initial parse failed: {}", contentType, initialParseException.getMessage());
            
            try {
                // Try to extract JSON array from the response
                String extracted = extractJsonArray(jsonString);
                // Validate the extracted JSON
                new JSONArray(extracted);
                logger.info("Successfully extracted and validated {} JSON", contentType);
                return extracted;
            } catch (Exception e) {
                logger.error("Failed to extract valid JSON for {}: {}", contentType, e.getMessage());
                // Return empty array as fallback
                return "[]";
            }
        }
    }
    
    /**
     * Extract JSON array from text that might contain markdown or other formatting
     */
    private String extractJsonArray(String text) {
        // Look for array pattern
        int startIndex = text.indexOf('[');
        int endIndex = text.lastIndexOf(']');
        
        if (startIndex >= 0 && endIndex > startIndex) {
            return text.substring(startIndex, endIndex + 1);
        }
        
        // Look for common markdown code block patterns
        if (text.contains("```json")) {
            startIndex = text.indexOf("```json") + 7;
            endIndex = text.indexOf("```", startIndex);
            if (endIndex > startIndex) {
                String extracted = text.substring(startIndex, endIndex).trim();
                // Make sure we got a JSON array
                if (extracted.startsWith("[") && extracted.endsWith("]")) {
                    return extracted;
                }
            }
        } else if (text.contains("```")) {
            startIndex = text.indexOf("```") + 3;
            // Skip the first line if it doesn't start with [
            if (!text.substring(startIndex).trim().startsWith("[")) {
                startIndex = text.indexOf("\n", startIndex) + 1;
            }
            endIndex = text.indexOf("```", startIndex);
            if (endIndex > startIndex) {
                String extracted = text.substring(startIndex, endIndex).trim();
                // Make sure we got a JSON array
                if (extracted.startsWith("[") && extracted.endsWith("]")) {
                    return extracted;
                }
            }
        }
        
        throw new IllegalArgumentException("Could not extract a valid JSON array from the text");
    }
    
    /**
     * Handle large content by splitting it into manageable chunks with semantic awareness
     */
    private String handleLargeContent(String content, int maxTokens) {
        if (content.length() <= maxTokens * 4) {  
            return content;
        }
        
        Document document = Document.from(content);
        
        // Use semantic-aware splitting that respects natural boundaries
        DocumentSplitter splitter = DocumentSplitters.recursive(maxTokens / 3, 200);
        List<TextSegment> segments = splitter.split(document);
        
        StringBuilder reducedContent = new StringBuilder();
        
        // Always include beginning segments (introduction/context)
        int introSegments = Math.min(4, segments.size());
        for (int i = 0; i < introSegments; i++) {
            reducedContent.append(segments.get(i).text()).append("\n\n");
        }
        
        // Include segments from the middle (main content) - more intelligently selected
        if (segments.size() > 8) {
            int midStartIdx = segments.size() / 3;
            int midSegments = Math.min(4, segments.size() - midStartIdx); // Increased from 3 to 4
            reducedContent.append("... [content continues] ...\n\n");
            for (int i = 0; i < midSegments; i++) {
                reducedContent.append(segments.get(midStartIdx + i).text()).append("\n\n");
            }
        }
        
        // Include segments from the end (conclusion/summary) - more intelligently selected
        if (segments.size() > 6) {
            int endStartIdx = Math.max(introSegments, segments.size() - 4); // Increased from 3 to 4
            reducedContent.append("... [content continues] ...\n\n");
            for (int i = endStartIdx; i < segments.size(); i++) {
                reducedContent.append(segments.get(i).text()).append("\n\n");
            }
        }
        
        logger.info("Processed content from beginning, middle, and end with semantic awareness. Final length: {}", reducedContent.length());
        return reducedContent.toString();
    }

    private Resource processVeryLargeDocument(Resource resource) {
        logger.debug("Using map-reduce approach for very large document: {}", resource.getId());
        
        long startTime = System.currentTimeMillis();
        
        // Split the document into major sections
        String content = resource.getContent();
        long splittingStartTime = System.currentTimeMillis();
        List<TextSegment> sections = splitIntoMajorSections(content);
        long splittingEndTime = System.currentTimeMillis();
        long splittingTime = splittingEndTime - splittingStartTime;
        logger.debug("Split document into {} major sections in {} ms", sections.size(), splittingTime);
        
        // Create all futures for all sections and all tasks in parallel
        List<CompletableFuture<String>> allSummaryFutures = new ArrayList<>();
        List<CompletableFuture<String>> allFlashcardsFutures = new ArrayList<>();
        List<CompletableFuture<String>> allQuizFutures = new ArrayList<>();
        List<CompletableFuture<String>> allChaptersFutures = new ArrayList<>();
        
        long parallelStartTime = System.currentTimeMillis();
        
        // Start all tasks for all sections simultaneously
        for (int i = 0; i < sections.size(); i++) {
            final int sectionIndex = i;
            TextSegment section = sections.get(i);
            String sectionContent = section.text();
            ChatLanguageModel chatModel = getChatModel();
            
            // Add delay between sections to prevent Azure rate limiting
            if (i > 0) {
                /*try {
                    Thread.sleep(1000); // 1 second delay between sections
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }*/
            }
            
            // Start all 4 tasks for this section in parallel
            CompletableFuture<String> summaryFuture = CompletableFuture.supplyAsync(() -> {
                long taskStartTime = System.currentTimeMillis();
                logger.debug("Starting summary generation for section {}/{}", sectionIndex + 1, sections.size());
                
                try {
                    // Add small delay to prevent overwhelming Azure API
                    //Thread.sleep(500);
                    
                    SummaryGenerator summaryGenerator = AiServices.builder(SummaryGenerator.class)
                            .chatLanguageModel(chatModel)
                            .build();
                    String result = summaryGenerator.generateSummary(prepareSummaryPrompt(sectionContent, resource.getTitle(), resource.getClass().getSimpleName()));
                    
                    long taskEndTime = System.currentTimeMillis();
                    logger.debug("Summary for section {}/{} completed in {} ms", sectionIndex + 1, sections.size(), taskEndTime - taskStartTime);
                    return result;
                } catch (Exception e) {
                    logger.error("Error generating summary for section {}/{}: {}", sectionIndex + 1, sections.size(), e.getMessage());
                    return "Error generating summary: " + e.getMessage();
                }
            }, contentGenerationExecutor);
            
            CompletableFuture<String> flashcardsFuture = CompletableFuture.supplyAsync(() -> {
                long taskStartTime = System.currentTimeMillis();
                logger.debug("Starting flashcards generation for section {}/{}", sectionIndex + 1, sections.size());
                
                try {
                    // Add small delay to prevent overwhelming Azure API
                    //Thread.sleep(1000);
                    
                    FlashcardGenerator flashcardGenerator = AiServices.builder(FlashcardGenerator.class)
                            .chatLanguageModel(chatModel)
                            .build();
                    String flashcardsJson = flashcardGenerator.generateFlashcards(prepareFlashcardsPrompt(sectionContent, resource.getTitle(), resource.getClass().getSimpleName()));
                    String result = validateAndCleanJson(flashcardsJson, "flashcards");
                    
                    long taskEndTime = System.currentTimeMillis();
                    logger.debug("Flashcards for section {}/{} completed in {} ms", sectionIndex + 1, sections.size(), taskEndTime - taskStartTime);
                    return result;
                } catch (Exception e) {
                    logger.error("Error generating flashcards for section {}/{}: {}", sectionIndex + 1, sections.size(), e.getMessage());
                    return "[]";
                }
            }, contentGenerationExecutor);
            
            CompletableFuture<String> quizFuture = CompletableFuture.supplyAsync(() -> {
                long taskStartTime = System.currentTimeMillis();
                logger.debug("Starting quiz generation for section {}/{}", sectionIndex + 1, sections.size());
                
                try {
                    // Add small delay to prevent overwhelming Azure API
                    //Thread.sleep(1500);
                    
                    QuizGenerator quizGenerator = AiServices.builder(QuizGenerator.class)
                            .chatLanguageModel(chatModel)
                            .build();
                    String quizJson = quizGenerator.generateQuiz(prepareQuizPrompt(sectionContent, resource.getTitle(), resource.getClass().getSimpleName()));
                    String result = validateAndCleanJson(quizJson, "quiz");
                    
                    long taskEndTime = System.currentTimeMillis();
                    logger.debug("Quiz for section {}/{} completed in {} ms", sectionIndex + 1, sections.size(), taskEndTime - taskStartTime);
                    return result;
                } catch (Exception e) {
                    logger.error("Error generating quiz for section {}/{}: {}", sectionIndex + 1, sections.size(), e.getMessage());
                    return "[]";
                }
            }, contentGenerationExecutor);
            
            CompletableFuture<String> chaptersFuture = CompletableFuture.supplyAsync(() -> {
                long taskStartTime = System.currentTimeMillis();
                logger.debug("Starting chapters generation for section {}/{}", sectionIndex + 1, sections.size());
                
                try {
                    // Add small delay to prevent overwhelming Azure API
                    //Thread.sleep(2000);
                    
                    ChapterGenerator chapterGenerator = AiServices.builder(ChapterGenerator.class)
                            .chatLanguageModel(chatModel)
                            .build();
                    String chaptersJson = chapterGenerator.generateChapters(prepareChaptersPrompt(sectionContent, resource.getTitle(), resource.getClass().getSimpleName()));
                    String result = validateAndCleanJson(chaptersJson, "chapters");
                    
                    long taskEndTime = System.currentTimeMillis();
                    logger.debug("Chapters for section {}/{} completed in {} ms", sectionIndex + 1, sections.size(), taskEndTime - taskStartTime);
                    return result;
                } catch (Exception e) {
                    logger.error("Error generating chapters for section {}/{}: {}", sectionIndex + 1, sections.size(), e.getMessage());
                    return "[]";
                }
            }, contentGenerationExecutor);
            
            // Add all futures to their respective lists
            allSummaryFutures.add(summaryFuture);
            allFlashcardsFutures.add(flashcardsFuture);
            allQuizFutures.add(quizFuture);
            allChaptersFutures.add(chaptersFuture);
        }
        
        // Wait for all tasks to complete
        long waitStartTime = System.currentTimeMillis();
        long parallelProcessingTime = 0; // Declare outside try block
        CompletableFuture<Void> allSummaryTasks = CompletableFuture.allOf(allSummaryFutures.toArray(new CompletableFuture[0]));
        CompletableFuture<Void> allFlashcardsTasks = CompletableFuture.allOf(allFlashcardsFutures.toArray(new CompletableFuture[0]));
        CompletableFuture<Void> allQuizTasks = CompletableFuture.allOf(allQuizFutures.toArray(new CompletableFuture[0]));
        CompletableFuture<Void> allChaptersTasks = CompletableFuture.allOf(allChaptersFutures.toArray(new CompletableFuture[0]));
        
        // Save content types immediately as they complete, without waiting for others
        CompletableFuture<Void> saveFlashcardsTask = allFlashcardsTasks.thenRunAsync(() -> {
            long saveStartTime = System.currentTimeMillis();
            try {
                JSONArray allFlashcards = new JSONArray();
                for (CompletableFuture<String> future : allFlashcardsFutures) {
                    try {
                        String flashcards = future.get();
                        try {
                            JSONArray sectionFlashcards = new JSONArray(flashcards);
                            for (int j = 0; j < sectionFlashcards.length(); j++) {
                                allFlashcards.put(sectionFlashcards.getJSONObject(j));
                            }
                        } catch (Exception e) {
                            logger.warn("Error parsing flashcards: {}", e.getMessage());
                        }
                    } catch (Exception e) {
                        logger.warn("Error getting flashcards future: {}", e.getMessage());
                    }
                }
                resource.setFlashcards(allFlashcards.toString());
                resourceRepository.save(resource);
                long saveEndTime = System.currentTimeMillis();
                logger.debug("Flashcards saved immediately in {} ms", saveEndTime - saveStartTime);
            } catch (Exception e) {
                logger.error("Error saving flashcards immediately: {}", e.getMessage());
            }
        }, contentGenerationExecutor);
        
        CompletableFuture<Void> saveQuizTask = allQuizTasks.thenRunAsync(() -> {
            long saveStartTime = System.currentTimeMillis();
            try {
                JSONArray allQuizQuestions = new JSONArray();
                for (CompletableFuture<String> future : allQuizFutures) {
                    try {
                        String quiz = future.get();
                        try {
                            JSONArray sectionQuiz = new JSONArray(quiz);
                            for (int j = 0; j < sectionQuiz.length(); j++) {
                                allQuizQuestions.put(sectionQuiz.getJSONObject(j));
                            }
                        } catch (Exception e) {
                            logger.warn("Error parsing quiz: {}", e.getMessage());
                        }
                    } catch (Exception e) {
                        logger.warn("Error getting quiz future: {}", e.getMessage());
                    }
                }
                resource.setQuiz(allQuizQuestions.toString());
                resourceRepository.save(resource);
                long saveEndTime = System.currentTimeMillis();
                logger.debug("Quiz saved immediately in {} ms", saveEndTime - saveStartTime);
            } catch (Exception e) {
                logger.error("Error saving quiz immediately: {}", e.getMessage());
            }
        }, contentGenerationExecutor);
        
        CompletableFuture<Void> saveChaptersTask = allChaptersTasks.thenRunAsync(() -> {
            long saveStartTime = System.currentTimeMillis();
            try {
                JSONArray allChapters = new JSONArray();
                for (int i = 0; i < allChaptersFutures.size(); i++) {
                    try {
                        String chapters = allChaptersFutures.get(i).get();
                        try {
                            JSONArray sectionChapters = new JSONArray(chapters);
                            for (int j = 0; j < sectionChapters.length(); j++) {
                                // Adjust indices to account for position in the overall document
                                JSONObject chapter = sectionChapters.getJSONObject(j);
                                if (chapter.has("start_index") && chapter.has("end_index")) {
                                    int baseIndex = getBaseIndex(sections, i);
                                    int startIndex = chapter.getInt("start_index") + baseIndex;
                                    int endIndex = chapter.getInt("end_index") + baseIndex;
                                    
                                    chapter.put("start_index", startIndex);
                                    chapter.put("end_index", endIndex);
                                }
                                allChapters.put(chapter);
                            }
                        } catch (Exception e) {
                            logger.warn("Error parsing chapters for section {}: {}", i + 1, e.getMessage());
                        }
                    } catch (Exception e) {
                        logger.warn("Error getting chapters future for section {}: {}", i + 1, e.getMessage());
                    }
                }
                resource.setChapters(allChapters.toString());
                resourceRepository.save(resource);
                long saveEndTime = System.currentTimeMillis();
                logger.debug("Chapters saved immediately in {} ms", saveEndTime - saveStartTime);
            } catch (Exception e) {
                logger.error("Error saving chapters immediately: {}", e.getMessage());
            }
        }, contentGenerationExecutor);
        
        // Wait for all tasks to complete (including immediate saves)
        CompletableFuture<Void> allTasks = CompletableFuture.allOf(allSummaryTasks, saveFlashcardsTask, saveQuizTask, saveChaptersTask);
        
        try {
            allTasks.get(10, TimeUnit.MINUTES); // 10 minute timeout for all processing
            long waitEndTime = System.currentTimeMillis();
            parallelProcessingTime = waitEndTime - parallelStartTime;
            long waitTime = waitEndTime - waitStartTime;
            logger.debug("All {} sections with {} tasks each completed in {} ms (wait time: {} ms)", 
                        sections.size(), 4, parallelProcessingTime, waitTime);
        } catch (TimeoutException e) {
            long waitEndTime = System.currentTimeMillis();
            parallelProcessingTime = waitEndTime - parallelStartTime;
            logger.error("All tasks processing timed out after {} ms", parallelProcessingTime);
            throw new RuntimeException("Large document processing timed out");
        } catch (Exception e) {
            long waitEndTime = System.currentTimeMillis();
            parallelProcessingTime = waitEndTime - parallelStartTime;
            logger.error("Error waiting for all tasks after {} ms: {}", parallelProcessingTime, e.getMessage());
            throw new RuntimeException("Error in large document processing", e);
        }
        
        // Collect summary results for final summary generation
        long collectionStartTime = System.currentTimeMillis();
        List<String> sectionSummaries = new ArrayList<>();
        
        // Collect results from summary futures
        for (int i = 0; i < sections.size(); i++) {
            try {
                String summary = allSummaryFutures.get(i).get();
                sectionSummaries.add(summary);
            } catch (Exception e) {
                logger.error("Error collecting summary from section {}: {}", i + 1, e.getMessage());
            }
        }
        
        long collectionEndTime = System.currentTimeMillis();
        long collectionTime = collectionEndTime - collectionStartTime;
        logger.debug("Collected summaries from {} sections in {} ms", sections.size(), collectionTime);
        
        // EXTRA SUMMARY STEP: Combine all section summaries into a final comprehensive summary
        long finalSummaryStartTime = System.currentTimeMillis();
        long totalTimeToSummary = finalSummaryStartTime - startTime;
        logger.debug("Starting final summary combination step for {} sections (total time elapsed: {} ms)", 
                    sectionSummaries.size(), totalTimeToSummary);
        
        String finalSummary;
        if (sectionSummaries.size() > 1) {
            // Combine section summaries and create a final comprehensive summary
            String combinedSections = String.join("\n\n", sectionSummaries);
            
            ChatLanguageModel chatModel = getChatModel();
            SummaryGenerator finalGenerator = AiServices.builder(SummaryGenerator.class)
                    .chatLanguageModel(chatModel)
                    .build();
            
            String finalPrompt;
            try {
                finalPrompt = promptService.createFinalSummaryWrapUpPrompt(resource.getTitle(), resource.getClass().getSimpleName(), combinedSections);
            } catch (Exception e) {
                logger.error("Error loading final summary wrap-up prompt, using fallback", e);
                finalPrompt = "You are given a set of section summaries. Combine and summarize them into a single, cohesive, comprehensive summary for the entire document. Eliminate redundancy, ensure logical flow, and preserve all important information.\n\nSection Summaries:\n" + combinedSections;
            }
            finalSummary = finalGenerator.generateSummary(finalPrompt);
            
            logger.debug("Final summary response from map-reduce generation for resource {}: {}", resource.getId(), finalSummary);
            logger.debug("Final summary response length from map-reduce generation for resource {}: {} characters", resource.getId(), finalSummary.length());
        } else if (sectionSummaries.size() == 1) {
            // Only one section, use its summary directly
            finalSummary = sectionSummaries.get(0);
            logger.debug("Single section document, using section summary directly");
        } else {
            // No sections processed, create empty summary
            finalSummary = "Error: No content was processed for this document.";
            logger.warn("No sections were processed successfully");
        }
        
        long finalSummaryEndTime = System.currentTimeMillis();
        long finalSummaryTime = finalSummaryEndTime - finalSummaryStartTime;
        long totalTimeToSummaryComplete = finalSummaryEndTime - startTime;
        logger.debug("Final summary combination completed in {} ms (total time elapsed: {} ms)", 
                    finalSummaryTime, totalTimeToSummaryComplete);
        
        // Update with final summary
        long finalSaveStartTime = System.currentTimeMillis();
        resource.setSummary(finalSummary);
        
        // Create complete generated content
        JSONObject generatedContent = new JSONObject();
        generatedContent.put("summary", finalSummary);
        generatedContent.put("flashcards", resource.getFlashcards());
        generatedContent.put("quiz", resource.getQuiz());
        generatedContent.put("chapters", resource.getChapters());
        
        resource.setGeneratedContent(generatedContent.toString());
        resourceRepository.save(resource);
        
        long finalSaveEndTime = System.currentTimeMillis();
        long finalSaveTime = finalSaveEndTime - finalSaveStartTime;
        logger.debug("Final save with summary completed in {} ms", finalSaveTime);
        
        // Process embeddings
        long embeddingStartTime = System.currentTimeMillis();
        try {
            processEmbeddings(resource);
            long embeddingEndTime = System.currentTimeMillis();
            long embeddingTime = embeddingEndTime - embeddingStartTime;
            logger.debug("Embedding processing completed in {} ms", embeddingTime);
        } catch (Exception e) {
            long embeddingEndTime = System.currentTimeMillis();
            long embeddingTime = embeddingEndTime - embeddingStartTime;
            logger.error("Embedding processing failed after {} ms: {}", embeddingTime, e.getMessage(), e);
        }
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        logger.debug("Map-reduce content generation completed in {} ms ({} seconds)", totalTime, totalTime / 1000.0);
        logger.debug("Timing breakdown:");
        logger.debug("  - Document splitting: {} ms", splittingTime);
        logger.debug("  - Parallel processing (all sections + all tasks): {} ms", parallelProcessingTime);
        logger.debug("  - Result collection: {} ms", collectionTime);
        logger.debug("  - Immediate save (flashcards/chapters/quiz): {} ms", parallelProcessingTime);
        logger.debug("  - Final summary generation: {} ms", finalSummaryTime);
        logger.debug("  - Final save: {} ms", finalSaveTime);
        logger.debug("GRAND TOTAL TIME ELAPSED: {} ms ({} seconds)", totalTime, totalTime / 1000.0);
        
        return resource;
    }
    
    /**
     * Calculate the base character index for a section
     */
    private int getBaseIndex(List<TextSegment> sections, int sectionIndex) {
        int baseIndex = 0;
        for (int i = 0; i < sectionIndex; i++) {
            baseIndex += sections.get(i).text().length();
        }
        return baseIndex;
    }
    
    /** 
     * Split document into major sections for map-reduce processing
     */
    private List<TextSegment> splitIntoMajorSections(String content) {
        // Use a larger chunk size for major sections
        DocumentSplitter splitter = DocumentSplitters.recursive(60000, 2000);
        Document document = Document.from(content);
        return splitter.split(document);
    }
    
    /**
     * Create a prompt for processing an individual document section
     */
    private String createSectionPrompt(Resource resource, String sectionText, int sectionNumber, int totalSections) {
        String resourceType = resource.getClass().getSimpleName();
        String resourceTitle = resource.getTitle() != null ? resource.getTitle() : "Untitled Resource";
        
        String prompt;
        try {
            prompt = promptService.createLargeContentGenerationPrompt(resourceTitle, resourceType, totalSections, sectionNumber, sectionText);
        } catch (IOException e) {
            logger.error("Error generating resource prompt", e);
            prompt = "Error generating resource prompt";
        }
        return prompt;
        /*return "You are an AI assistant processing Section " + sectionNumber + " of " + totalSections + 
               " from a document. Generate educational content from this section:\n\n" +
               
               "### QUIZ\n" +
               "- Generate a high-quality multiple-choice quiz from this section.\n" +
               "- Each question should test key concepts in this section only.\n" +
               "- Provide exactly **four** answer choices per question, with **only one correct answer**.\n\n" +
               
               "### FLASHCARDS\n" +
               "- Generate educational flashcards for key terms and concepts in this section.\n" +
               "- Each flashcard should focus on a concept from this section only.\n" +
               "- Provide a concise answer and helpful hint.\n\n" +
               
               "## SUMMARY\n" +
               "- Provide a ** extremely detailed and well-structured summary** of the content of this section only.\n" +
               "- The summary should be **rich in information**, capturing key concepts, important details, and examples where applicable.\n" +
               "- The point of this summary is not to describe waht the resource contains but to compress all the required information into a cohesive summary.\n"+
               "- Important information like formulas or definitions should be included. The students should be able to get the entire knowledge of the resource by this summary.\n"+
               "- Aim for **at least 20 sentences**, ensuring completeness without excessive verbosity.\n" +
               "- Format the summary in **Markdown**, using bullet points, headings, and emphasis where necessary.\n\n" +
               
               "### RELATIONS\n" +
               "- Extract key concept relations from this section only.\n\n" +
               
               "---\n\n" +
               "Resource type: **" + resourceType + "**\n" +
               "Resource title: **" + resourceTitle + "**\n" +
               "Section " + sectionNumber + " of " + totalSections + ":\n\n" + 
               sectionText + "\n\n" +
               
               "Return as strict JSON in this format:\n" +
               "{\n" +
               "  \"quiz\": [{ \"question\": \"\", \"options\": [\"\", \"\", \"\", \"\"], \"answer\": \"\" }],\n" +
               "  \"flashcards\": [{ \"question\": \"\", \"answer\": \"\", \"hint\": \"\" }],\n" +
               "  \"summary\": \"\",\n" +
               "  \"chapters\": [{ \"title\": \"\", \"short_description\": \"\", \"start_index\": 0, \"end_index\": 1000 }],\n" +
               "  \"relations\": [{ \"concept1\": \"\", \"concept2\": \"\" }]\n" +
               "}\n" +
               "Only return valid JSON without any extra text, markdown, or explanations."+
               "Only respond in Greek.";*/
    }
    
    // The individual prompt methods are kept for backward compatibility
    
    private String prepareSummaryPrompt(String content, String resourceTitle, String resourceType) {
        String truncatedContent = handleLargeContent(content, 3000);
        
        try {
            return promptService.createSummaryGenerationPrompt(resourceTitle, resourceType, truncatedContent);
        } catch (IOException e) {
            logger.error("Error loading summary generation prompt", e);
            // Fallback to hardcoded prompt
            PromptTemplate template = PromptTemplate.from(
                "- Provide a **detailed and well-structured summary** of the content.\n" +
                "- The summary should be **rich in information**, capturing key concepts, important details, and examples where applicable.\n" +
                "- Aim for **at least 12 sentences**, ensuring completeness without excessive verbosity.\n" +
                "- Format the summary in **Markdown**, using bullet points, headings, and emphasis where necessary.\n\n" +
                "Text to summarize:\n{{content}}"
            );
            return template.apply(Map.of("content", truncatedContent)).text();
        }
    }
    
    private String prepareFlashcardsPrompt(String content, String resourceTitle, String resourceType) {
        String truncatedContent = handleLargeContent(content, 2500);

        try {
            return promptService.createFlashcardsGenerationPrompt(resourceTitle, resourceType, truncatedContent);
        } catch (IOException e) {
            logger.error("Error loading flashcards generation prompt", e);
            // Fallback to hardcoded prompt
            PromptTemplate template = PromptTemplate.from(
                "You are an AI assistant. Your job is to generate educational flashcards based on the provided content.\n\n" +
                "### FLASHCARDS\n" +
                "- Each flashcard should focus on a key term, concept, or question from the material.\n" +
                "- Provide a **concise but clear answer**, and include a **helpful short hint**.\n" +
                "- Ensure flashcards span a broad range of the material and reflect important learning points.\n\n" +
                "---\n\n" +
                "The content of the resource is:\n\n{{content}}\n\n" +
                "### Output Format\n" +
                "- Return your response in **strict JSON format** like this:\n" +
                "[\n" +
                "  { \"question\": \"\", \"answer\": \"\", \"hint\": \"\" },\n" +
                "  { \"question\": \"\", \"answer\": \"\", \"hint\": \"\" }\n" +
                "]\n" +
                "- **Do not include any extra text, markdown, or explanations. Make sure to respond in Greek.**"
            );
            return template.apply(Map.of("content", truncatedContent)).text();
        }
    }

    
    private String prepareQuizPrompt(String content, String resourceTitle, String resourceType) {
        String truncatedContent = handleLargeContent(content, 2500);

        try {
            return promptService.createQuizGenerationPrompt(resourceTitle, resourceType, truncatedContent);
        } catch (IOException e) {
            logger.error("Error loading quiz generation prompt", e);
            // Fallback to hardcoded prompt
            PromptTemplate template = PromptTemplate.from(
                "You are an AI assistant. Your job is to generate a high-quality multiple-choice quiz from the provided content.\n\n" +
                "### QUIZ INSTRUCTIONS\n" +
                "- Each question should test key concepts from the material.\n" +
                "- Vary the difficulty level across questions (easy, medium, hard).\n" +
                "- Provide **exactly four** answer choices per question.\n" +
                "- Ensure **only one** of the four answers is correct.\n" +
                "- The incorrect answers (distractors) should be **plausible but incorrect**.\n" +
                "- Use **clear, direct language** in both questions and answers.\n\n" +
                "---\n\n" +
                "The content of the resource is:\n\n{{content}}\n\n" +
                "### Output Format\n" +
                "- Return your response in **strict JSON format** like this:\n" +
                "[\n" +
                "  {\n" +
                "    \"question\": \"\",\n" +
                "    \"options\": [\"\", \"\", \"\", \"\"],\n" +
                "    \"answer\": \"\"\n" +
                "  }\n" +
                "]\n" +
                "- **Do not include any extra text, markdown, or explanations. Make sure to respond in Greek.**"
            );
            return template.apply(Map.of("content", truncatedContent)).text();
        }
    }

    
    private String prepareChaptersPrompt(String content, String resourceTitle, String resourceType) {
        String truncatedContent = handleLargeContent(content, 100000);
        
        try {
            return promptService.createChaptersGenerationPrompt(resourceTitle, resourceType, truncatedContent);
        } catch (IOException e) {
            logger.error("Error loading chapters generation prompt", e);
            // Fallback to hardcoded prompt
            PromptTemplate template = PromptTemplate.from(
                "Divide the following text into logical chapters or sections. Each chapter should have " +
                "a title and content. Return the chapters as a JSON array of objects with 'title' and 'content' fields. " +
                "Format the output as a JSON array without any markdown or code blocks - just pure JSON.\n\n" +
                "Text to divide into chapters:\n{{content}}\n\n" +
                "Example format:\n[{\"title\":\"Chapter 1 Title\",\"content\":\"Chapter 1 content\"},{\"title\":\"Chapter 2 Title\",\"content\":\"Chapter 2 content\"}]"
            );
            return template.apply(Map.of("content", truncatedContent)).text();
        }
    }
    
    public String generateSummary(Long resourceId) {
        long startTime = System.currentTimeMillis();
        ChatLanguageModel chatModel = getChatModel();
        SummaryGenerator generator = AiServices.builder(SummaryGenerator.class)
                .chatLanguageModel(chatModel)
                .build();
        logger.info("[ContentGen] Starting summary generation for resource: {}", resourceId);
        try {
            Resource resource = resourceRepository.findById(resourceId)
                    .orElseThrow(() -> new RuntimeException("Resource not found with ID: " + resourceId));
            String content = resource.getContent();
            if (content == null || content.isEmpty()) {
                throw new IllegalStateException("Resource content is empty. Extract content first.");
            }
            if (content.length() <= SUMMARY_CONTEXT_CHAR_LIMIT) {
                String summary = generator.generateSummary(prepareSummaryPrompt(content, resource.getTitle(), resource.getClass().getSimpleName()));
                transactionTemplate.execute(status -> {
                    resourceRepository.updateSummaryById(resourceId, summary);
                    return null;
                });
                logger.info("[ContentGen] Finished summary generation for resource: {} in {} ms", resourceId, System.currentTimeMillis() - startTime);
                return summary;
            } else {
                logger.info("[ContentGen] Splitting content into sections for summary generation (length: {})", content.length());
                List<String> sections = splitIntoLargeSections(content, SUMMARY_CONTEXT_CHAR_LIMIT);
                List<CompletableFuture<String>> futures = new ArrayList<>();
                for (int i = 0; i < sections.size(); i++) {
                    final int idx = i;
                    futures.add(CompletableFuture.supplyAsync(() -> {
                        long sectionStart = System.currentTimeMillis();
                        logger.info("[ContentGen] Generating summary for section {} of {} (resource: {})", idx + 1, sections.size(), resourceId);
                        String sectionSummary = generator.generateSummary(prepareSummaryPrompt(sections.get(idx), resource.getTitle(), resource.getClass().getSimpleName()));
                        logger.info("[ContentGen] Finished section {} summary in {} ms (resource: {})", idx + 1, System.currentTimeMillis() - sectionStart, resourceId);
                        return sectionSummary;
                    }, contentGenerationExecutor));
                }
                List<String> allSummaries = futures.stream().map(CompletableFuture::join).collect(Collectors.toList());
                String combinedSummaries = String.join("\n", allSummaries);
                logger.info("[ContentGen] Generating final summary from section summaries for resource: {}", resourceId);
                String finalPrompt;
                try {
                    finalPrompt = promptService.createFinalSummaryWrapUpPrompt(resource.getTitle(), resource.getClass().getSimpleName(), combinedSummaries);
                } catch (Exception e) {
                    logger.error("[ContentGen] Error loading final summary wrap-up prompt, using fallback", e);
                    finalPrompt = "You are given a set of section summaries. Write a single, comprehensive summary that covers all the main points.";
                }
                String finalSummary = generator.generateSummary(finalPrompt);
                transactionTemplate.execute(status -> {
                    resourceRepository.updateSummaryById(resourceId, finalSummary);
                    return null;
                });
                logger.info("[ContentGen] Finished final summary generation for resource: {} in {} ms", resourceId, System.currentTimeMillis() - startTime);
                return finalSummary;
            }
        } catch (Exception e) {
            logger.error("[ContentGen] Error generating summary for resource: {}", resourceId, e);
            throw e;
        }
    }

    public String generateFlashcards(Resource resource) {
        long startTime = System.currentTimeMillis();
        logger.info("[ContentGen] Starting flashcards generation for resource: {}", resource.getId());
        String content = resource.getContent();
        if (content == null || content.isEmpty()) {
            logger.error("[ContentGen] Resource content is empty for flashcards. Extract content first. Resource: {}", resource.getId());
            throw new IllegalStateException("Resource content is empty. Extract content first.");
        }
        ChatLanguageModel chatModel = getChatModel();
        FlashcardGenerator generator = AiServices.builder(FlashcardGenerator.class)
                .chatLanguageModel(chatModel)
                .build();
        String flashcards;
        try {
            if (content.length() <= FLASHCARDS_CONTEXT_CHAR_LIMIT) {
                flashcards = generator.generateFlashcards(prepareFlashcardsPrompt(content, resource.getTitle(), resource.getClass().getSimpleName()));
            } else {
                logger.info("[ContentGen] Splitting content into sections for flashcards generation (length: {})", content.length());
                List<String> sections = splitIntoLargeSections(content, FLASHCARDS_CONTEXT_CHAR_LIMIT);
                List<CompletableFuture<String>> futures = new ArrayList<>();
                for (int i = 0; i < sections.size(); i++) {
                    final int idx = i;
                    futures.add(CompletableFuture.supplyAsync(() -> {
                        long sectionStart = System.currentTimeMillis();
                        logger.info("[ContentGen] Generating flashcards for section {} of {} (resource: {})", idx + 1, sections.size(), resource.getId());
                        String sectionFlashcards = generator.generateFlashcards(prepareFlashcardsPrompt(sections.get(idx), resource.getTitle(), resource.getClass().getSimpleName()));
                        logger.info("[ContentGen] Finished section {} flashcards in {} ms (resource: {})", idx + 1, System.currentTimeMillis() - sectionStart, resource.getId());
                        return sectionFlashcards;
                    }, contentGenerationExecutor));
                }
                JSONArray allFlashcards = new JSONArray();
                for (CompletableFuture<String> future : futures) {
                    try {
                        JSONArray sectionFlashcards = new JSONArray(future.get());
                        for (int i = 0; i < sectionFlashcards.length(); i++) {
                            allFlashcards.put(sectionFlashcards.getJSONObject(i));
                        }
                    } catch (Exception e) {
                        logger.error("[ContentGen] Error generating flashcards for section: {}", e.getMessage());
                    }
                }
                flashcards = allFlashcards.toString();
            }
            logger.info("[ContentGen] Calling updateFlashcardsById for resource: {} (flashcards length: {})", resource.getId(), flashcards.length());
            String cleaned = extractJsonArrayIfCodeBlock(flashcards);
            transactionTemplate.execute(status -> {
                resourceRepository.updateFlashcardsById(resource.getId(), cleaned);
                return null;
            });
            logger.info("[ContentGen] Finished flashcards generation for resource: {} in {} ms", resource.getId(), System.currentTimeMillis() - startTime);
            return cleaned;
        } catch (Exception e) {
            logger.error("[ContentGen] Error generating flashcards for resource: {}", resource.getId(), e);
            throw e;
        }
    }

    public String generateQuiz(Long resourceId) {
        long startTime = System.currentTimeMillis();
        logger.info("[ContentGen] Starting quiz generation for resource: {}", resourceId);
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found with ID: " + resourceId));
        String content = resource.getContent();
        if (content == null || content.isEmpty()) {
            logger.error("[ContentGen] Resource content is empty for quiz. Extract content first. Resource: {}", resourceId);
            throw new IllegalStateException("Resource content is empty. Extract content first.");
        }
        ChatLanguageModel chatModel = getChatModel();
        QuizGenerator generator = AiServices.builder(QuizGenerator.class)
                .chatLanguageModel(chatModel)
                .build();
        String quiz;
        try {
            if (content.length() <= QUIZ_CONTEXT_CHAR_LIMIT) {
                quiz = generator.generateQuiz(prepareQuizPrompt(content, resource.getTitle(), resource.getClass().getSimpleName()));
            } else {
                logger.info("[ContentGen] Splitting content into sections for quiz generation (length: {})", content.length());
                List<String> sections = splitIntoLargeSections(content, QUIZ_CONTEXT_CHAR_LIMIT);
                List<CompletableFuture<String>> futures = new ArrayList<>();
                for (int i = 0; i < sections.size(); i++) {
                    final int idx = i;
                    futures.add(CompletableFuture.supplyAsync(() -> {
                        long sectionStart = System.currentTimeMillis();
                        logger.info("[ContentGen] Generating quiz for section {} of {} (resource: {})", idx + 1, sections.size(), resourceId);
                        String sectionQuiz = generator.generateQuiz(prepareQuizPrompt(sections.get(idx), resource.getTitle(), resource.getClass().getSimpleName()));
                        logger.info("[ContentGen] Finished section {} quiz in {} ms (resource: {})", idx + 1, System.currentTimeMillis() - sectionStart, resourceId);
                        return sectionQuiz;
                    }, contentGenerationExecutor));
                }
                JSONArray allQuiz = new JSONArray();
                for (CompletableFuture<String> future : futures) {
                    try {
                        JSONArray sectionQuiz = new JSONArray(future.get());
                        for (int i = 0; i < sectionQuiz.length(); i++) {
                            allQuiz.put(sectionQuiz.getJSONObject(i));
                        }
                    } catch (Exception e) {
                        logger.error("[ContentGen] Error generating quiz for section: {}", e.getMessage());
                    }
                }
                quiz = allQuiz.toString();
            }
            logger.info("[ContentGen] Calling updateQuizById for resource: {} (quiz length: {})", resourceId, quiz.length());
            String cleaned = extractJsonArrayIfCodeBlock(quiz);
            transactionTemplate.execute(status -> {
                resourceRepository.updateQuizById(resourceId, cleaned);
                return null;
            });
            logger.info("[ContentGen] Finished quiz generation for resource: {} in {} ms", resourceId, System.currentTimeMillis() - startTime);
            return cleaned;
        } catch (Exception e) {
            logger.error("[ContentGen] Error generating quiz for resource: {}", resourceId, e);
            throw e;
        }
    }

    public String generateChapters(Long resourceId) {
        long startTime = System.currentTimeMillis();
        logger.info("[ContentGen] Starting chapters generation for resource: {}", resourceId);
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found with ID: " + resourceId));
        String content = resource.getContent();
        if (content == null || content.isEmpty()) {
            logger.error("[ContentGen] Resource content is empty for chapters. Extract content first. Resource: {}", resourceId);
            throw new IllegalStateException("Resource content is empty. Extract content first.");
        }
        ChatLanguageModel chatModel = getChatModel();
        ChapterGenerator generator = AiServices.builder(ChapterGenerator.class)
                .chatLanguageModel(chatModel)
                .build();
        String chapters;
        try {
            if (content.length() <= CHAPTERS_CONTEXT_CHAR_LIMIT) {
                chapters = generator.generateChapters(prepareChaptersPrompt(content, resource.getTitle(), resource.getClass().getSimpleName()));
            } else {
                logger.info("[ContentGen] Splitting content into sections for chapters generation (length: {})", content.length());
                List<String> sections = splitIntoLargeSections(content, CHAPTERS_CONTEXT_CHAR_LIMIT);
                List<CompletableFuture<String>> futures = new ArrayList<>();
                for (int i = 0; i < sections.size(); i++) {
                    final int idx = i;
                    futures.add(CompletableFuture.supplyAsync(() -> {
                        long sectionStart = System.currentTimeMillis();
                        logger.info("[ContentGen] Generating chapters for section {} of {} (resource: {})", idx + 1, sections.size(), resourceId);
                        String sectionChapters = generator.generateChapters(prepareChaptersPrompt(sections.get(idx), resource.getTitle(), resource.getClass().getSimpleName()));
                        logger.info("[ContentGen] Finished section {} chapters in {} ms (resource: {})", idx + 1, System.currentTimeMillis() - sectionStart, resourceId);
                        return sectionChapters;
                    }, contentGenerationExecutor));
                }
                JSONArray allChapters = new JSONArray();
                for (CompletableFuture<String> future : futures) {
                    try {
                        JSONArray sectionChapters = new JSONArray(future.get());
                        for (int i = 0; i < sectionChapters.length(); i++) {
                            allChapters.put(sectionChapters.getJSONObject(i));
                        }
                    } catch (Exception e) {
                        logger.error("[ContentGen] Error generating chapters for section: {}", e.getMessage());
                    }
                }
                chapters = allChapters.toString();
            }
            logger.info("[ContentGen] Calling updateChaptersById for resource: {} (chapters length: {})", resourceId, chapters.length());
            String cleaned = extractJsonArrayIfCodeBlock(chapters);
            transactionTemplate.execute(status -> {
                resourceRepository.updateChaptersById(resourceId, cleaned);
                return null;
            });
            logger.info("[ContentGen] Finished chapters generation for resource: {} in {} ms", resourceId, System.currentTimeMillis() - startTime);
            return cleaned;
        } catch (Exception e) {
            logger.error("[ContentGen] Error generating chapters for resource: {}", resourceId, e);
            throw e;
        }
    }

    // Helper to split content into large sections for chapter generation
    private List<String> splitIntoLargeSections(String content, int maxSectionLength) {
        List<String> sections = new ArrayList<>();
        int start = 0;
        while (start < content.length()) {
            int end = Math.min(start + maxSectionLength, content.length());
            sections.add(content.substring(start, end));
            start = end;
        }
        return sections;
    }

    @Async
    public void generateAllContentAsync(Long resourceId) {
        generateAllContentParallel(resourceId);
    }


    private String generateChunkId(Long resourceId, int chunkIndex) {
        return "resource-" + resourceId + "-chunk-" + chunkIndex;
    }

    private List<Float> toFloatList(float[] array) {
        List<Float> list = new ArrayList<>(array.length);
        for (float value : array) {
            list.add(value);
        }
        return list;
    }


    public void processEmbeddings(Resource resource) {
        logger.debug("Starting embedding process for resource ID: {}", resource.getId());
        
        String content = resource.getContent();
        int contentLength = content.length();
        
        // Check for extremely large documents that could cause memory issues
        if (contentLength > 1000000) { // 1MB limit
            logger.error("Document too large for processing: {} chars ({} MB). Skipping embedding generation.", 
                        contentLength, contentLength / (1024 * 1024));
            throw new RuntimeException("Document too large for processing: " + contentLength + " characters");
        }
        
        // Use batch processing for very large documents to prevent OutOfMemoryError
        if (contentLength > 100000) {
            processEmbeddingsInBatches(resource);
        } else {
            processEmbeddingsNormal(resource);
        }
    }
    
    /**
     * Process embeddings in batches for very large documents
     * Enhanced with memory monitoring and smaller batch sizes
     */
    private void processEmbeddingsInBatches(Resource resource) {
        logger.info("Using batch processing for large document ({} chars)", resource.getContent().length());
        
        final int[] chunkIndex = {0};
        final int batchSize = 20; // Further reduced to prevent memory buildup
        
        // Monitor memory usage
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        logger.info("Initial memory usage: {} MB / {} MB ({}%)", 
                   initialMemory / (1024 * 1024), 
                   maxMemory / (1024 * 1024),
                   (initialMemory * 100) / maxMemory);
        
        chunkingService.processLargeDocumentInBatches(resource.getContent(), batchSize, chunks -> {
            List<PineconeVector> batchRecords = new ArrayList<>();
            
            for (String chunk : chunks) {
                try {
                    logger.debug("Processing chunk index {}: {}...", chunkIndex[0], abbreviate(chunk, 100));
                    
                    float[] embedding = embeddingService.embed(chunk);
                    logger.debug("Generated embedding for chunk index {}", chunkIndex[0]);
                    
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("resource_id", resource.getId());
                    metadata.put("chunk_index", chunkIndex[0]);
                    metadata.put("chunk_text", chunk);
                    
                    List<Float> embeddingList = toFloatList(embedding);
                    PineconeVector vector = new PineconeVector(generateChunkId(resource.getId(), chunkIndex[0]), embeddingList, metadata);
                    batchRecords.add(vector);
                    
                    chunkIndex[0]++;
                    
                    // Clear embedding array to help GC
                    embedding = null;
                } catch (Exception e) {
                    logger.error("Failed to process embedding for chunk index {}: {}", chunkIndex[0], e.getMessage(), e);
                    chunkIndex[0]++;
                }
            }
            
            // Process this batch
            try {
                embeddingService.upsertVectors(batchRecords);
                logger.debug("Successfully upserted batch of {} vectors for resource ID: {}", batchRecords.size(), resource.getId());
                
                // Monitor memory after batch processing
                long currentMemory = runtime.totalMemory() - runtime.freeMemory();
                long memoryPercentage = (currentMemory * 100) / maxMemory;
                logger.debug("Memory usage after batch {}: {} MB / {} MB ({}%)", 
                           chunkIndex[0] / batchSize, 
                           currentMemory / (1024 * 1024),
                           maxMemory / (1024 * 1024),
                           memoryPercentage);
                
                // Force GC if memory usage is high
                if (memoryPercentage > 80) {
                    logger.warn("High memory usage detected ({}%), forcing garbage collection", memoryPercentage);
                    System.gc();
                }
            } catch (Exception e) {
                logger.error("Failed to upsert batch vectors: {}", e.getMessage(), e);
            } finally {
                // Clear batch records to help GC
                batchRecords.clear();
            }
        });
        
        // Final memory cleanup
        System.gc();
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        logger.info("Final memory usage: {} MB", finalMemory / (1024 * 1024));
    }
    
    /**
     * Normal embedding processing for smaller documents
     * Enhanced with memory monitoring and chunk size limits
     */
    private void processEmbeddingsNormal(Resource resource) {
        logger.debug("Using normal processing for document ({} chars)", resource.getContent().length());
        
        // For medium documents, use smaller chunk sizes to prevent memory issues
        List<String> chunks;
        if (resource.getContent().length() > 50000) {
            chunks = chunkingService.fixedSizeChunking(resource.getContent(), 800, 150); // Smaller chunks
        } else {
            chunks = chunkingService.splitIntoChunks(resource.getContent());
        }
        
        logger.debug("Split content into {} chunks", chunks.size());
        
        // Process chunks in smaller batches even for "normal" processing
        final int batchSize = 20;
        List<PineconeVector> records = new ArrayList<>();
        int chunkIndex = 0;
        
        for (String chunk : chunks) {
            logger.debug("Processing chunk index {}: {}...", chunkIndex, abbreviate(chunk, 100));
            
            try {
                float[] embedding = embeddingService.embed(chunk);
                logger.debug("Generated embedding for chunk index {}", chunkIndex);
                
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("resource_id", resource.getId());
                metadata.put("chunk_index", chunkIndex);
                metadata.put("chunk_text", chunk);
                
                List<Float> embeddingList = toFloatList(embedding);
                PineconeVector vector = new PineconeVector(generateChunkId(resource.getId(), chunkIndex), embeddingList, metadata);
                records.add(vector);
                
                // Clear embedding array to help GC
                embedding = null;
            } catch (Exception e) {
                logger.error("Failed to process embedding for chunk index {}: {}", chunkIndex, e.getMessage(), e);
            }
            
            chunkIndex++;
            
            // Process in batches to prevent memory buildup
            if (records.size() >= batchSize) {
                try {
                    embeddingService.upsertVectors(new ArrayList<>(records));
                    logger.debug("Processed batch of {} vectors", records.size());
                } catch (Exception e) {
                    logger.error("Failed to upsert batch vectors: {}", e.getMessage(), e);
                }
                records.clear();
                System.gc(); // Force GC after each batch
            }
        }
        
        // Process remaining records
        if (!records.isEmpty()) {
        try {
            embeddingService.upsertVectors(records);
                logger.debug("Successfully upserted final {} vectors into Pinecone for resource ID: {}", records.size(), resource.getId());
        } catch (Exception e) {
                logger.error("Failed to upsert final vectors into Pinecone: {}", e.getMessage(), e);
        }
        }
        
        // Final cleanup
        records.clear();
        System.gc();
    }
    
    private String abbreviate(String text, int maxLength) {
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }


    public Resource generateAdditionalFlashcards(Resource resource) {
        logger.info("Appending additional flashcards for resource ID: {}", resource.getId());

        if (resource.getContent() == null || resource.getContent().isEmpty()) {
            throw new IllegalStateException("Resource content is empty. Extract content first.");
        }

        ChatLanguageModel chatModel = getChatModel();

        FlashcardGenerator flashcardGenerator = AiServices.builder(FlashcardGenerator.class)
                .chatLanguageModel(chatModel)
                .build();

        String flashcardsJson = flashcardGenerator.generateFlashcards(prepareFlashcardsPrompt(resource.getContent(), resource.getTitle(), resource.getClass().getSimpleName()));
        String cleanJson = validateAndCleanJson(flashcardsJson, "flashcards");
        JSONArray newFlashcards = new JSONArray(cleanJson);

        JSONArray existingFlashcards;
        try {
            existingFlashcards = new JSONArray(resource.getFlashcards());
        } catch (Exception e) {
            logger.warn("Existing flashcards could not be parsed. Starting fresh.");
            existingFlashcards = new JSONArray();
        }

        for (int i = 0; i < newFlashcards.length(); i++) {
            existingFlashcards.put(newFlashcards.getJSONObject(i));
        }

        resource.setFlashcards(existingFlashcards.toString());

        return resourceRepository.save(resource);
    }

    /**
     * Generate all content types in parallel with chunk timing tracking
     */
    @Transactional
    public Resource generateAllContentParallel(Long resourceId) {
        long startTime = System.currentTimeMillis();
        logger.info("Starting parallel content generation with chunk timing for resource: {}", resourceId);
        logger.debug("🕐 Content generation started at: {}", startTime);
        
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found with ID: " + resourceId));
        
        String content = resource.getContent();
        int contentLength = content.length();
        logger.info("Document size: {} characters", contentLength);
        
        // Check memory before starting parallel processing
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        double memoryUsage = (double) usedMemory / totalMemory;
        
        logger.info("Memory usage before parallel processing: {:.2f}% ({} MB used / {} MB total)", 
                   String.format("%.2f", memoryUsage * 100), usedMemory / (1024 * 1024), totalMemory / (1024 * 1024));
        
        // Handle large files with appropriate strategy
        final String processedContent = content;
       
        
        ChatLanguageModel chatModel = getChatModel();
        
        try {
            // Track progress
            final int totalTasks = 4;
            final int[] completedTasks = {0};
            
            // Generate all content types in parallel with chunk timing and immediate saving
            CompletableFuture<String> summaryFuture = CompletableFuture.supplyAsync(() -> generateSummary(resource.getId()), contentGenerationExecutor);
            CompletableFuture<String> flashcardsFuture = CompletableFuture.supplyAsync(() -> generateFlashcards(resource), contentGenerationExecutor);
            CompletableFuture<String> quizFuture = CompletableFuture.supplyAsync(() -> generateQuiz(resource.getId()), contentGenerationExecutor);
            CompletableFuture<String> chaptersFuture = CompletableFuture.supplyAsync(() -> generateChapters(resource.getId()), contentGenerationExecutor);
            
            // Wait for all parallel tasks to complete with timeout
            logger.info("⏳ Waiting for all parallel content generation tasks to complete...");
            CompletableFuture<Void> allTasks = CompletableFuture.allOf(
                summaryFuture, flashcardsFuture, quizFuture, chaptersFuture
            );
            
            try {
                allTasks.get(5, TimeUnit.MINUTES); // 5 minute timeout
                logger.debug("🎉 All parallel content generation tasks completed successfully!");
            } catch (TimeoutException e) {
                logger.error("⏰ Content generation timed out after 5 minutes");
                throw new RuntimeException("Content generation timed out");
            } catch (Exception e) {
                logger.error("❌ Error waiting for parallel content generation: {}", e.getMessage());
                throw new RuntimeException("Error in parallel content generation", e);
            }
            
            // Get results from all futures to create combined generated content
            String summary = summaryFuture.get();
            String flashcards = flashcardsFuture.get();
            String quiz = quizFuture.get();
            String chapters = chaptersFuture.get();
            
            // Create combined generated content
            JSONObject generatedContent = new JSONObject();
            generatedContent.put("summary", summary);
            
            try {
                generatedContent.put("flashcards", new JSONArray(flashcards));
            } catch (Exception e) {
                logger.warn("⚠️ Error parsing flashcards as JSON array: {}", e.getMessage());
                generatedContent.put("flashcards", flashcards);
            }
            
            try {
                generatedContent.put("quiz", new JSONArray(quiz));
            } catch (Exception e) {
                logger.warn("⚠️ Error parsing quiz as JSON array: {}", e.getMessage());
                generatedContent.put("quiz", quiz);
            }
            
            try {
                generatedContent.put("chapters", new JSONArray(chapters));
            } catch (Exception e) {
                logger.warn("⚠️ Error parsing chapters as JSON array: {}", e.getMessage());
                generatedContent.put("chapters", chapters);
            }
            
            // Save the combined generated content
            //resource.setGeneratedContent(generatedContent.toString());
            
            // Process embeddings
            try {
                processEmbeddings(resource);
            } catch (Exception e) {
                logger.error("Embedding processing failed: {}", e.getMessage(), e);
            }
            
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            logger.info("🎉 All parallel content generation completed in {} ms", totalTime);
            
            return resource;
            
        } catch (Exception e) {
            long errorTime = System.currentTimeMillis();
            long totalTime = errorTime - startTime;
            logger.error("❌ Error in parallel content generation after {} ms: {}", totalTime, e.getMessage());
            throw new RuntimeException("Parallel content generation failed", e);
        }
    }
    
    /**
     * Helper method to generate content with chunk timing
     */
    private <T> String generateContentWithChunkTiming(String contentType, int taskNumber, int totalTasks, 
            String content, Resource resource, ChatLanguageModel chatModel, 
            Function3<String, String, String, String> promptFunction, 
            Class<T> generatorClass, int[] completedTasks, long overallStartTime) {
        
        long taskStartTime = System.currentTimeMillis();
        logger.debug("🔄 [{}/{}] Generating {}...", taskNumber, totalTasks, contentType);
        logger.debug("🔄 [{}/{}] {} generation started at: {} ms from overall start", 
                    taskNumber, totalTasks, contentType, taskStartTime - overallStartTime);
        
        try {
            // Track preparation time
            long prepStartTime = System.currentTimeMillis();
            String prompt = promptFunction.apply(content, resource.getTitle(), resource.getClass().getSimpleName());
            long prepEndTime = System.currentTimeMillis();
            long prepTime = prepEndTime - prepStartTime;
            logger.debug("📝 [{}/{}] {} prompt preparation took: {} ms", taskNumber, totalTasks, contentType, prepTime);
            
            // Track service creation time
            long serviceStartTime = System.currentTimeMillis();
            T generator = AiServices.builder(generatorClass)
                    .chatLanguageModel(chatModel)
                    .build();
            long serviceEndTime = System.currentTimeMillis();
            long serviceInitTime = serviceEndTime - serviceStartTime;
            logger.debug("⚙️ [{}/{}] {} service creation took: {} ms", taskNumber, totalTasks, contentType, serviceInitTime);
            
            // Track generation time with chunk monitoring
            long generationStartTime = System.currentTimeMillis();
            logger.debug("📤 [{}/{}] Sending {} generation request...", taskNumber, totalTasks, contentType);
            
            // Simulate chunk tracking
            final long[] firstResponseTime = {-1};
            
            // Start progress monitor for this task
            CompletableFuture<Void> progressMonitor = CompletableFuture.runAsync(() -> {
                try {
                    long monitorStart = System.currentTimeMillis();
                    logger.debug("🔍 [{}/{}] Starting {} progress monitor at: {} ms from overall start", 
                                taskNumber, totalTasks, contentType, monitorStart - overallStartTime);
                    
                    while (firstResponseTime[0] == -1) {
                        Thread.sleep(100); // Check every 100ms
                        long currentTime = System.currentTimeMillis();
                        long elapsed = currentTime - monitorStart;
                        
                        // Log progress every 3 seconds for individual tasks
                        if (elapsed > 0 && elapsed % 3000 == 0) {
                            logger.debug("⏳ [{}/{}] Still waiting for {} first response... {} ms elapsed", 
                                        taskNumber, totalTasks, contentType, elapsed);
                        }
                        
                        // Timeout after 2 minutes
                        if (elapsed > 120000) {
                            logger.warn("⚠️ [{}/{}] No {} response received after 2 minutes", taskNumber, totalTasks, contentType);
                            break;
                        }
                    }
                    
                    if (firstResponseTime[0] != -1) {
                        logger.debug("🎯 [{}/{}] {} progress monitor detected first response at: {} ms from overall start", 
                                    taskNumber, totalTasks, contentType, firstResponseTime[0] - overallStartTime);
                    }
                } catch (InterruptedException e) {
                    logger.debug("🔍 [{}/{}] {} progress monitor interrupted", taskNumber, totalTasks, contentType);
                    Thread.currentThread().interrupt();
                }
            });
            
            // Generate the content
            String result;
            if (generatorClass == SummaryGenerator.class) {
                result = ((SummaryGenerator) generator).generateSummary(prompt);
                // Add debug logging for summary response
                logger.debug("Summary response from chunk timing generation for resource {}: {}", resource.getId(), result);
                logger.debug("Summary response length from chunk timing generation for resource {}: {} characters", resource.getId(), result.length());
            } else if (generatorClass == FlashcardGenerator.class) {
                String json = ((FlashcardGenerator) generator).generateFlashcards(prompt);
                result = validateAndCleanJson(json, "flashcards");
            } else if (generatorClass == QuizGenerator.class) {
                String json = ((QuizGenerator) generator).generateQuiz(prompt);
                result = validateAndCleanJson(json, "quiz");
            } else if (generatorClass == ChapterGenerator.class) {
                String json = ((ChapterGenerator) generator).generateChapters(prompt);
                result = validateAndCleanJson(json, "chapters");
            } else {
                throw new RuntimeException("Unsupported generator type: " + generatorClass.getSimpleName());
            }
            
            // Record first response time
            long firstResponseEndTime = System.currentTimeMillis();
            if (firstResponseTime[0] == -1) {
                firstResponseTime[0] = firstResponseEndTime;
                logger.debug("⚡ [{}/{}] {} FIRST CHUNK RECEIVED at: {} ms from overall start", 
                            taskNumber, totalTasks, contentType, firstResponseEndTime - overallStartTime);
                logger.debug("⚡ [{}/{}] {} time to first chunk: {} ms", 
                            taskNumber, totalTasks, contentType, firstResponseEndTime - generationStartTime);
            }
            
            long generationEndTime = System.currentTimeMillis();
            long generationTime = generationEndTime - generationStartTime;
            long timeToFirstResponse = firstResponseTime[0] - generationStartTime;
            
            logger.debug("📊 [{}/{}] {} generation completed at: {} ms from overall start", 
                        taskNumber, totalTasks, contentType, generationEndTime - overallStartTime);
            logger.debug("📊 [{}/{}] {} total generation duration: {} ms", taskNumber, totalTasks, contentType, generationTime);
            logger.debug("📊 [{}/{}] {} time to first chunk: {} ms", taskNumber, totalTasks, contentType, timeToFirstResponse);
            
            logger.info("✅ [{}/{}] {} generation completed!", taskNumber, totalTasks, contentType);
            logger.info("⚡ [{}/{}] {} time to first response: {} ms", taskNumber, totalTasks, contentType, timeToFirstResponse);
            logger.info("📊 [{}/{}] {} total generation time: {} ms", taskNumber, totalTasks, contentType, generationTime);
            logger.info("📊 [{}/{}] {} result length: {} characters", taskNumber, totalTasks, contentType, result.length());
            
            // Calculate estimated chunks
            int estimatedChunks = Math.max(1, result.length() / 50);
            long avgTimePerChunk = generationTime / estimatedChunks;
            logger.debug("📈 [{}/{}] {} estimated chunks: {}, avg time per chunk: {} ms", 
                        taskNumber, totalTasks, contentType, estimatedChunks, avgTimePerChunk);
            
            // Save content immediately
            synchronized (resource) {
                if (generatorClass == SummaryGenerator.class) {
                    resource.setSummary(result);
                } else if (generatorClass == FlashcardGenerator.class) {
                    resource.setFlashcards(result);
                } else if (generatorClass == QuizGenerator.class) {
                    resource.setQuiz(result);
                } else if (generatorClass == ChapterGenerator.class) {
                    resource.setChapters(result);
                }
                resourceRepository.save(resource);
                logger.debug("💾 [{}/{}] {} saved immediately", taskNumber, totalTasks, contentType, result);
            }
            
            completedTasks[0]++;
            long taskEndTime = System.currentTimeMillis();
            long totalTaskTime = taskEndTime - taskStartTime;
            logger.debug("✅ [{}/{}] {} generation completed and saved", taskNumber, totalTasks, contentType, result);
            logger.debug("📊 [{}/{}] {} total task time: {} ms", taskNumber, totalTasks, contentType, totalTaskTime);
            
            return result;
            
        } catch (Exception e) {
            logger.error("❌ [{}/{}] Error generating {}: {}", taskNumber, totalTasks, contentType, e.getMessage());
            completedTasks[0]++;
            long taskEndTime = System.currentTimeMillis();
            logger.debug("📊 [{}/{}] {} generation failed after: {} ms", taskNumber, totalTasks, contentType, (taskEndTime - taskStartTime));
            
            if (generatorClass == SummaryGenerator.class) {
                return "Error generating summary: " + e.getMessage();
            } else {
                return "[]";
            }
        }
    }
    
    // Functional interface for 3-parameter function
    @FunctionalInterface
    private interface Function3<T, U, V, R> {
        R apply(T t, U u, V v);
    }

    /**
     * Generate hierarchical summary strategy for parallel content generation
     */
    private String generateHierarchicalSummaryStrategy(String content, Resource resource, ChatLanguageModel chatModel, 
            int[] completedTasks, long startTime) {
        
        long taskStartTime = System.currentTimeMillis();
        logger.debug("🔄 [1/4] Generating Hierarchical Summary...");
        logger.debug("🔄 [1/4] Hierarchical Summary generation started at: {} ms from overall start", 
                    taskStartTime - startTime);
        
        try {
            // Step 1: Split content into logical sections
            long splittingStartTime = System.currentTimeMillis();
            List<TextSegment> sections = splitIntoSummarySections(content);
            long splittingEndTime = System.currentTimeMillis();
            long splittingTime = splittingEndTime - splittingStartTime;
            logger.info("Split content into {} sections for hierarchical summary in {} ms", sections.size(), splittingTime);
            // Log each section's content (abbreviated)
            for (int i = 0; i < sections.size(); i++) {
                String abbreviatedSection = abbreviate(sections.get(i).text(), 300);
                logger.debug("Section {} content (abbreviated): {}", i + 1, abbreviatedSection);
            }
            // Step 2: Generate summaries for each section in parallel
            long sectionGenerationStartTime = System.currentTimeMillis();
            long sectionGenerationTime = 0; // Declare outside try-catch
            List<CompletableFuture<String>> sectionFutures = new ArrayList<>();
            
            for (int i = 0; i < sections.size(); i++) {
                final int sectionIndex = i;
                TextSegment section = sections.get(i);
                
                CompletableFuture<String> sectionFuture = CompletableFuture.supplyAsync(() -> {
                    long sectionStartTime = System.currentTimeMillis();
                    logger.debug("Generating summary for section {}/{}", sectionIndex + 1, sections.size());
                    
                    SummaryGenerator sectionGenerator = AiServices.builder(SummaryGenerator.class)
                            .chatLanguageModel(chatModel)
                            .build();
                    
                    String sectionPrompt;
                    try {
                        sectionPrompt = promptService.createSummaryGenerationPrompt(
                            resource.getTitle(), 
                            resource.getClass().getSimpleName(), 
                            section.text()
                        );
                    } catch (IOException e) {
                        logger.error("Error loading section summary prompt, using fallback", e);
                        sectionPrompt = createSectionSummaryPrompt(resource, section.text(), sectionIndex + 1, sections.size());
                    }
                    
                    String sectionSummary = sectionGenerator.generateSummary(sectionPrompt);
                    long sectionEndTime = System.currentTimeMillis();
                    long sectionTime = sectionEndTime - sectionStartTime;
                    logger.debug("Section {} summary completed in {} ms, length: {} characters", 
                                sectionIndex + 1, sectionTime, sectionSummary.length());
                    
                    return sectionSummary;
                }, contentGenerationExecutor);
                
                sectionFutures.add(sectionFuture);
            }
            
            // Wait for all section summaries to complete
            CompletableFuture<Void> allSectionFutures = CompletableFuture.allOf(
                sectionFutures.toArray(new CompletableFuture[0])
            );
            
            try {
                allSectionFutures.get(3, TimeUnit.MINUTES); // 3 minute timeout for section summaries
                long sectionGenerationEndTime = System.currentTimeMillis();
                sectionGenerationTime = sectionGenerationEndTime - sectionGenerationStartTime;
                logger.info("All {} section summaries completed successfully in {} ms", sections.size(), sectionGenerationTime);
            } catch (TimeoutException e) {
                long sectionGenerationEndTime = System.currentTimeMillis();
                sectionGenerationTime = sectionGenerationEndTime - sectionGenerationStartTime;
                logger.error("Section summaries generation timed out after {} ms", sectionGenerationTime);
                throw new RuntimeException("Section summaries generation timed out");
            } catch (Exception e) {
                long sectionGenerationEndTime = System.currentTimeMillis();
                sectionGenerationTime = sectionGenerationEndTime - sectionGenerationStartTime;
                logger.error("Error waiting for section summaries after {} ms: {}", sectionGenerationTime, e.getMessage());
                throw new RuntimeException("Error in section summaries generation", e);
            }
            
            // Collect all section summaries
            long collectionStartTime = System.currentTimeMillis();
            List<String> sectionSummaries = new ArrayList<>();
            for (int i = 0; i < sectionFutures.size(); i++) {
                try {
                    String summary = sectionFutures.get(i).get();
                    sectionSummaries.add(summary);
                    logger.info("Section {} summary: {}", i + 1, abbreviate(summary, 400));
                } catch (Exception e) {
                    logger.error("Error getting section summary: {}", e.getMessage());
                    sectionSummaries.add("Error generating section summary: " + e.getMessage());
                }
            }
            long collectionEndTime = System.currentTimeMillis();
            long collectionTime = collectionEndTime - collectionStartTime;
            logger.debug("Collected {} section summaries in {} ms", sectionSummaries.size(), collectionTime);
            
            // Step 3: Combine section summaries into final comprehensive summary
            long combinationStartTime = System.currentTimeMillis();
            String combinedSections = String.join("\n\n", sectionSummaries);
            
            String finalPrompt;
            try {
                finalPrompt = promptService.createSummaryGenerationPrompt(
                    resource.getTitle(), 
                    resource.getClass().getSimpleName(), 
                    combinedSections
                );
            } catch (IOException e) {
                logger.error("Error loading final summary prompt, using fallback", e);
                finalPrompt = createFinalSummaryPrompt(resource, combinedSections);
            }
            
            SummaryGenerator finalGenerator = AiServices.builder(SummaryGenerator.class)
                    .chatLanguageModel(chatModel)
                    .build();
            
            String finalSummary = finalGenerator.generateSummary(finalPrompt);
            long combinationEndTime = System.currentTimeMillis();
            long combinationTime = combinationEndTime - combinationStartTime;
            logger.info("Final summary combination completed in {} ms", combinationTime);
            
            logger.debug("Hierarchical summary response for resource {}: {}", resource.getId(), finalSummary);
            logger.debug("Hierarchical summary response length for resource {}: {} characters", resource.getId(), finalSummary.length());
            
            // Save content immediately
            long saveStartTime = System.currentTimeMillis();
            synchronized (resource) {
                resource.setSummary(finalSummary);
                resourceRepository.save(resource);
                logger.debug("💾 [1/4] Hierarchical Summary saved immediately");
            }
            long saveEndTime = System.currentTimeMillis();
            long saveTime = saveEndTime - saveStartTime;
            logger.debug("Summary saved in {} ms", saveTime);
            
            completedTasks[0]++;
            long taskEndTime = System.currentTimeMillis();
            long totalTaskTime = taskEndTime - taskStartTime;
            
            // Log comprehensive timing summary
            logger.info("✅ [1/4] Hierarchical Summary generation completed!");
            logger.info("📊 [1/4] Hierarchical Summary total task time: {} ms", totalTaskTime);
            logger.info("📊 [1/4] Hierarchical Summary breakdown:");
            logger.info("   - Content splitting: {} ms", splittingTime);
            logger.info("   - Section generation: {} ms", sectionGenerationTime);
            logger.info("   - Section collection: {} ms", collectionTime);
            logger.info("   - Final combination: {} ms", combinationTime);
            logger.info("   - Database save: {} ms", saveTime);
            logger.info("   - Total hierarchical summary time: {} ms ({} seconds)", 
                       totalTaskTime, totalTaskTime / 1000.0);
            
            return finalSummary;
            
        } catch (Exception e) {
            long taskEndTime = System.currentTimeMillis();
            long totalTaskTime = taskEndTime - taskStartTime;
            logger.error("❌ [1/4] Error generating Hierarchical Summary after {} ms: {}", totalTaskTime, e.getMessage());
            completedTasks[0]++;
            logger.debug("📊 [1/4] Hierarchical Summary generation failed after: {} ms ({} seconds)", 
                        totalTaskTime, totalTaskTime / 1000.0);
            return "Error generating hierarchical summary: " + e.getMessage();
        }
    }
    
    /**
     * Split content into smaller, semantically meaningful sections for finer coverage
     */
    private List<TextSegment> splitIntoSummarySections(String content) {
        int contentLength = content.length();

        if (contentLength <= 30000) {
            // Small documents: 4-6 sections for even finer coverage
            DocumentSplitter splitter = DocumentSplitters.recursive(6000, 600);
            Document document = Document.from(content);
            return splitter.split(document);
        } else if (contentLength <= 80000) {
            // Medium documents: 6-10 sections
            DocumentSplitter splitter = DocumentSplitters.recursive(9000, 900);
            Document document = Document.from(content);
            return splitter.split(document);
        } else if (contentLength <= 150000) {
            // Large documents: 10-15 sections
            DocumentSplitter splitter = DocumentSplitters.recursive(12000, 1200);
            Document document = Document.from(content);
            return splitter.split(document);
        } else {
            // Very large documents: 15-20 sections
            DocumentSplitter splitter = DocumentSplitters.recursive(15000, 1500);
            Document document = Document.from(content);
            return splitter.split(document);
        }
    }
    
    /**
     * Create prompt for individual section summary
     */
    private String createSectionSummaryPrompt(Resource resource, String sectionText, int sectionNumber, int totalSections) {
        String resourceType = resource.getClass().getSimpleName();
        String resourceTitle = resource.getTitle() != null ? resource.getTitle() : "Untitled Resource";
        
        return String.format(
            "You are summarizing Section %d of %d from a %s titled '%s'.\n\n" +
            "Create a detailed, comprehensive summary of this section that includes:\n" +
            "- All key concepts and definitions\n" +
            "- Important formulas, numbers, or data\n" +
            "- Examples and explanations\n" +
            "- Relationships to other concepts\n\n" +
            "Section %d Content:\n%s\n\n" +
            "Provide a thorough summary that captures all essential information from this section.",
            sectionNumber, totalSections, resourceType, resourceTitle,
            sectionNumber, sectionText
        );
    }
    
    /**
     * Create prompt for combining section summaries into final summary
     */
    private String createFinalSummaryPrompt(Resource resource, String combinedSections) {
        String resourceType = resource.getClass().getSimpleName();
        String resourceTitle = resource.getTitle() != null ? resource.getTitle() : "Untitled Resource";
        
        return String.format(
            "You are creating a final comprehensive summary for a %s titled '%s'.\n\n" +
            "Below are detailed summaries of all sections of this document. Your task is to:\n" +
            "1. Combine these section summaries into one cohesive, well-structured summary\n" +
            "2. Eliminate redundancy while preserving all important information\n" +
            "3. Ensure logical flow and connections between concepts\n" +
            "4. Create a summary that serves as a complete study guide\n\n" +
            "Section Summaries:\n%s\n\n" +
            "Create a comprehensive final summary that students can use as their primary study material.",
            resourceType, resourceTitle, combinedSections
        );
    }

    /**
     * Create prompt for combining section summaries into final summary
     */
    private String createFinalSummaryWrapUpPrompt(Resource resource, String combinedSections) {
        String resourceType = resource.getClass().getSimpleName();
        String resourceTitle = resource.getTitle() != null ? resource.getTitle() : "Untitled Resource";
        
        try {
            return promptService.createFinalSummaryWrapUpPrompt(resourceTitle, resourceType, combinedSections);
        } catch (IOException e) {
            logger.error("Error loading final summary wrap-up prompt, using fallback", e);
            // Fallback to hardcoded prompt
            return String.format(
                "You are creating a final comprehensive summary for a %s titled '%s'.\n\n" +
                "Below are detailed summaries of all sections of this document. Your task is to:\n" +
                "1. Combine these section summaries into one cohesive, well-structured summary\n" +
                "2. Eliminate redundancy while preserving all important information\n" +
                "3. Ensure logical flow and connections between concepts\n" +
                "4. Create a summary that serves as a complete study guide\n\n" +
                "Section Summaries:\n%s\n\n" +
                "Create a comprehensive final summary that students can use as their primary study material.\n\n" +
                "Wrap up and create a cohesive final summary.",
                resourceType, resourceTitle, combinedSections
            );
        }
    }

    /**
     * Estimate token count (rough approximation: 4 characters per token)
     */
    private int estimateTokens(String text) {
        return text.length() / 4;
    }

    /**
     * Generate a summary from raw content and title, without saving anything.
     * Used for test/evaluation endpoints.
     */
    public String generateSummaryFromContent(String content, String title) {
        if (content == null || content.isEmpty()) {
            throw new IllegalArgumentException("Content is empty. Provide valid content.");
        }
        if (content.length() > 40000) {
            // Use map-reduce for very large files
            return generateSummaryMapReduceForContent(content, title);
        }
        // Handle large content for test endpoint as well
        String processedContent = handleLargeContent(content, 4000);
        int inputTokens = estimateTokens(processedContent);
        logger.info("[Test] Input content: {} chars (~{} tokens)", processedContent.length(), inputTokens);
        
        ChatLanguageModel chatModel = getChatModel();
        String prompt = prepareSummaryPrompt(processedContent, title, "File");
        dev.langchain4j.data.message.AiMessage response = chatModel.generate(new dev.langchain4j.data.message.UserMessage(prompt)).content();
        String summary = response.text();
        
        int outputTokens = estimateTokens(summary);
        logger.info("[Test] Generated summary: {} chars (~{} tokens)", summary.length(), outputTokens);
        logger.debug("[Test] Generated summary for test content: {}", abbreviate(summary, 400));
        return summary;
    }

    /**
     * Map-reduce style summary generation for very large content (no saving, no Resource).
     */
    private String generateSummaryMapReduceForContent(String content, String title) {
        long startTime = System.currentTimeMillis();
        int totalInputTokens = estimateTokens(content);
        logger.info("[Test] Using map-reduce approach for very large document ({} chars, ~{} tokens)", content.length(), totalInputTokens);
        List<TextSegment> sections = splitIntoSummarySections(content);
        logger.info("[Test] Split document into {} sections for map-reduce summary", sections.size());
        ChatLanguageModel chatModel = getChatModel();
        List<CompletableFuture<String>> sectionFutures = new ArrayList<>();
        for (int i = 0; i < sections.size(); i++) {
            final int sectionIndex = i;
            final String sectionText = sections.get(i).text();
            sectionFutures.add(CompletableFuture.supplyAsync(() -> {
                try {
                    String sectionPrompt = prepareSummaryPrompt(sectionText, title, "File");
                    int sectionInputTokens = estimateTokens(sectionText);
                    dev.langchain4j.data.message.AiMessage sectionResponse = chatModel.generate(new dev.langchain4j.data.message.UserMessage(sectionPrompt)).content();
                    String sectionSummary = sectionResponse.text();
                    int sectionOutputTokens = estimateTokens(sectionSummary);
                    
                    logger.debug("[Test] Section {} summary: {} chars (~{} tokens) -> {} chars (~{} tokens)", 
                                sectionIndex + 1, sectionText.length(), sectionInputTokens, 
                                sectionSummary.length(), sectionOutputTokens);
                    return sectionSummary;
                } catch (Exception e) {
                    logger.error("[Test] Error generating section summary {}: {}", sectionIndex + 1, e.getMessage());
                    return "Error generating section summary: " + e.getMessage();
                }
            }, contentGenerationExecutor));
        }
        // Wait for all section summaries
        List<String> sectionSummaries = new ArrayList<>();
        for (int i = 0; i < sectionFutures.size(); i++) {
            try {
                sectionSummaries.add(sectionFutures.get(i).get());
            } catch (Exception e) {
                logger.error("[Test] Error getting section summary {}: {}", i + 1, e.getMessage());
                sectionSummaries.add("Error generating section summary: " + e.getMessage());
            }
        }
        // Combine section summaries into a final summary
        String combinedSections = String.join("\n\n", sectionSummaries);
        int combinedInputTokens = estimateTokens(combinedSections);
        String finalPrompt = prepareSummaryPrompt(combinedSections, title, "File");
        dev.langchain4j.data.message.AiMessage finalResponse = chatModel.generate(new dev.langchain4j.data.message.UserMessage(finalPrompt)).content();
        String finalSummary = finalResponse.text();
        int finalOutputTokens = estimateTokens(finalSummary);
        
        logger.info("[Test] Final summary: {} chars (~{} tokens) from {} chars (~{} tokens)", 
                   finalSummary.length(), finalOutputTokens, combinedSections.length(), combinedInputTokens);
        logger.debug("[Test] Final summary: {}", abbreviate(finalSummary, 400));
        long endTime = System.currentTimeMillis();
        logger.info("[Test] Map-reduce summary generation completed in {} ms (Total: {} -> {} tokens)", 
                   (endTime - startTime), totalInputTokens, finalOutputTokens);
        return finalSummary;
    }

    // ==================== ADVANCED MEMORY OPTIMIZATION METHODS ====================
    
    /**
     * Check current memory usage and trigger GC if needed
     */
    private void checkMemoryAndGC() {
        long currentTime = System.currentTimeMillis();
        long lastGC = lastGCTime.get();
        
        // Check if enough time has passed since last GC
        if (currentTime - lastGC < GC_INTERVAL_MS) {
            return;
        }
        
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        double memoryUsage = (double) usedMemory / totalMemory;
        
        logger.debug("Memory usage: {:.2f}% ({} MB used / {} MB total)", 
                    String.format("%.2f", memoryUsage * 100), usedMemory / (1024 * 1024), totalMemory / (1024 * 1024));
        
        if (memoryUsage > MEMORY_THRESHOLD) {
            logger.info("High memory usage detected ({}%), triggering garbage collection", String.format("%.2f", memoryUsage * 100));
            System.gc();
            lastGCTime.set(currentTime);
            
            // Wait a bit for GC to complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Ultra-efficient streaming chunking that processes one chunk at a time
     */
    private void processChunksWithStreaming(String content, Consumer<String> chunkProcessor) {
        int chunkSize = 800; // Optimal chunk size for memory efficiency
        int overlap = 100;
        int contentLength = content.length();
        int chunkIndex = 0;
        
        logger.info("Starting streaming chunk processing for {} characters", contentLength);
        
        for (int i = 0; i < contentLength; i += chunkSize - overlap) {
            int endIndex = Math.min(i + chunkSize, contentLength);
            String chunk = content.substring(i, endIndex);
            
            // Process the chunk
            chunkProcessor.accept(chunk);
            
            chunkIndex++;
            processedChunks.incrementAndGet();
            
            // Check memory every N chunks
            if (chunkIndex % MEMORY_CHECK_INTERVAL == 0) {
                checkMemoryAndGC();
            }
            
            // Clear the chunk reference immediately
            chunk = null;
        }
        
        logger.info("Completed streaming chunk processing: {} chunks processed", chunkIndex);
    }
    
    /**
     * Ultra-efficient embedding processing with micro-batching
     */
    public void processEmbeddingsUltraEfficient(Resource resource) {
        if (resource.getContent() == null || resource.getContent().isEmpty()) {
            logger.warn("Resource content is empty, skipping embedding processing");
            return;
        }
        
        logger.info("Starting ultra-efficient embedding processing for resource: {}", resource.getId());
        long startTime = System.currentTimeMillis();
        
        try {
            // Use streaming chunking with immediate processing
            AtomicInteger chunkCount = new AtomicInteger(0);
            AtomicInteger embeddingCount = new AtomicInteger(0);
            
            processChunksWithStreaming(resource.getContent(), chunk -> {
                try {
                    // Generate embedding for this single chunk
                    float[] embedding = embeddingService.embed(chunk);
                    
                    // Create vector and upload immediately
                    PineconeVector vector = new PineconeVector();
                    vector.setId(generateChunkId(resource.getId(), chunkCount.get()));
                    vector.setValues(toFloatList(embedding));
                    vector.setMetadata(Map.of(
                        "resourceId", resource.getId().toString(),
                        "chunkIndex", String.valueOf(chunkCount.get()),
                        "content", chunk.substring(0, Math.min(chunk.length(), 100)) + "..."
                    ));
                    
                    // Upload to Pinecone immediately
                    List<PineconeVector> vectors = List.of(vector);
                    embeddingService.upsertVectors(vectors);
                    embeddingCount.incrementAndGet();
                    
                    // Clear references immediately
                    embedding = null;
                    vector = null;
                    vectors = null;
                    
                    logger.debug("Processed chunk {}: {} characters -> embedding uploaded", 
                                chunkCount.get(), chunk.length());
                    
                } catch (Exception e) {
                    logger.error("Error processing chunk {}: {}", chunkCount.get(), e.getMessage());
                }
                
                chunkCount.incrementAndGet();
            });
            
            long endTime = System.currentTimeMillis();
            logger.info("Ultra-efficient embedding processing completed: {} chunks, {} embeddings in {} ms", 
                       chunkCount.get(), embeddingCount.get(), (endTime - startTime));
            
        } catch (Exception e) {
            logger.error("Error in ultra-efficient embedding processing: {}", e.getMessage(), e);
            throw new RuntimeException("Embedding processing failed", e);
        }
    }
    
    /**
     * Memory-aware content generation with automatic scaling
     */
    public Resource generateAllContentMemoryOptimized(Long resourceId) {
        long startTime = System.currentTimeMillis();
        logger.info("Starting memory-optimized content generation for resource: {}", resourceId);
        
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found with ID: " + resourceId));
        
        if (resource.getContent() == null || resource.getContent().isEmpty()) {
            throw new IllegalStateException("Resource content is empty. Extract content first.");
        }
        
        int contentLength = resource.getContent().length();
        logger.info("Document size: {} characters", contentLength);
        
        // Check initial memory state
        checkMemoryAndGC();
        
        try {
            // Choose processing strategy based on content size and memory availability
            if (contentLength > 200000) {
                logger.info("Very large document detected, using ultra-streaming approach");
                return processUltraLargeDocument(resource);
            } else if (contentLength > 100000) {
                logger.info("Large document detected, using streaming approach");
                return processLargeDocumentStreaming(resource);
            } else {
                logger.info("Standard document size, using optimized unified approach");
                return processStandardDocumentOptimized(resource);
            }
        } catch (Exception e) {
            logger.error("Error in memory-optimized content generation: {}", e.getMessage(), e);
            return generateAllContentFallback(resourceId);
        } finally {
            // Final memory cleanup
            checkMemoryAndGC();
            long endTime = System.currentTimeMillis();
            logger.info("Memory-optimized content generation completed in {} ms", (endTime - startTime));
            long totalTime = endTime - startTime;
            logger.info("Total content generation time for resource {}: {} ms ({} seconds)", resourceId, totalTime, totalTime / 1000.0);
        }
    }
    
    /**
     * Process ultra-large documents with extreme memory efficiency
     */
    private Resource processUltraLargeDocument(Resource resource) {
        logger.info("Processing ultra-large document with extreme memory efficiency");
        
        // Use micro-batching for content generation
        String content = resource.getContent();
        int sectionSize = 15000; // Smaller sections for ultra-large docs
        int overlap = 1000;
        
        List<String> sections = new ArrayList<>();
        for (int i = 0; i < content.length(); i += sectionSize - overlap) {
            int endIndex = Math.min(i + sectionSize, content.length());
            sections.add(content.substring(i, endIndex));
        }
        
        logger.info("Split ultra-large document into {} sections", sections.size());
        
        // Process sections with memory monitoring
        ChatLanguageModel chatModel = getChatModel();
        StringBuilder combinedSummary = new StringBuilder();
        
        for (int i = 0; i < sections.size(); i++) {
            String section = sections.get(i);
            logger.info("Processing section {}/{} ({} characters)", i + 1, sections.size(), section.length());
            
            try {
                SummaryGenerator summaryGenerator = AiServices.builder(SummaryGenerator.class)
                        .chatLanguageModel(chatModel)
                        .build();
                
                String sectionSummary = summaryGenerator.generateSummary(
                    prepareSummaryPrompt(section, resource.getTitle(), resource.getClass().getSimpleName())
                );
                
                combinedSummary.append("Section ").append(i + 1).append(": ").append(sectionSummary).append("\n\n");
                
                // Clear section reference immediately
                section = null;
                sectionSummary = null;
                
                // Check memory after each section
                checkMemoryAndGC();
                
            } catch (Exception e) {
                logger.error("Error processing section {}: {}", i + 1, e.getMessage());
                combinedSummary.append("Section ").append(i + 1).append(": Error processing section\n\n");
            }
        }
        
        // Generate final summary from combined sections
        String finalSummary = generateFinalSummaryFromSections(combinedSummary.toString(), resource, chatModel);
        resource.setSummary(finalSummary);
        
        // Clear large objects
        combinedSummary = null;
        sections = null;
        
        // Process embeddings with ultra-efficiency
        processEmbeddingsUltraEfficient(resource);
        
        return resourceRepository.save(resource);
    }
    
    /**
     * Process large documents with streaming approach
     */
    private Resource processLargeDocumentStreaming(Resource resource) {
        logger.info("Processing large document with streaming approach");
        
        String content = handleLargeContent(resource.getContent(), 4000);
        ChatLanguageModel chatModel = getChatModel();
        
        // Generate content with memory monitoring
        UnifiedContentGenerator generator = AiServices.builder(UnifiedContentGenerator.class)
                .chatLanguageModel(chatModel)
                .build();
        
        String unifiedPrompt = createUnifiedPrompt(resource, content);
        String generatedJson = generator.generateContent(unifiedPrompt);
        
        // Process the response
        String extractedJson = extractJsonObject(generatedJson);
        JSONObject allContent = new JSONObject(extractedJson);
        
        // Set content with memory cleanup
        if (allContent.has("summary")) {
            resource.setSummary(allContent.getString("summary"));
        }
        if (allContent.has("flashcards")) {
            resource.setFlashcards(allContent.getJSONArray("flashcards").toString());
        }
        if (allContent.has("quiz")) {
            resource.setQuiz(allContent.getJSONArray("quiz").toString());
        }
        if (allContent.has("chapters")) {
            resource.setChapters(allContent.getJSONArray("chapters").toString());
        }
        
        resource.setGeneratedContent(allContent.toString());
        
        // Clear large objects
        content = null;
        generatedJson = null;
        extractedJson = null;
        allContent = null;
        
        // Process embeddings efficiently
        processEmbeddingsUltraEfficient(resource);
        
        return resourceRepository.save(resource);
    }
    
    /**
     * Process standard documents with optimization
     */
    private Resource processStandardDocumentOptimized(Resource resource) {
        logger.info("Processing standard document with optimization");
        
        String content = handleLargeContent(resource.getContent(), 4000);
        ChatLanguageModel chatModel = getChatModel();
        
        UnifiedContentGenerator generator = AiServices.builder(UnifiedContentGenerator.class)
                .chatLanguageModel(chatModel)
                .build();
        
        String unifiedPrompt = createUnifiedPrompt(resource, content);
        String generatedJson = generator.generateContent(unifiedPrompt);
        
        String extractedJson = extractJsonObject(generatedJson);
        JSONObject allContent = new JSONObject(extractedJson);
        
        // Set content
        if (allContent.has("summary")) {
            resource.setSummary(allContent.getString("summary"));
        }
        if (allContent.has("flashcards")) {
            resource.setFlashcards(allContent.getJSONArray("flashcards").toString());
        }
        if (allContent.has("quiz")) {
            resource.setQuiz(allContent.getJSONArray("quiz").toString());
        }
        if (allContent.has("chapters")) {
            resource.setChapters(allContent.getJSONArray("chapters").toString());
        }
        
        resource.setGeneratedContent(allContent.toString());
        
        // Process embeddings
        processEmbeddingsUltraEfficient(resource);
        
        return resourceRepository.save(resource);
    }
    
    /**
     * Generate final summary from combined section summaries
     */
    private String generateFinalSummaryFromSections(String combinedSections, Resource resource, ChatLanguageModel chatModel) {
        try {
            SummaryGenerator summaryGenerator = AiServices.builder(SummaryGenerator.class)
                    .chatLanguageModel(chatModel)
                    .build();
            
            String finalPrompt = prepareSummaryPrompt(combinedSections, resource.getTitle(), resource.getClass().getSimpleName());
            return summaryGenerator.generateSummary(finalPrompt);
        } catch (Exception e) {
            logger.error("Error generating final summary: {}", e.getMessage());
            return "Error generating final summary: " + e.getMessage();
        }
    }
    
    /**
     * Get current memory statistics
     */
    public Map<String, Object> getMemoryStatistics() {
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMemoryMB", totalMemory / (1024 * 1024));
        stats.put("freeMemoryMB", freeMemory / (1024 * 1024));
        stats.put("usedMemoryMB", usedMemory / (1024 * 1024));
        stats.put("maxMemoryMB", maxMemory / (1024 * 1024));
        stats.put("memoryUsagePercent", (double) usedMemory / totalMemory * 100);
        stats.put("processedChunks", processedChunks.get());
        stats.put("lastGCTime", lastGCTime.get());
        
        return stats;
    }

    private String extractJsonArrayIfCodeBlock(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("```json")) {
            int start = trimmed.indexOf("```json") + 7;
            int end = trimmed.indexOf("```", start);
            if (end > start) {
                return trimmed.substring(start, end).trim();
            }
        } else if (trimmed.startsWith("```")) {
            int start = trimmed.indexOf("```") + 3;
            int end = trimmed.indexOf("```", start);
            if (end > start) {
                return trimmed.substring(start, end).trim();
            }
        }
        return text;
    }

}