package com.uninote.backend.service;
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
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class LangChainContentService {

    private static final Logger logger = LoggerFactory.getLogger(LangChainContentService.class);
    
    @Autowired
    private AzureOpenAiConfig azureConfig;
    
    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ChunkingService chunkingService;

    @Autowired
    private EmbeddingService embeddingService;
    
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
                .temperature(0.7)
                .build();
    }
    
    /**
     * Generate all content types at once for a resource using a unified prompt
     */
    public Resource generateAllContent(Long resourceId) {
        logger.info("Generating all content for resource: {}", resourceId);
        
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found with ID: " + resourceId));
        
        if (resource.getContent() == null || resource.getContent().isEmpty()) {
            throw new IllegalStateException("Resource content is empty. Extract content first.");
        }
        
        // Check document size and use appropriate processing method
        int contentLength = resource.getContent().length();
        logger.info("Document size: {} characters", contentLength);
        
        try {
            if (contentLength > 40000) {
                // Very large document - use map-reduce approach
                logger.info("Using map-reduce approach for very large document");
                return processVeryLargeDocument(resource);
            } else {
                // Standard approach with improved content sampling for medium/large docs
                String content = handleLargeContent(resource.getContent(), 4000);
                
                // Continue with your existing unified approach using the improved content
                ChatLanguageModel chatModel = getChatModel();
                UnifiedContentGenerator generator = AiServices.builder(UnifiedContentGenerator.class)
                        .chatLanguageModel(chatModel)
                        .build();
                
                String unifiedPrompt = createUnifiedPrompt(resource, content);
                logger.info("Sending unified content generation request");
                String generatedJson = generator.generateContent(unifiedPrompt);
                logger.info("Received response from unified content generation");
                
                // Process the response as in your original method
                String extractedJson = extractJsonObject(generatedJson);
                JSONObject allContent = new JSONObject(extractedJson);
                
                // Extract and save all content types
                if (allContent.has("summary")) {
                    String summary = allContent.getString("summary");
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
                logger.info("Successfully generated all content types");
                try {
                    processEmbeddings(resource);
                } catch (Exception e) {
                    logger.error("Embedding processing failed: {}", e.getMessage(), e);
                }

                

                return resourceRepository.save(resource);
            }
        } catch (Exception e) {
            logger.error("Error in content generation: {}", e.getMessage(), e);
            
            logger.info("Falling back to individual content generation");
            return generateAllContentFallback(resourceId);
        }
    }
    
    /**
     * Fallback method to generate content individually if unified approach fails
     */
    private Resource generateAllContentFallback(Long resourceId) {
        logger.info("Using individual content generation for resource: {}", resourceId);
        
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found with ID: " + resourceId));
        
        String content = resource.getContent();
        ChatLanguageModel chatModel = getChatModel();
        
        try {
            // Generate summary 
            logger.info("Generating summary...");
            SummaryGenerator summaryGenerator = AiServices.builder(SummaryGenerator.class)
                    .chatLanguageModel(chatModel)
                    .build();
            String summary = summaryGenerator.generateSummary(prepareSummaryPrompt(content));
            resource.setSummary(summary);
            
            // Generate flashcards
            logger.info("Generating flashcards...");
            FlashcardGenerator flashcardGenerator = AiServices.builder(FlashcardGenerator.class)
                    .chatLanguageModel(chatModel)
                    .build();
            String flashcardsJson = flashcardGenerator.generateFlashcards(prepareFlashcardsPrompt(content));
            
            // Validate and clean flashcards JSON
            String flashcards = validateAndCleanJson(flashcardsJson, "flashcards");
            resource.setFlashcards(flashcards);
            
            // Generate quiz
            logger.info("Generating quiz...");
            QuizGenerator quizGenerator = AiServices.builder(QuizGenerator.class)
                    .chatLanguageModel(chatModel)
                    .build();
            String quizJson = quizGenerator.generateQuiz(prepareQuizPrompt(content));
            
            // Validate and clean quiz JSON
            String quiz = validateAndCleanJson(quizJson, "quiz");
            resource.setQuiz(quiz);
            
            // Generate chapters
            logger.info("Generating chapters...");
            ChapterGenerator chapterGenerator = AiServices.builder(ChapterGenerator.class)
                    .chatLanguageModel(chatModel)
                    .build();
            String chaptersJson = chapterGenerator.generateChapters(prepareChaptersPrompt(content));
            
            // Validate and clean chapters JSON
            String chapters = validateAndCleanJson(chaptersJson, "chapters");
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
            
            return resourceRepository.save(resource);
        } catch (Exception e) {
            logger.error("Error during content generation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate content", e);
        }
    }
    
    /**
     * Create a unified prompt for generating all content types at once
     */
    private String createUnifiedPrompt(Resource resource, String content) {
        String resourceType = resource.getClass().getSimpleName();
        String resourceTitle = resource.getTitle() != null ? resource.getTitle() : "Untitled Resource";
        
        return "You are an AI assistant. Your job is to generate four types of educational content from the given resource:\n\n" +
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
               "- Provide a **detailed and well-structured summary** of the content.\n" +
               "- The summary should be **rich in information**, capturing key concepts, important details, and examples where applicable.\n" +
               "- Aim for **at least 12 sentences**, ensuring completeness without excessive verbosity.\n" +
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
               "- **Do not include any extra text, markdown, or explanations. Only return a valid JSON object. Make sure to respond in greek.**";
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
     * Handle large content by splitting it into manageable chunks
     */
    private String handleLargeContent(String content, int maxTokens) {
        if (content.length() <= maxTokens * 4) {  
            return content;
        }
        
        Document document = Document.from(content);
        
        // Split the document
        DocumentSplitter splitter = DocumentSplitters.recursive(maxTokens / 3, 100);
        List<TextSegment> segments = splitter.split(document);
        
        StringBuilder reducedContent = new StringBuilder();
        
        // Always include beginning segments (introduction/context)
        int introSegments = Math.min(4, segments.size());
        for (int i = 0; i < introSegments; i++) {
            reducedContent.append(segments.get(i).text()).append("\n\n");
        }
        
        // Include some segments from the middle (main content)
        if (segments.size() > 8) {
            int midStartIdx = segments.size() / 3;
            int midSegments = Math.min(3, segments.size() - midStartIdx);
            reducedContent.append("... [content continues] ...\n\n");
            for (int i = 0; i < midSegments; i++) {
                reducedContent.append(segments.get(midStartIdx + i).text()).append("\n\n");
            }
        }
        
        // Include segments from the end (conclusion/summary)
        if (segments.size() > 6) {
            int endStartIdx = Math.max(introSegments, segments.size() - 3);
            reducedContent.append("... [content continues] ...\n\n");
            for (int i = endStartIdx; i < segments.size(); i++) {
                reducedContent.append(segments.get(i).text()).append("\n\n");
            }
        }
        
        logger.info("Processed content from beginning, middle, and end. Final length: {}", reducedContent.length());
        return reducedContent.toString();
    }

    private Resource processVeryLargeDocument(Resource resource) {
        logger.info("Using map-reduce approach for very large document: {}", resource.getId());
        
        // Create a chat model
        ChatLanguageModel chatModel = getChatModel();
        
        // Split the document into major sections
        String content = resource.getContent();
        List<TextSegment> sections = splitIntoMajorSections(content);
        logger.info("Split document into {} major sections", sections.size());
        
        // Process each section to get section-specific content
        JSONArray allFlashcards = new JSONArray();
        JSONArray allQuizQuestions = new JSONArray();
        JSONArray allChapters = new JSONArray();
        JSONArray allRelations = new JSONArray();
        StringBuilder summaryBuilder = new StringBuilder();
        
        // Add document title to summary
        summaryBuilder.append("# ").append(resource.getTitle() != null ? resource.getTitle() : "Document Summary").append("\n\n");
        
        // Process each section
        for (int i = 0; i < sections.size(); i++) {
            TextSegment section = sections.get(i);
            logger.info("Processing section {}/{}", i+1, sections.size());
            
            try {
                // Create section-specific prompt
                String sectionPrompt = createSectionPrompt(resource, section.text(), i+1, sections.size());
                
                // Generate content for this section
                UnifiedContentGenerator generator = AiServices.builder(UnifiedContentGenerator.class)
                        .chatLanguageModel(chatModel)
                        .build();
                
                String sectionResponse = generator.generateContent(sectionPrompt);
                String extractedJson = extractJsonObject(sectionResponse);
                JSONObject sectionContent = new JSONObject(extractedJson);
                
                // Extract section summary
                if (sectionContent.has("summary")) {
                    summaryBuilder.append("## Section ").append(i+1).append("\n\n");
                    summaryBuilder.append(sectionContent.getString("summary")).append("\n\n");
                }
                
                // Collect flashcards
                if (sectionContent.has("flashcards")) {
                    JSONArray sectionFlashcards = sectionContent.getJSONArray("flashcards");
                    for (int j = 0; j < sectionFlashcards.length(); j++) {
                        allFlashcards.put(sectionFlashcards.getJSONObject(j));
                    }
                }
                
                // Collect quiz questions
                if (sectionContent.has("quiz")) {
                    JSONArray sectionQuiz = sectionContent.getJSONArray("quiz");
                    for (int j = 0; j < sectionQuiz.length(); j++) {
                        allQuizQuestions.put(sectionQuiz.getJSONObject(j));
                    }
                }
                
                // Collect relations
                if (sectionContent.has("relations")) {
                    JSONArray sectionRelations = sectionContent.getJSONArray("relations");
                    for (int j = 0; j < sectionRelations.length(); j++) {
                        allRelations.put(sectionRelations.getJSONObject(j));
                    }
                }
                
                // Add section information to chapters
                if (sectionContent.has("chapters")) {
                    JSONArray sectionChapters = sectionContent.getJSONArray("chapters");
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
                }
                
            } catch (Exception e) {
                logger.error("Error processing section {}: {}", i+1, e.getMessage());
            }
        }
        
        // Combine all content
        String finalSummary = summaryBuilder.toString();
        resource.setSummary(finalSummary);
        resource.setFlashcards(allFlashcards.toString());
        resource.setQuiz(allQuizQuestions.toString());
        resource.setChapters(allChapters.toString());
        
        // Create combined generated content
        JSONObject generatedContent = new JSONObject();
        generatedContent.put("summary", finalSummary);
        generatedContent.put("flashcards", allFlashcards);
        generatedContent.put("quiz", allQuizQuestions);
        generatedContent.put("chapters", allChapters);
        generatedContent.put("relations", allRelations);
        
        resource.setGeneratedContent(generatedContent.toString());
        resourceRepository.save(resource);
        processEmbeddings(resource);
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
        DocumentSplitter splitter = DocumentSplitters.recursive(200000, 2000);
        Document document = Document.from(content);
        return splitter.split(document);
    }
    
    /**
     * Create a prompt for processing an individual document section
     */
    private String createSectionPrompt(Resource resource, String sectionText, int sectionNumber, int totalSections) {
        String resourceType = resource.getClass().getSimpleName();
        String resourceTitle = resource.getTitle() != null ? resource.getTitle() : "Untitled Resource";
        
        return "You are an AI assistant processing Section " + sectionNumber + " of " + totalSections + 
               " from a document. Generate educational content from this section:\n\n" +
               
               "### QUIZ\n" +
               "- Generate a high-quality multiple-choice quiz from this section.\n" +
               "- Each question should test key concepts in this section only.\n" +
               "- Provide exactly **four** answer choices per question, with **only one correct answer**.\n\n" +
               
               "### FLASHCARDS\n" +
               "- Generate educational flashcards for key terms and concepts in this section.\n" +
               "- Each flashcard should focus on a concept from this section only.\n" +
               "- Provide a concise answer and helpful hint.\n\n" +
               
               "### SUMMARY\n" +
               "- Provide a detailed summary of this section only.\n" +
               "- Capture the key points and concepts from this specific section.\n" +
               "- Format the summary in Markdown.\n\n" +
               
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
               "Only respond in Greek.";
    }
    
    // The individual prompt methods are kept for backward compatibility
    
    private String prepareSummaryPrompt(String content) {
        String truncatedContent = handleLargeContent(content, 3000);
        
        PromptTemplate template = PromptTemplate.from(
            "- Provide a **detailed and well-structured summary** of the content.\n" +
            "- The summary should be **rich in information**, capturing key concepts, important details, and examples where applicable.\n" +
            "- Aim for **at least 12 sentences**, ensuring completeness without excessive verbosity.\n" +
            "- Format the summary in **Markdown**, using bullet points, headings, and emphasis where necessary.\n\n" +
            "Text to summarize:\n{{content}}"
        );
        
        return template.apply(Map.of("content", truncatedContent)).text();
    }
    
    private String prepareFlashcardsPrompt(String content) {
        String truncatedContent = handleLargeContent(content, 2500);

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

    
    private String prepareQuizPrompt(String content) {
        String truncatedContent = handleLargeContent(content, 2500);
        
        PromptTemplate template = PromptTemplate.from(
            "Create a multiple-choice quiz based on the following text. Each question should have " +
            "one correct answer and three incorrect answers. Return the quiz as a JSON array of objects " +
            "with 'question', 'correctAnswer', and 'incorrectAnswers' (an array) fields. " +
            "Format the output as a JSON array without any markdown or code blocks - just pure JSON.\n\n" +
            "Text for quiz:\n{{content}}\n\n" +
            "Example format:\n[{\"question\":\"Question 1\",\"correctAnswer\":\"Correct answer\",\"incorrectAnswers\":[\"Wrong 1\",\"Wrong 2\",\"Wrong 3\"]}]"
        );
        
        return template.apply(Map.of("content", truncatedContent)).text();
    }
    
    private String prepareChaptersPrompt(String content) {
        String truncatedContent = handleLargeContent(content, 100000);
        
        PromptTemplate template = PromptTemplate.from(
            "Divide the following text into logical chapters or sections. Each chapter should have " +
            "a title and content. Return the chapters as a JSON array of objects with 'title' and 'content' fields. " +
            "Format the output as a JSON array without any markdown or code blocks - just pure JSON.\n\n" +
            "Text to divide into chapters:\n{{content}}\n\n" +
            "Example format:\n[{\"title\":\"Chapter 1 Title\",\"content\":\"Chapter 1 content\"},{\"title\":\"Chapter 2 Title\",\"content\":\"Chapter 2 content\"}]"
        );
        
        return template.apply(Map.of("content", truncatedContent)).text();
    }
    
    // Individual generation methods are kept for backward compatibility
    public Resource generateSummary(Long resourceId) {
        logger.info("Generating summary for resource: {}", resourceId);
        
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found with ID: " + resourceId));
        
        if (resource.getContent() == null || resource.getContent().isEmpty()) {
            throw new IllegalStateException("Resource content is empty. Extract content first.");
        }
        
        ChatLanguageModel chatModel = getChatModel();
        SummaryGenerator generator = AiServices.builder(SummaryGenerator.class)
                .chatLanguageModel(chatModel)
                .build();
        
        String summary = generator.generateSummary(prepareSummaryPrompt(resource.getContent()));
        
        resource.setSummary(summary);
        return resourceRepository.save(resource);
    }
    
    public Resource generateFlashcards(Long resourceId) {
        logger.info("Generating flashcards for resource: {}", resourceId);
        
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found with ID: " + resourceId));
        
        if (resource.getContent() == null || resource.getContent().isEmpty()) {
            throw new IllegalStateException("Resource content is empty. Extract content first.");
        }
        
        ChatLanguageModel chatModel = getChatModel();
        FlashcardGenerator generator = AiServices.builder(FlashcardGenerator.class)
                .chatLanguageModel(chatModel)
                .build();
        
        String flashcards = generator.generateFlashcards(prepareFlashcardsPrompt(resource.getContent()));
        
        resource.setFlashcards(flashcards);
        return resourceRepository.save(resource);
    }
    
    public Resource generateQuiz(Long resourceId) {
        logger.info("Generating quiz for resource: {}", resourceId);
        
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found with ID: " + resourceId));
        
        if (resource.getContent() == null || resource.getContent().isEmpty()) {
            throw new IllegalStateException("Resource content is empty. Extract content first.");
        }
        
        ChatLanguageModel chatModel = getChatModel();
        QuizGenerator generator = AiServices.builder(QuizGenerator.class)
                .chatLanguageModel(chatModel)
                .build();
        
        String quiz = generator.generateQuiz(prepareQuizPrompt(resource.getContent()));
        
        resource.setQuiz(quiz);
        return resourceRepository.save(resource);
    }
    
    public Resource generateChapters(Long resourceId) {
        logger.info("Generating chapters for resource: {}", resourceId);
        
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found with ID: " + resourceId));
        
        if (resource.getContent() == null || resource.getContent().isEmpty()) {
            throw new IllegalStateException("Resource content is empty. Extract content first.");
        }
        
        ChatLanguageModel chatModel = getChatModel();
        ChapterGenerator generator = AiServices.builder(ChapterGenerator.class)
                .chatLanguageModel(chatModel)
                .build();
        
        String chapters = generator.generateChapters(prepareChaptersPrompt(resource.getContent()));
        
        resource.setChapters(chapters);
        return resourceRepository.save(resource);
    }

    @Async
    public void generateAllContentAsync(Long resourceId) {
        generateAllContent(resourceId);
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
        logger.info("Starting embedding process for resource ID: {}", resource.getId());
    
        List<String> chunks = chunkingService.splitIntoChunks(resource.getContent());
        logger.debug("Split content into {} chunks", chunks.size());
    
        List<PineconeVector> records = new ArrayList<>();
        int chunkIndex = 0;
    
        for (String chunk : chunks) {
            logger.info("Processing chunk index {}: {}...", chunkIndex, abbreviate(chunk, 100));
    
            try {
                float[] embedding = embeddingService.embed(chunk);
                logger.info("Generated embedding for chunk index {}", chunkIndex);
    
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("resource_id", resource.getId());
                metadata.put("chunk_index", chunkIndex);
                metadata.put("chunk_text", chunk);
    
                List<Float> embeddingList = toFloatList(embedding);
                PineconeVector vector = new PineconeVector(generateChunkId(resource.getId(), chunkIndex), embeddingList, metadata);
                records.add(vector);
            } catch (Exception e) {
                logger.error("Failed to process embedding for chunk index {}: {}", chunkIndex, e.getMessage(), e);
            }
    
            chunkIndex++;
        }
    
        try {
            embeddingService.upsertVectors(records);
            logger.info("Successfully upserted {} vectors into Pinecone for resource ID: {}", records.size(), resource.getId());
        } catch (Exception e) {
            logger.error("Failed to upsert vectors into Pinecone: {}", e.getMessage(), e);
        }
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

        String flashcardsJson = flashcardGenerator.generateFlashcards(prepareFlashcardsPrompt(resource.getContent()));
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


}