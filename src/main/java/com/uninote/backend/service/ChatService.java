package com.uninote.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.azure.ai.openai.models.ChatCompletionsJsonResponseFormat;
import com.azure.core.http.HttpClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.uninote.backend.dto.ChatHistoryDto;
import com.uninote.backend.dto.FileResourceDTO;
import com.uninote.backend.dto.MessageDTO;
import com.uninote.backend.dto.NoteResourceDTO;
import com.uninote.backend.dto.ResourceDTO;
import com.uninote.backend.dto.TranscriptSnippetDto;
import com.uninote.backend.dto.YouTubeResourceDTO;
import com.uninote.backend.entity.Chat;
import com.uninote.backend.entity.FileResource;
import com.uninote.backend.entity.Message;
import com.uninote.backend.entity.MessageMedia;
import com.uninote.backend.entity.NoteResource;
import com.uninote.backend.entity.Resource;
import com.uninote.backend.entity.ResourceChat;
import com.uninote.backend.entity.SpaceChat;
import com.uninote.backend.entity.YouTubeResource;
import com.uninote.backend.repository.MessageRepository;
import com.uninote.backend.repository.ResourceChatRepository;
import com.uninote.backend.repository.ResourceRepository;
import com.uninote.backend.repository.SpaceChatRepository;
import com.uninote.backend.repository.SpaceResourceRepository;
import com.uninote.backend.service.embedding.EmbeddingService;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.ImageContent;
import java.util.Base64;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.AiMessage;

import com.azure.ai.openai.models.ChatCompletionsJsonResponseFormat;


import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileStore;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.uninote.backend.config.AzureOpenAiConfig;
import com.uninote.backend.dto.ChatRequest;
import com.uninote.backend.entity.Space;
import com.uninote.backend.repository.ChatRepository;
import com.uninote.backend.repository.MessageMediaRepository;
import com.uninote.backend.service.PdfContentAnalyzerService;
import com.fasterxml.jackson.core.type.TypeReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.apache.http.HttpResponse;
import org.springframework.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import org.springframework.http.HttpMethod;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);


    private final RestTemplate restTemplate = new RestTemplate();
    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ResourceChatRepository resourceChatRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AzureOpenAiConfig azureConfig;

    @Autowired
    private SpaceChatRepository spaceChatRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private MessageMediaRepository messageMediaRepository;

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private SpaceResourceRepository spaceResourceRepository;

    @Autowired
    private ResourceRepository resourceRepository;


    @Autowired
    private PromptService promptService;

    @Autowired
    private PdfContentAnalyzerService pdfContentAnalyzerService;

    @Autowired
    private LangfuseClient langfuseClient;

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    private static final int MAX_RESOURCE_CHARS = 130000;


    public List<Message> getChatMessagesByChat(Chat chat) {
        return messageRepository.findByChatOrderByCreatedAtAsc(chat);
    }

    public List<Message> getChatMessagesById(Chat chat) {
        return getChatMessagesByChat(chat);
    }

    public Map<String, Object> getChatHistory(String chatUuid, String jwt) {
        String url = "http://localhost:8000/api/chat/{chatUuid}/history/";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwt); 

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class, chatUuid); 
            Map<String, Object> chatHistory = response.getBody();
            return chatHistory;
        } catch (Exception e) {
            e.printStackTrace();  
            return null; 
        }
    }


    public ChatHistoryDto getChatHistory(String chatUuid) {
        log.info("Fetching chat history for chatUuid: {}", chatUuid);
        long startTime = System.currentTimeMillis();

        ResourceChat resourceChat = resourceChatRepository.findByChat_Uuid(chatUuid)
            .orElse(null);
        log.info("Found resource of type: {}");
        long queryTime = System.currentTimeMillis();
        log.info("Database query took {} ms", queryTime - startTime);

        ResourceDTO resourceDTO = null;
        String title;

        Chat chat;
        if (resourceChat != null) {
            log.info("Found ResourceChat — mapping resource.");
            chat = resourceChat.getChat();
            Resource resource = resourceChat.getResource();
            title = resource.getTitle();

            if (resource instanceof FileResource) {
                FileResource fr = (FileResource) resource;
                log.info("Mapping FileResource with id: {}", fr.getId());

                FileResourceDTO dto = new FileResourceDTO();
                dto.setId(fr.getId());
                dto.setTitle(fr.getTitle());
                dto.setCreatedAt(fr.getCreatedAt());
                dto.setSummary(fr.getSummary());
                Boolean isDigitized = pdfContentAnalyzerService.isValidContent(fr.getContent());
                if (isDigitized) {
                    dto.setContent(fr.getContent());
                } else {
                    dto.setContent(null);
                }
                dto.setSupabaseFileUrl(fr.getFileUrl());
                dto.setFlashcards(fr.getFlashcards());
                dto.setChapters(fr.getChapters());
                dto.setQuizzes(fr.getQuiz());

                resourceDTO = dto;

            } else if (resource instanceof NoteResource) {
                NoteResource nr = (NoteResource) resource;
                log.info("Mapping YouTubeResource with id: {}", nr.getId());

                NoteResourceDTO dto = new NoteResourceDTO();
                dto.setId(nr.getId());
                dto.setTitle(nr.getTitle());
                dto.setCreatedAt(nr.getCreatedAt());
                dto.setSummary(nr.getSummary());
                dto.setChapters(nr.getChapters());
                dto.setFlashcards(nr.getFlashcards());
                dto.setQuizzes(nr.getQuiz());
                Boolean isDigitized = pdfContentAnalyzerService.isValidContent(nr.getContent());
                if (isDigitized) {
                    dto.setContent(nr.getContent());
                } else {
                    dto.setContent(null);
                }               
                dto.setFileUrl(nr.getNote().getPdfUrl());

                resourceDTO = dto;
            } else if (resource instanceof YouTubeResource) {
                YouTubeResource nr = (YouTubeResource) resource;
                log.info("Mapping YouTubeResource with id: {}", nr.getId());

                YouTubeResourceDTO dto = new YouTubeResourceDTO();
                dto.setId(nr.getId());
                dto.setTitle(nr.getTitle());
                dto.setCreatedAt(nr.getCreatedAt());
                dto.setSummary(nr.getSummary());
                dto.setContent(nr.getContent());
                dto.setChapters(nr.getChapters());
                dto.setFlashcards(nr.getFlashcards());
                dto.setQuizzes(nr.getQuiz());
                dto.setContent(nr.getContent());
                dto.setYoutubeUrl(nr.getYoutubeUrl());
                try {
                    String snippetsJson = nr.getSnippets();
                    if (snippetsJson != null && !snippetsJson.trim().isEmpty()) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        List<TranscriptSnippetDto> snippetList = objectMapper.readValue(
                            snippetsJson,
                            new TypeReference<List<TranscriptSnippetDto>>() {}
                        );
                        dto.setTranscriptSnippets(snippetList);
                    } else {
                        dto.setTranscriptSnippets(Collections.emptyList());
                    }
                } catch (Exception e) {
                    logger.error("Failed to parse snippets JSON from DB: {}", e.getMessage());
                    dto.setTranscriptSnippets(Collections.emptyList());
                }


                resourceDTO = dto;
                

            } else {
                log.error("Unsupported resource type: {}", resource.getClass().getSimpleName());
                throw new IllegalStateException("Unsupported resource type: " + resource.getClass().getSimpleName());
            }
        } else {
            log.warn("No ResourceChat found — treating as SimpleChat.");
            chat = chatRepository.findByUuid(chatUuid)
                    .orElseThrow(() -> new RuntimeException("Chat not found for UUID: " + chatUuid));
            title = chat.getTitle() != null ? chat.getTitle() : "Untitled Chat";
        }

        Long chatId = chat.getId();
        log.info("Fetching messages for chatId: {}", chatId);

        List<MessageDTO> messageDtos = messageRepository.findAllByChatIdOrderByCreatedAtAsc(chatId)
            .stream()
            .map(m -> new MessageDTO(m))
            .collect(Collectors.toList());

        log.info("Retrieved {} messages for chatId: {}", messageDtos.size(), chatId);

        return new ChatHistoryDto(
            chatId,
            title,
            resourceDTO, 
            messageDtos
        );
    }
    
    private List<Map<String, String>> processUploadedImagesForChat(List<MultipartFile> uploadedImages, Chat chat) {
        List<Map<String, String>> uploadedMediaMeta = new ArrayList<>();
        if (uploadedImages != null && !uploadedImages.isEmpty()) {
            for (MultipartFile image : uploadedImages) {
                try {
                    String fileName = "chat_" + chat.getId() + "/" + UUID.randomUUID() + "_" + image.getOriginalFilename();
                    String uploadedImageUrl = fileStorageService.uploadFile(image, fileName, chat.getId(), "chat_media");
                    logger.info("Uploaded image URL: {}", uploadedImageUrl);
                    
                    String mimeType = determineMimeType(image.getOriginalFilename());
                    String mediaType = mimeType.split("/")[0]; 
                    
                    Map<String, String> mediaMeta = new HashMap<>();
                    mediaMeta.put("url", uploadedImageUrl);
                    mediaMeta.put("type", mediaType);
                    mediaMeta.put("filename", image.getOriginalFilename());
                    uploadedMediaMeta.add(mediaMeta);
                } catch (Exception e) {
                    logger.warn("Failed to upload/process image: {}", e.getMessage());
                }
            }
        }
        return uploadedMediaMeta;
    }

    private Map<String, Object> prepareChatRequestBody(String systemPrompt, List<Message> previousMessages, 
            String userMessage, List<MultipartFile> uploadedImages, boolean isFirstMessage) throws IOException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 3000);
        
        List<Map<String, Object>> messages = new ArrayList<>();
        
        Map<String, Object> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);
        
        for (Message message : previousMessages) {
            message.getMedia().size();
            
            if (message.getUserMessage() != null) {
                Map<String, Object> userMsg = new HashMap<>();
                userMsg.put("role", "user");
                userMsg.put("content", message.getUserMessage());
                messages.add(userMsg);
            }
            
            if (message.getServiceResponse() != null) {
                Map<String, Object> aiMsg = new HashMap<>();
                aiMsg.put("role", "assistant");
                aiMsg.put("content", message.getServiceResponse());
                messages.add(aiMsg);
            }
        }
        
        Map<String, Object> currentUserMsg = new HashMap<>();
        currentUserMsg.put("role", "user");
        
        if (uploadedImages != null && !uploadedImages.isEmpty()) {
            List<Map<String, Object>> contentList = new ArrayList<>();
            
            Map<String, Object> textContent = new HashMap<>();
            textContent.put("type", "text");
            textContent.put("text", userMessage);
            contentList.add(textContent);
            
            for (MultipartFile image : uploadedImages) {
                Map<String, Object> imageContent = new HashMap<>();
                imageContent.put("type", "image_url");
                
                Map<String, String> imageUrl = new HashMap<>();
                String base64Image = Base64.getEncoder().encodeToString(image.getBytes());
                imageUrl.put("url", "data:" + image.getContentType() + ";base64," + base64Image);
                
                imageContent.put("image_url", imageUrl);
                contentList.add(imageContent);
            }
            currentUserMsg.put("content", contentList);
        } else {
            currentUserMsg.put("content", userMessage);
        }
        messages.add(currentUserMsg);
        
        requestBody.put("messages", messages);
        
        if (isFirstMessage) {
            Map<String, Object> responseFormat = new HashMap<>();
            responseFormat.put("type", "json_object");
            requestBody.put("response_format", responseFormat);
        }
        
        requestBody.put("stream", true);
        return requestBody;
    }

    private Long saveChatMessageAndMedia(Chat chat, String userMessage, String textResponse, 
            List<Map<String, String>> uploadedMediaMeta) {
        Message message = new Message();
        message.setChat(chat);
        message.setUserMessage(userMessage);
        message.setServiceResponse(textResponse);
        message.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        Message savedMessage = messageRepository.save(message);
        
        for (Map<String, String> media : uploadedMediaMeta) {
            MessageMedia messageMedia = new MessageMedia();
            messageMedia.setMessage(savedMessage);
            messageMedia.setMediaUrl(media.get("url"));
            messageMedia.setMediaType(media.get("type")); 
            messageMedia.setOriginalFilename(media.get("filename"));
            messageMediaRepository.save(messageMedia);
        }
        
        chat.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        chatRepository.save(chat);
        
        return savedMessage.getId();
    }

    private void handleFirstChatMessage(String finalResponse, Chat chat) {
        try {
            String jsonString = finalResponse;
            if (finalResponse.contains("```json")) {
                jsonString = finalResponse.substring(
                    finalResponse.indexOf("```json") + 7, 
                    finalResponse.lastIndexOf("```")
                ).trim();
            } else if (finalResponse.contains("```")) {
                jsonString = finalResponse.substring(
                    finalResponse.indexOf("```") + 3, 
                    finalResponse.lastIndexOf("```")
                ).trim();
            }
            
            jsonString = jsonString.replaceAll("^```(json)?", "").replaceAll("```$", "").trim();
            JsonNode parsedJson = objectMapper.readTree(jsonString);
            logger.info(parsedJson.toString());
            
            String aiGeneratedTitle = parsedJson.has("title") ? parsedJson.get("title").asText() : chat.getTitle();
            if (aiGeneratedTitle != null && !aiGeneratedTitle.trim().isEmpty()) {
                chat.setTitle(aiGeneratedTitle);
                chatRepository.save(chat);
                logger.info("AI-generated chat title: {}", aiGeneratedTitle);
            }
        } catch (Exception e) {
            logger.error("Failed to parse JSON response: {}", e.getMessage());
            logger.error("Problem response text: {}", finalResponse);
        }
    }

    private Chat getChatByUuid(String chatUuid) {
        return chatRepository.findByUuid(chatUuid)
                .orElseThrow(() -> new RuntimeException("Chat with UUID " + chatUuid + " not found"));
    }

    private String determineAndBuildSystemPrompt(Chat baseChat, ResourceChat resourceChat, SpaceChat spaceChat, String userMessage, boolean isFirstMessage) {
        String systemPrompt;
        if (resourceChat != null) {
            logger.info("Handling as ResourceChat");
            systemPrompt = buildResourceChatSystemPrompt(resourceChat, userMessage);
        } else if (spaceChat != null) {
            logger.info("Handling as SpaceChat");
            systemPrompt = buildSpaceChatSystemPrompt(spaceChat, userMessage);
        } else {
            logger.info("Handling as SimpleChat");
            systemPrompt = buildSimpleChatSystemPrompt();
        }
        
        if (isFirstMessage) {
            systemPrompt += createFirstMessagePrompt();
        }
        return systemPrompt;
    }

    private HttpResponse makeAzureApiRequest(String apiUrl, Map<String, Object> requestBody) throws IOException {
        org.apache.http.client.HttpClient httpClient = org.apache.http.impl.client.HttpClients.createDefault();
        org.apache.http.client.methods.HttpPost request = new org.apache.http.client.methods.HttpPost(apiUrl);
        request.setHeader("Content-Type", "application/json");
        request.setHeader("api-key", azureConfig.getAzureApiKey());
        
        StringEntity entity = new StringEntity(objectMapper.writeValueAsString(requestBody), StandardCharsets.UTF_8);
        request.setEntity(entity);
        
        return httpClient.execute(request);
    }

    private String processStreamingResponse(HttpResponse response, SseEmitter emitter) throws IOException {
        StringBuilder fullResponse = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data: ")) {
                    String data = line.substring(6);
                    if ("[DONE]".equals(data)) break;
                    
                    try {
                        JsonNode chunk = objectMapper.readTree(data);
                        String content = chunk.path("choices")
                                        .path(0)
                                        .path("delta")
                                        .path("content")
                                        .asText("");
                        
                        if (!content.isEmpty()) {
                            fullResponse.append(content);
                            emitter.send(objectMapper.writeValueAsString(content));
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to parse chunk: {}", e.getMessage());
                    }
                }
            }
        }
        return fullResponse.toString();
    }

    private void sendFinalResponse(SseEmitter emitter, Chat chat, String userMessage, String finalResponseStr, Long messageId) throws IOException {
        Map<String, Object> finalPayload = new HashMap<>();
        finalPayload.put("user_message", userMessage);
        finalPayload.put("message_id", messageId);
        
        Map<String, Object> serviceResponse = new HashMap<>();
        serviceResponse.put("title", chat.getTitle() != null ? chat.getTitle() : "General Assistance");
        serviceResponse.put("user_message", userMessage);
        serviceResponse.put("service_response", finalResponseStr.trim());
        serviceResponse.put("message_id", messageId);
        serviceResponse.put("annotations", new HashMap<>());
        serviceResponse.put("sources", new ArrayList<>());
        
        finalPayload.put("service_response", serviceResponse);
        
        emitter.send(objectMapper.writeValueAsString(finalPayload));
    }

    private void handleError(SseEmitter emitter, Exception e) {
        logger.error("Error processing chat message: {}", e.getMessage(), e);
        try {
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("error", "Failed to process chat message: " + e.getMessage());
            emitter.send(objectMapper.writeValueAsString(errorNode));
        } catch (IOException ioException) {
            logger.error("Failed to send error event", ioException);
        }
        emitter.completeWithError(e);
    }

    @Transactional
    public SseEmitter addMessageToChat(String chatUuid, String userMessage, List<MultipartFile> uploadedImages) {
        logger.info("Adding message to chat: {}", chatUuid);
        if (uploadedImages != null) {
            logger.info("Uploaded images count: {}", uploadedImages.size());
        }
        
        SseEmitter emitter = new SseEmitter(300000L); // 5 minute timeout
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        executor.execute(() -> {
            try {
                // Get chat and determine type
                Chat baseChat = getChatByUuid(chatUuid);
                ResourceChat resourceChat = resourceChatRepository.findById(baseChat.getId()).orElse(null);
                SpaceChat spaceChat = spaceChatRepository.findById(baseChat.getId()).orElse(null);
                Object specificChat = resourceChat != null ? resourceChat : (spaceChat != null ? spaceChat : baseChat);
                
                // Build system prompt
                boolean isFirstMessage = false;
                String systemPrompt = determineAndBuildSystemPrompt(baseChat, resourceChat, spaceChat, userMessage, isFirstMessage);
                
                // Process images and prepare request
                List<Map<String, String>> uploadedMediaMeta = processUploadedImagesForChat(uploadedImages, baseChat);
                List<Message> previousMessages = messageRepository.findAllByChatIdOrderByCreatedAtAsc(baseChat.getId());
                Map<String, Object> requestBody = prepareChatRequestBody(systemPrompt, previousMessages, userMessage, uploadedImages, isFirstMessage);
                
                // Make API request
                String apiUrl = String.format("%s/openai/deployments/%s/chat/completions?api-version=2023-12-01-preview",
                    azureConfig.getAzureEndpoint(),
                    azureConfig.getChatDeployment());
                
                HttpResponse response = makeAzureApiRequest(apiUrl, requestBody);
                
                if (response.getStatusLine().getStatusCode() == 200) {
                    String finalResponseStr = processStreamingResponse(response, emitter);
                    
                    // Log generation
                    logger.info("Preparing to log to Langfuse - Chat UUID: {}, Is First Message: {}", chatUuid, isFirstMessage);
                    logger.info("Langfuse logging - User ID: {}", baseChat.getUser().getId());
                    logger.info("Langfuse logging - Model: {}", azureConfig.getChatDeployment());
                    
                    String fullPrompt = buildFullPrompt(systemPrompt, (List<Map<String, Object>>) requestBody.get("messages"));
                    logger.info("Langfuse logging - Prompt length: {}", fullPrompt.length());
                    logger.info("Langfuse logging - Response length: {}", finalResponseStr.length());
                    
                    langfuseClient.logGeneration(
                        chatUuid,
                        isFirstMessage,
                        baseChat.getUser().getId().toString(),
                        fullPrompt,
                        finalResponseStr,
                        azureConfig.getChatDeployment()
                    );
                    
                    // Handle first message if needed
                    if (isFirstMessage) {
                        handleFirstChatMessage(finalResponseStr, baseChat);
                    }
                    
                    // Save message and update timestamps
                    Long savedMessageId = saveChatMessageAndMedia(baseChat, userMessage, finalResponseStr, uploadedMediaMeta);
                    if (specificChat instanceof SpaceChat) {
                        spaceChat.getSpace().setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
                    }
                    
                    // Send final response
                    sendFinalResponse(emitter, baseChat, userMessage, finalResponseStr, savedMessageId);
                } else {
                    throw new RuntimeException("Azure OpenAI API returned status code: " + response.getStatusLine().getStatusCode());
                }
                
                emitter.complete();
                
            } catch (Exception e) {
                handleError(emitter, e);
            } finally {
                executor.shutdown();
            }
        });
        
        return emitter;
    }

    private String buildSimpleChatSystemPrompt() {
        String prompt;
            try {
                prompt = promptService.createSimpleSystemPrompt();
            } catch (IOException e) {
                System.err.println("Failed to load resource prompt: " + e.getMessage());

                prompt = "An error occured";
            }        
            return prompt;
    }

    private String buildResourceChatSystemPrompt(ResourceChat resourceChat, String userMessage) {
        Resource resource = resourceChat.getResource();
        String resourceTitle = resource != null ? resource.getTitle() : "Unknown Resource";
        String resourceContent = resource != null ? resource.getContent() : "No content available";
        
        if (resourceContent.length() <= MAX_RESOURCE_CHARS) {
            return createResourceSystemPrompt(resourceTitle, resourceContent);
        } else {
            List<Map<String, String>> topChunks = embeddingService.searchSimilarChunks(userMessage, resource.getId(), 5);
            logger.info("Number of top chunks retrieved: {}", topChunks.size());
            String resourcesSummary = resource.getSummary();
            String content = resource.getContent();
            String limitedContent;
            if (content.isBlank() || content==null) {
                limitedContent = "";
            } else{
                limitedContent = content.substring(0, Math.min(content.length(), 5000));
            }
            return createLargeResourceSystemPrompt(resourceTitle, resourcesSummary, topChunks, limitedContent);
        }
    }

    private String buildSpaceChatSystemPrompt(SpaceChat spaceChat, String userMessage) {
        final int MAX_RESOURCE_CONTENT_LENGTH = 10000;
        Space space = spaceChat.getSpace();
        Set<Long> resourceIds = new HashSet<>(spaceResourceRepository.findResourceIdsBySpaceId(space.getId()));
        List<Map<String, String>> topChunks = embeddingService.searchSimilarChunksAcrossResources(userMessage, resourceIds, 5);
        
        for (int i = 0; i < topChunks.size(); i++) {
            Map<String, String> chunk = topChunks.get(i);
            String chunkText = chunk.getOrDefault("chunk_text", "").replaceAll("\n", " ").trim();
            logger.info("Chunk {}: {}", i + 1, abbreviate(chunkText, 200));
        }
        
        logger.info("Number of top chunks retrieved: {}", topChunks.size());
        String resourcesSummary = formatResourceChunks(topChunks);
        Set<Long> usedResourceIds = topChunks.stream()
            .map(chunk -> Long.parseLong(chunk.get("resource_id")))
            .collect(Collectors.toSet());

       StringBuilder summariesText = new StringBuilder("Resource Summaries:\n\n");

        resourceRepository.findAllById(usedResourceIds).forEach(resource -> {
            String contentToUse;
            if (resource.getContent() != null && resource.getContent().length() <= MAX_RESOURCE_CONTENT_LENGTH) {
                contentToUse = resource.getContent().trim();
                logger.info("Using full content for resource ID {}", resource.getId());
            } else {
                contentToUse = (resource.getSummary() != null ? resource.getSummary().trim() : "(no summary)");
                logger.info("Using summary for resource ID {}", resource.getId());
            }

            summariesText.append("- Resource Title ").append(resource.getTitle()).append(": ")
                        .append(contentToUse).append("\n");
        });
        return createSpaceSystemPrompt(summariesText.toString(),resourcesSummary);
    }

// Keep your existing helper methods like createResourceSystemPrompt, 
// createLargeResourceSystemPrompt, etc. unchanged
    
    private String createResourceSystemPrompt(String resourceTitle, String resourceContent) {
            String prompt;
            try {
                prompt = promptService.createResourceSystemPrompt(resourceTitle, resourceContent);
            } catch (IOException e) {
                System.err.println("Failed to load resource prompt: " + e.getMessage());

                prompt = "An error occured";
            }        
            return prompt;
    }
    
    private String createLargeResourceSystemPrompt(
        String resourceTitle,
        String resourcesSummary,
        List<Map<String, String>> chunks,
        String content) {

        StringBuilder chunksSection = new StringBuilder();

        if (chunks != null && !chunks.isEmpty()) {
            chunksSection.append("### **Excerpts from the Resource**\n");
            for (int i = 0; i < chunks.size(); i++) {
                Map<String, String> chunk = chunks.get(i);
                String chunkText = chunk != null
                        ? Optional.ofNullable(chunk.get("chunk_text")).orElse("").trim()
                        : "";
                if (!chunkText.isEmpty()) {
                    chunksSection.append("- Excerpt ").append(i + 1).append(": ").append(chunkText).append("\n\n");
                }
            }
        } else {
            chunksSection.append("### **The start of the resource:**\n");
            chunksSection.append(Optional.ofNullable(content).orElse(""));
        }

        // Handle nulls safely
        String safeResourceTitle = Optional.ofNullable(resourceTitle).orElse("Unknown Resource");
        String safeResourcesSummary = Optional.ofNullable(resourcesSummary).orElse("");
        String safeChunksSection = chunksSection.toString();

        String prompt;
        try {
            prompt = promptService.createLargeResourceSystemPrompt(
                    safeResourceTitle,
                    safeResourcesSummary,
                    safeChunksSection,
                    content
            );
        } catch (IOException e) {
            System.err.println("Failed to load resource prompt: " + e.getMessage());
            prompt = "An error occurred";
        }

        return prompt;
    }
    
    private String createSpaceSystemPrompt(String resourceSummaries,String resourcesSummary) {
        String prompt;
            try {
                prompt = promptService.createSpaceSystemPrompt(resourcesSummary,resourceSummaries);
            } catch (IOException e) {
                System.err.println("Failed to load resource prompt: " + e.getMessage());

                prompt = "An error occured";
            }        
            return prompt;
    }
    
    private String createFirstMessagePrompt() {
        return "\n\n### **First Message Special Handling**\n" +
               "- Generate a 2–4 word **chat title**.\n" +
               "- Then answer the user's question in Markdown.\n\n" +
               "### **Strict JSON Output Format**\n" +
               "```json\n" +
               "{\n" +
               "\"title\": \"Generated title here\",\n" +
               "\"response\": \"Markdown response here\",\n" +
               "\"sources\": [\"Exact excerpt 1\", \"Exact excerpt 2\"]\n" +
               "}\n" +
               "```\n" +
               "- Only output valid JSON — no extra text or formatting.";
    }
    
    private String formatResourceChunks(List<Map<String, String>> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return "\nNo relevant resource excerpts found.";
        }
        
        StringBuilder summary = new StringBuilder("\nHere are some relevant resource excerpts:\n");
        for (Map<String, String> chunk : chunks) {
            summary.append("- ").append(chunk.get("text")).append("...\n");
        }
        
        return summary.toString();
    }
    
    private String determineMediaType(String filename) {
        String lowercaseFilename = filename.toLowerCase();
        if (lowercaseFilename.endsWith(".jpg") || lowercaseFilename.endsWith(".jpeg") 
                || lowercaseFilename.endsWith(".png") || lowercaseFilename.endsWith(".gif")) {
            return "image";
        } else if (lowercaseFilename.endsWith(".mp4") || lowercaseFilename.endsWith(".avi") 
                || lowercaseFilename.endsWith(".mov")) {
            return "video";
        } else if (lowercaseFilename.endsWith(".mp3") || lowercaseFilename.endsWith(".wav")) {
            return "audio";
        }
        return "file";
    }
    
    private String determineMimeType(String filename) {
        String lowercaseFilename = filename.toLowerCase();
        if (lowercaseFilename.endsWith(".jpg") || lowercaseFilename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowercaseFilename.endsWith(".png")) {
            return "image/png";
        } else if (lowercaseFilename.endsWith(".gif")) {
            return "image/gif";
        }
        return "application/octet-stream"; // fallback
    }
    
    @Transactional
    public void clearChat(String chatUuid) {
        logger.info("Clearing chat: {}", chatUuid);

        Chat chat = chatRepository.findByUuid(chatUuid)
                .orElseThrow(() -> new RuntimeException("Chat with UUID " + chatUuid + " not found"));

        List<Message> messages = messageRepository.findByChatOrderByCreatedAtAsc(chat);

        for (Message message : messages) {
            List<MessageMedia> medias = message.getMedia();
            for (MessageMedia media : medias) {
                try {
                    fileStorageService.deleteFile(media.getMediaUrl());
                } catch (Exception e) {
                    logger.warn("Failed to delete file: {}", media.getMediaUrl());
                }

                messageMediaRepository.delete(media);
            }

            messageRepository.delete(message);
        }

        chat.setTitle(null);
        chat.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        chatRepository.save(chat);

        logger.info("Cleared chat: {}", chatUuid);
    }


    @Transactional
    public void deleteChat(String chatUuid) {
        logger.info("Deleting chat: {}", chatUuid);

        Chat chat = chatRepository.findByUuid(chatUuid)
                .orElseThrow(() -> new RuntimeException("Chat with UUID " + chatUuid + " not found"));

        chat.setDeleted(true);
    }

    private String safeSubstring(String text, int maxLength) {
        if (text == null) return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }
    

    private String abbreviate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }

    @Transactional
    public ChatRequest updateChat(String chatUuid, ChatRequest chatDto) {
        Chat chat = chatRepository.findByUuid(chatUuid)
                .orElseThrow(() -> new RuntimeException("Chat with UUID " + chatUuid + " not found"));
        if (chatDto.getTitle() != null && !chatDto.getTitle().isBlank()) {
            chat.setTitle(chatDto.getTitle());
        }

        Chat newChat = chatRepository.save(chat);
        return new ChatRequest(newChat.getTitle());
        
    }

    private String buildFullPrompt(String systemPrompt, List<Map<String, Object>> messages) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append(systemPrompt).append("\n");

        for (Map<String, Object> message : messages) {
            promptBuilder.append(message.get("role")).append(": ");
            promptBuilder.append(message.get("content")).append("\n");
        }
        return promptBuilder.toString();
    }

    @Transactional
    public boolean updateMessageRating(Long messageId, String rating) {
        try {
            int updatedRows = messageRepository.updateMessageRating(messageId, rating);
            return updatedRows > 0;
        } catch (Exception e) {
            logger.error("Error updating message rating: {}", e.getMessage(), e);
            return false;
        }
    }

}
