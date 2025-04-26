package com.uninote.backend.service;
import com.uninote.backend.config.AzureOpenAiConfig;
import com.uninote.backend.entity.Resource;
import com.uninote.backend.repository.ResourceRepository;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;
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

import java.util.List;
import java.util.Map;


@Service
public class LangChainContentService {

    private static final Logger logger = LoggerFactory.getLogger(LangChainContentService.class);
    
    @Autowired
    private AzureOpenAiConfig azureConfig;
    
    @Autowired
    private ResourceRepository resourceRepository;
    
    @SystemMessage("You are an educational assistant that creates concise, comprehensive summaries.")
    private interface SummaryGenerator {
        String generateSummary(String content);
    }
    
    // Interface for flashcard generation
    @SystemMessage("You are an educational assistant that creates effective flashcards for learning.")
    private interface FlashcardGenerator {
        String generateFlashcards(String content);
    }
    
    // Interface for quiz generation
    @SystemMessage("You are an educational assistant that creates challenging but fair quiz questions.")
    private interface QuizGenerator {
        String generateQuiz(String content);
    }
    
    // Interface for chapter detection and organization
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
     * Generate a summary for a resource
     */
    public Resource generateSummary(Long resourceId) {
        logger.info("Generating summary for resource: {}", resourceId);
        
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found with ID: " + resourceId));
        
        if (resource.getContent() == null || resource.getContent().isEmpty()) {
            throw new IllegalStateException("Resource content is empty. Extract content first.");
        }
        
        // Create the LangChain service for summary generation
        ChatLanguageModel chatModel = getChatModel();
        SummaryGenerator generator = AiServices.builder(SummaryGenerator.class)
                .chatLanguageModel(chatModel)
                .build();
        
        // Generate the summary
        String summary = generator.generateSummary(prepareSummaryPrompt(resource.getContent()));
        
        // Save the summary to the resource
        resource.setSummary(summary);
        return resourceRepository.save(resource);
    }
    
    /**
     * Generate flashcards for a resource
     */
    public Resource generateFlashcards(Long resourceId) {
        logger.info("Generating flashcards for resource: {}", resourceId);
        
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found with ID: " + resourceId));
        
        if (resource.getContent() == null || resource.getContent().isEmpty()) {
            throw new IllegalStateException("Resource content is empty. Extract content first.");
        }
        
        // Create the LangChain service for flashcard generation
        ChatLanguageModel chatModel = getChatModel();
        FlashcardGenerator generator = AiServices.builder(FlashcardGenerator.class)
                .chatLanguageModel(chatModel)
                .build();
        
        // Generate the flashcards
        String flashcards = generator.generateFlashcards(prepareFlashcardsPrompt(resource.getContent()));
        
        // Save the flashcards to the resource
        resource.setFlashcards(flashcards);
        return resourceRepository.save(resource);
    }
    
    /**
     * Generate quiz questions for a resource
     */
    public Resource generateQuiz(Long resourceId) {
        logger.info("Generating quiz for resource: {}", resourceId);
        
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found with ID: " + resourceId));
        
        if (resource.getContent() == null || resource.getContent().isEmpty()) {
            throw new IllegalStateException("Resource content is empty. Extract content first.");
        }
        
        // Create the LangChain service for quiz generation
        ChatLanguageModel chatModel = getChatModel();
        QuizGenerator generator = AiServices.builder(QuizGenerator.class)
                .chatLanguageModel(chatModel)
                .build();
        
        // Generate the quiz
        String quiz = generator.generateQuiz(prepareQuizPrompt(resource.getContent()));
        
        // Save the quiz to the resource
        resource.setQuiz(quiz);
        return resourceRepository.save(resource);
    }
    
    /**
     * Generate chapters for a resource
     */
    public Resource generateChapters(Long resourceId) {
        logger.info("Generating chapters for resource: {}", resourceId);
        
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found with ID: " + resourceId));
        
        if (resource.getContent() == null || resource.getContent().isEmpty()) {
            throw new IllegalStateException("Resource content is empty. Extract content first.");
        }
        
        // Create the LangChain service for chapter generation
        ChatLanguageModel chatModel = getChatModel();
        ChapterGenerator generator = AiServices.builder(ChapterGenerator.class)
                .chatLanguageModel(chatModel)
                .build();
        
        // Generate the chapters
        String chapters = generator.generateChapters(prepareChaptersPrompt(resource.getContent()));
        
        // Save the chapters to the resource
        resource.setChapters(chapters);
        return resourceRepository.save(resource);
    }
    
    /**
     * Generate all content types at once for a resource
     */
    public Resource generateAllContent(Long resourceId) {
        logger.info("Generating all content for resource: {}", resourceId);
        
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found with ID: " + resourceId));
        
        if (resource.getContent() == null || resource.getContent().isEmpty()) {
            throw new IllegalStateException("Resource content is empty. Extract content first.");
        }
        
        // Generate individual content types
        String content = resource.getContent();
        
        // Create a chat model to be shared
        ChatLanguageModel chatModel = getChatModel();
        
        // Generate summary 
        SummaryGenerator summaryGenerator = AiServices.builder(SummaryGenerator.class)
                .chatLanguageModel(chatModel)
                .build();
        String summary = summaryGenerator.generateSummary(prepareSummaryPrompt(content));
        
        // Generate flashcards
        FlashcardGenerator flashcardGenerator = AiServices.builder(FlashcardGenerator.class)
                .chatLanguageModel(chatModel)
                .build();
        String flashcards = flashcardGenerator.generateFlashcards(prepareFlashcardsPrompt(content));
        
        // Generate quiz
        QuizGenerator quizGenerator = AiServices.builder(QuizGenerator.class)
                .chatLanguageModel(chatModel)
                .build();
        String quiz = quizGenerator.generateQuiz(prepareQuizPrompt(content));
        
        // Generate chapters
        ChapterGenerator chapterGenerator = AiServices.builder(ChapterGenerator.class)
                .chatLanguageModel(chatModel)
                .build();
        String chapters = chapterGenerator.generateChapters(prepareChaptersPrompt(content));
        
        // Update the resource with all generated content
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
            generatedContent.put("flashcards", flashcards);
        }
        
        try {
            generatedContent.put("quiz", new JSONArray(quiz));
        } catch (Exception e) {
            generatedContent.put("quiz", quiz);
        }
        
        try {
            generatedContent.put("chapters", new JSONArray(chapters));
        } catch (Exception e) {
            generatedContent.put("chapters", chapters);
        }
        
        resource.setGeneratedContent(generatedContent.toString());
        
        return resourceRepository.save(resource);
    }
    
    /**
     * Handle large content by splitting it into manageable chunks
     */
    private String handleLargeContent(String content, int maxTokens) {
        if (content.length() <= maxTokens * 4) {  // Rough approximation of tokens to chars
            return content;
        }
        
        // Create a document from the content
        Document document = Document.from(content);
        
        // Split the document
        DocumentSplitter splitter = DocumentSplitters.recursive(maxTokens, 100);
        List<TextSegment> segments = splitter.split(document);
        
        // For large documents, use only the first few segments
        StringBuilder reducedContent = new StringBuilder();
        int segmentsToUse = Math.min(5, segments.size());
        
        for (int i = 0; i < segmentsToUse; i++) {
            reducedContent.append(segments.get(i).text()).append("\n\n");
        }
        
        return reducedContent.toString();
    }
    
    /**
     * Prepare a prompt for summary generation
     */
    private String prepareSummaryPrompt(String content) {
        // Truncate content if needed to avoid token limits
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
    
    /**
     * Prepare a prompt for flashcard generation
     */
    private String prepareFlashcardsPrompt(String content) {
        // Truncate content if needed to avoid token limits
        String truncatedContent = handleLargeContent(content, 2500);
        
        PromptTemplate template = PromptTemplate.from(
            "Create a set of flashcards based on the following text. Each flashcard should have " +
            "a 'front' with a question or concept, and a 'back' with the answer or explanation. " +
            "Return the flashcards as a JSON array of objects with 'front' and 'back' fields. " +
            "Format the output as a JSON array without any markdown or code blocks - just pure JSON.\n\n" +
            "Text for flashcards:\n{{content}}\n\n" +
            "Example format:\n[{\"front\":\"Question 1\",\"back\":\"Answer 1\"},{\"front\":\"Question 2\",\"back\":\"Answer 2\"}]"
        );
        
        return template.apply(Map.of("content", truncatedContent)).text();
    }
    
    /**
     * Prepare a prompt for quiz generation
     */
    private String prepareQuizPrompt(String content) {
        // Truncate content if needed to avoid token limits
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
    
    /**
     * Prepare a prompt for chapter generation
     */
    private String prepareChaptersPrompt(String content) {
        // Truncate content if needed to avoid token limits
        String truncatedContent = handleLargeContent(content, 3000);
        
        PromptTemplate template = PromptTemplate.from(
            "Divide the following text into logical chapters or sections. Each chapter should have " +
            "a title and content. Return the chapters as a JSON array of objects with 'title' and 'content' fields. " +
            "Format the output as a JSON array without any markdown or code blocks - just pure JSON.\n\n" +
            "Text to divide into chapters:\n{{content}}\n\n" +
            "Example format:\n[{\"title\":\"Chapter 1 Title\",\"content\":\"Chapter 1 content\"},{\"title\":\"Chapter 2 Title\",\"content\":\"Chapter 2 content\"}]"
        );
        
        return template.apply(Map.of("content", truncatedContent)).text();
    }

    @Async
    public void generateAllContentAsync(Long resourceId) {
        generateAllContent(resourceId);
    }
}