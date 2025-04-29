package com.uninote.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.uninote.backend.dto.ChatHistoryDto;
import com.uninote.backend.dto.FileResourceDTO;
import com.uninote.backend.dto.MessageDTO;
import com.uninote.backend.dto.ResourceDTO;
import com.uninote.backend.dto.YouTubeResourceDTO;
import com.uninote.backend.entity.Chat;
import com.uninote.backend.entity.FileResource;
import com.uninote.backend.entity.Message;
import com.uninote.backend.entity.MessageMedia;
import com.uninote.backend.entity.Resource;
import com.uninote.backend.entity.ResourceChat;
import com.uninote.backend.entity.SpaceChat;
import com.uninote.backend.entity.YouTubeResource;
import com.uninote.backend.repository.MessageRepository;
import com.uninote.backend.repository.ResourceChatRepository;
import com.uninote.backend.repository.SpaceChatRepository;
import com.uninote.backend.repository.SpaceResourceRepository;
import com.uninote.backend.service.embedding.EmbeddingService;

import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.data.message.Content;


import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.FileStore;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uninote.backend.config.AzureOpenAiConfig;
import com.uninote.backend.entity.Space;
import com.uninote.backend.repository.ChatRepository;
import com.uninote.backend.repository.MessageMediaRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Timestamp;
import java.time.LocalDateTime;

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

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    private static final int MAX_RESOURCE_CHARS = 15000;


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
            .orElseThrow(() -> {
                log.error("ResourceChat not found for UUID: {}", chatUuid);
                return new RuntimeException("ResourceChat not found");
            });
        log.info("Found resource of type: {}");
        long queryTime = System.currentTimeMillis();
        log.info("Database query took {} ms", queryTime - startTime);
        Resource resource = resourceChat.getResource();
        log.info("Found resource of type: {}", resource.getClass().getSimpleName());

        ResourceDTO resourceDTO;

        if (resource instanceof FileResource) {
            FileResource fr = (FileResource) resource;
            log.info("Mapping FileResource with id: {}", fr.getId());

            FileResourceDTO dto = new FileResourceDTO();
            dto.setId(fr.getId());
            dto.setTitle(fr.getTitle());
            dto.setCreatedAt(fr.getCreatedAt());
            dto.setSummary(fr.getSummary());
            dto.setContent(fr.getContent());
            dto.setSupabaseFileUrl(fr.getFileUrl());
            dto.setFlashcards(fr.getFlashcards());
            dto.setChapters(fr.getChapters());
            dto.setQuizzes(fr.getQuiz());

            resourceDTO = dto;

        } else if (resource instanceof YouTubeResource) {
            YouTubeResource yt = (YouTubeResource) resource;
            log.info("Mapping YouTubeResource with id: {}", yt.getId());

            YouTubeResourceDTO dto = new YouTubeResourceDTO();
            dto.setId(yt.getId());
            dto.setTitle(yt.getTitle());
            dto.setCreatedAt(yt.getCreatedAt());
            dto.setSummary(yt.getSummary());
            dto.setChapters(yt.getChapters());
            dto.setFlashcards(yt.getFlashcards());
            dto.setQuizzes(yt.getQuiz());
            dto.setContent(yt.getContent());
            dto.setYoutubeUrl(yt.getYoutubeUrl());

            resourceDTO = dto;

        } else {
            log.error("Unsupported resource type: {}", resource.getClass().getSimpleName());
            throw new IllegalStateException("Unsupported resource type: " + resource.getClass().getSimpleName());
        }

        Long chatId = resourceChat.getChat().getId();
        log.info("Fetching messages for chatId: {}", chatId);

        List<MessageDTO> messageDtos = messageRepository.findAllByChatIdOrderByCreatedAtAsc(chatId)
            .stream()
            .map(m -> new MessageDTO(m))
            .collect(Collectors.toList());

        log.info("Retrieved {} messages for chatId: {}", messageDtos.size(), chatId);

        return new ChatHistoryDto(
            chatId,
            resource.getTitle(),
            resourceDTO,
            messageDtos
        );
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
                // Find the chat by UUID
                Chat baseChat = chatRepository.findByUuid(chatUuid)
                        .orElseThrow(() -> new RuntimeException("Chat with UUID " + chatUuid + " not found"));
                
                ResourceChat resourceChat = resourceChatRepository.findById(baseChat.getId()).orElse(null);

                SpaceChat spaceChat = null;
                if (resourceChat == null) {
                    // No ResourceChat found, try SpaceChat
                    spaceChat = spaceChatRepository.findById(baseChat.getId()).orElse(null);
                }
                
                if (resourceChat == null && spaceChat == null) {
                    throw new RuntimeException("No associated ResourceChat or SpaceChat found for chat UUID: " + chatUuid);
                }
                
                Object specificChat;
                if (resourceChat != null) {
                    specificChat = resourceChat;
                    logger.info("Handling as ResourceChat");
                } else {
                    specificChat = spaceChat;
                    logger.info("Handling as SpaceChat");
                }
                
                // Build chat history
                List<Map<String, Object>> chatHistory = new ArrayList<>();
                
                String systemPrompt;
                if (specificChat.getClass().getName().endsWith("ResourceChat")) {
                    // Use reflection to access the resource
                    Resource resource = null;
                    try {
                        Method getResourceMethod = specificChat.getClass().getMethod("getResource");
                        resource = (Resource) getResourceMethod.invoke(specificChat);
                    } catch (Exception e) {
                        logger.error("Error accessing resource method", e);
                    }
                    String resourceTitle = resource != null ? resource.getTitle() : "Unknown Resource";
                    String resourceContent = resource != null ? resource.getContent() : "No content available";
                    
                    if (resourceContent.length() <= MAX_RESOURCE_CHARS) {
                        systemPrompt = createResourceSystemPrompt(resourceTitle, resourceContent);
                    } else {
                        List<Map<String, String>> topChunks = embeddingService.searchSimilarChunks(userMessage, resource.getId(), 5);
                        logger.info("Number of top chunks retrieved: {}", topChunks.size());
                        String resourcesSummary = resource.getSummary();
                        systemPrompt = createLargeResourceSystemPrompt(resourceTitle, resourcesSummary, topChunks);
                    }
                } else {
                    spaceChat = (SpaceChat) specificChat;
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
                    systemPrompt = createSpaceSystemPrompt(resourcesSummary);
                }
                
                // Check if this is the first message in the chat
                boolean isFirstMessage = messageRepository.countByChat(baseChat) == 0;
                
                if (isFirstMessage) {
                    systemPrompt += createFirstMessagePrompt();
                }
                
                // Add system message to chat history
                Map<String, Object> systemMessage = new HashMap<>();
                systemMessage.put("role", "system");
                
                List<Map<String, Object>> systemContent = new ArrayList<>();
                Map<String, Object> textContent = new HashMap<>();
                textContent.put("type", "text");
                textContent.put("text", systemPrompt);
                systemContent.add(textContent);
                
                systemMessage.put("content", systemContent);
                chatHistory.add(systemMessage);
                
                // Add previous messages to chat history
                List<Message> previousMessages = messageRepository.findAllByChatIdOrderByCreatedAtAsc(baseChat.getId());
                for (Message message : previousMessages) {
                    message.getMedia().size();
                    if (message.getUserMessage() != null) {
                        Map<String, Object> userMsg = new HashMap<>();
                        userMsg.put("role", "user");
                        
                        List<Map<String, Object>> msgContent = new ArrayList<>();
                        Map<String, Object> msgText = new HashMap<>();
                        msgText.put("type", "text");
                        msgText.put("text", message.getUserMessage());
                        msgContent.add(msgText);
                        
                        // Add message media
                        List<MessageMedia> medias = message.getMedia();
                        for (MessageMedia media : medias) {
                            Map<String, Object> mediaContent = new HashMap<>();
                            String key = media.getMediaType() + "_url";
                            mediaContent.put("type", key);
                            
                            Map<String, String> urlMap = new HashMap<>();
                            urlMap.put("url", media.getMediaUrl());
                            mediaContent.put(key, urlMap);
                            
                            msgContent.add(mediaContent);
                        }
                        
                        userMsg.put("content", msgContent);
                        chatHistory.add(userMsg);
                    }
                    
                    if (message.getServiceResponse() != null) {
                        Map<String, Object> aiMsg = new HashMap<>();
                        aiMsg.put("role", "assistant");
                        aiMsg.put("content", message.getServiceResponse());
                        chatHistory.add(aiMsg);
                    }
                }
                
                // Add current user message to chat history
                Map<String, Object> currentUserMsg = new HashMap<>();
                currentUserMsg.put("role", "user");
                
                List<Map<String, Object>> currentMsgContent = new ArrayList<>();
                Map<String, Object> currentMsgText = new HashMap<>();
                List<Map<String, String>> uploadedMediaMeta = new ArrayList<>();

                currentMsgText.put("type", "text");
                currentMsgText.put("text", userMessage);
                currentMsgContent.add(currentMsgText);
                
                if (uploadedImages != null && !uploadedImages.isEmpty()) {
                    for (MultipartFile image : uploadedImages) {
                        try {
                            // Upload to your storage
                            String fileName = "chat_" + baseChat.getId() + "/" + UUID.randomUUID() + "_" + image.getOriginalFilename();
                            String uploadedImageUrl = fileStorageService.uploadFile(image, fileName, baseChat.getId(), "chat_media");
                            logger.info(uploadedImageUrl);
                            // Determine media type
                            String mimeType = determineMimeType(image.getOriginalFilename());
                            String mediaType = mimeType.split("/")[0]; // e.g., "image"
                
                            // Build image content for GPT
                            Map<String, Object> imageContent = new HashMap<>();
                            imageContent.put("type", mediaType + "_url");
                
                            Map<String, String> imageUrlMap = new HashMap<>();
                            imageUrlMap.put("url", uploadedImageUrl);
                            imageContent.put(mediaType + "_url", imageUrlMap);
                
                            currentMsgContent.add(imageContent); // ⬅️ VERY IMPORTANT: add to chat message content
                
                            // Save metadata to save to database later
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
                
                

                
                
                
                currentUserMsg.put("content", currentMsgContent);
                chatHistory.add(currentUserMsg);
                
                // Make API calls to get AI response
                StringBuilder responseBuffer = new StringBuilder();
                
                try {
                    String azureUrl = azureConfig.getAzureEndpoint()
                        + "/openai/deployments/" + azureConfig.getChatDeployment()
                        + "/chat/completions?api-version=2024-02-15-preview";
                
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.set("api-key", azureConfig.getAzureApiKey());
                    headers.setAccept(List.of(MediaType.APPLICATION_JSON));
                
                    // Build payload
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("messages", buildAzureMessages(chatHistory));
                    payload.put("temperature", 0.7);
                    payload.put("stream", false); // if you want chunked, set true
                    payload.put("max_tokens", 1500);
                
                    HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
                
                    logger.info("Sending chatHistory to Azure endpoint: {}", azureUrl);
                
                    ResponseEntity<JsonNode> response = restTemplate.exchange(
                        azureUrl,
                        HttpMethod.POST,
                        request,
                        JsonNode.class
                    );
                
                    JsonNode body = response.getBody();
                    logger.info("Azure response: {}", body);
                
                    if (body != null && body.has("choices")) {
                        String aiResponse = body.get("choices").get(0).get("message").get("content").asText();
                
                        // Stream the chunks to the client
                        String[] chunks = aiResponse.split("(?<=\\G.{50})");
                        for (String chunk : chunks) {
                            if (!chunk.isEmpty()) {
                                responseBuffer.append(chunk);
                                emitter.send(objectMapper.writeValueAsString(chunk));
                                Thread.sleep(50); // Simulate typing
                            }
                        }
                    }
                
                } catch (Exception e) {
                    logger.error("Error calling Azure Chat Completion API: {}", e.getMessage());
                    ObjectNode errorNode = objectMapper.createObjectNode();
                    errorNode.put("status", "error");
                    errorNode.put("message", "Failed to call Azure Chat Completion: " + e.getMessage());
                    try {
                        emitter.send(objectMapper.writeValueAsString(errorNode));
                    } catch (IOException ioException) {
                        logger.error("Failed to send error event", ioException);
                    }
                    emitter.complete();
                    return;
                }
                
                
                // Process and save the message after streaming
                String finalResponse = responseBuffer.toString().trim();
                String textResponse = "";
                String aiGeneratedTitle = baseChat.getTitle();
                List<String> sources = new ArrayList<>();
                Map<String, Object> annotations = new HashMap<>();
                
                if (isFirstMessage) {
                    try {
                        // Extract the JSON from the response if it's wrapped in code blocks
                        String jsonString = finalResponse.replace("```json", "").replace("```", "").trim();
                        JsonNode responseJson = objectMapper.readTree(jsonString);
                        
                        aiGeneratedTitle = responseJson.has("title") ? responseJson.get("title").asText() : baseChat.getTitle();
                        textResponse = responseJson.has("response") ? responseJson.get("response").asText() : "";
                        
                        if (responseJson.has("sources") && responseJson.get("sources").isArray()) {
                            JsonNode sourcesNode = responseJson.get("sources");
                            for (int i = 0; i < sourcesNode.size(); i++) {
                                sources.add(sourcesNode.get(i).asText());
                            }
                        }
                        
                        if (responseJson.has("annotations")) {
                            JsonNode annotationsNode = responseJson.get("annotations");
                            // Convert the annotations node to a Map
                            if (annotationsNode.isObject()) {
                                annotations = objectMapper.convertValue(annotationsNode, Map.class);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Failed to parse JSON response: {}", e.getMessage());
                        textResponse = finalResponse;
                    }
                } else {
                    textResponse = finalResponse;
                }
                
                if (textResponse.trim().isEmpty()) {
                    textResponse = "I'm here to assist you!";
                }
                
                if (aiGeneratedTitle == null || aiGeneratedTitle.trim().isEmpty()) {
                    aiGeneratedTitle = baseChat.getTitle() != null ? baseChat.getTitle() : "General Assistance";
                }
                
                // Update chat title if this is the first message
                if (isFirstMessage && aiGeneratedTitle != null) {
                    baseChat.setTitle(aiGeneratedTitle);
                    chatRepository.save(baseChat);
                    logger.info("AI-generated chat title: {}", aiGeneratedTitle);
                }
                
                // Save the message and media
                Message message = new Message();
                message.setChat(baseChat);
                message.setUserMessage(userMessage);
                message.setServiceResponse(textResponse);
                message.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
                Message savedMessage = messageRepository.save(message);
              

                // Save media attachments
                for (Map<String, String> media : uploadedMediaMeta) {
                    MessageMedia messageMedia = new MessageMedia();
                    messageMedia.setMessage(savedMessage);
                    messageMedia.setMediaUrl(media.get("url"));  // <-- Correct URL
                    messageMedia.setMediaType(media.get("type")); // image
                    messageMedia.setOriginalFilename(media.get("filename"));
                    messageMediaRepository.save(messageMedia);
                }
                
                
                // Update timestamps
                baseChat.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
                if (specificChat instanceof SpaceChat) {
                    spaceChat.getSpace().setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
                    // Save space
                }
                chatRepository.save(baseChat);
                
                // Send final complete response
                Map<String, Object> finalPayload = new HashMap<>();
                finalPayload.put("user_message", userMessage);
                
                Map<String, Object> serviceResponse = new HashMap<>();
                serviceResponse.put("title", isFirstMessage ? aiGeneratedTitle : (baseChat.getTitle() != null ? baseChat.getTitle() : "General Assistance"));
                serviceResponse.put("user_message", userMessage);
                serviceResponse.put("service_response", textResponse.trim());
                serviceResponse.put("annotations", annotations);
                serviceResponse.put("sources", sources);
                
                finalPayload.put("service_response", serviceResponse);
                
                emitter.send(objectMapper.writeValueAsString(finalPayload));
                emitter.complete();
                
            } catch (Exception e) {
                logger.error("Error processing chat message: {}", e.getMessage(), e);
                try {
                    ObjectNode errorNode = objectMapper.createObjectNode();
                    errorNode.put("error", "Failed to process chat message: " + e.getMessage());
                    emitter.send(objectMapper.writeValueAsString(errorNode));
                } catch (IOException ioException) {
                    logger.error("Failed to send error event", ioException);
                }
                emitter.completeWithError(e);
            } finally {
                executor.shutdown();
            }
        });
        
        return emitter;
    }
    
    // Helper methods
    
    private String createResourceSystemPrompt(String resourceTitle, String resourceContent) {
        return "You are **Tutie**, the best AI tutor. Your goal is to provide accurate, well-structured, and insightful responses in **Markdown format**.\n\n" +
               "### **Resource Information**\n" +
               "- This chat is based on a resource titled **'" + resourceTitle + "'**.\n" +
               "- The content of the resource is as follows:\n\n" +
               "```text\n" + resourceContent + "...\n```\n\n" +
               "### **Response Guidelines**\n" +
               "1. **Make sure your response provides value** — do not just repeat the resource.\n" +
               "2. **Use Markdown** formatting, include clear structure and context.\n" +
               "3. **Extract direct quotes from the resource** to support the response.\n" +
               "4. **Use LaTeX** for any math equations:\n" +
               "   - Inline math should be wrapped in `$...$`\n" +
               "   - Block-level equations should be wrapped in `$$...$$`\n" +
               "5. **Ensure completeness**, provide insights beyond what's explicitly stated.";
    }
    
    private String createLargeResourceSystemPrompt(String resourceTitle, String resourcesSummary, List<Map<String, String>> chunks) {
        StringBuilder chunksSection = new StringBuilder();
        
        if (chunks != null && !chunks.isEmpty()) {
            chunksSection.append("### **Excerpts from the Resource**\n");
            for (int i = 0; i < chunks.size(); i++) {
                Map<String, String> chunk = chunks.get(i);
                String chunkText = chunk.getOrDefault("chunk_text", "").trim();
                if (!chunkText.isEmpty()) {
                    chunksSection.append("- Excerpt ").append(i + 1).append(": ").append(chunkText).append("\n\n");
                }
            }
        }
    
        return "You are **Tutie**, the best AI tutor. Your goal is to provide accurate, well-structured, and insightful responses in **Markdown format**.\n\n" +
               "### **Resource Information (Summary)**\n" +
               "- Title: **'" + resourceTitle + "'**\n" +
               "- Summary:\n" + resourcesSummary + "\n\n" +
               chunksSection.toString() +
               "### **Response Guidelines**\n" +
               "1. **Make sure your response provides value** based on the provided summary and excerpts.\n" +
               "2. **Use Markdown** formatting with a clear structure and context.\n" +
               "3. **Use direct quotes from excerpts when applicable.**\n" +
               "4. **Use LaTeX** for any math equations:\n" +
               "   - Inline math should be wrapped in `$...$`\n" +
               "   - Block-level math should be wrapped in `$$...$$`\n" +
               "5. **Ensure completeness**, but **do not hallucinate beyond the provided excerpts**.\n" +
               "6. **When unsure, state that the information was not available.**";
    }
    
    private String createSpaceSystemPrompt(String resourcesSummary) {
        return "You are Tutie, an AI tutor helping students in a study space, which contains many resources.\n\n" +
                " Use the provided chunks only.\n\n" +
               resourcesSummary + "\n\n" +
               "Guidelines:\n" +
               "- Be concise and factual\n" +
               "- Cite the resource chunks in a 'sources' field\n" +
               "- Do not make up or hallucinate information\n";
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
    
    /*private List<dev.langchain4j.data.message.ChatMessage> convertToLangChainMessages(List<Map<String, Object>> messages) {
        List<dev.langchain4j.data.message.ChatMessage> langChainMessages = new ArrayList<>();

        for (Map<String, Object> message : messages) {
            String role = (String) message.get("role");
            Object content = message.get("content");

            if (content instanceof String) {
                // Simple text content
                switch (role) {
                    case "system":
                        langChainMessages.add(new dev.langchain4j.data.message.SystemMessage((String) content));
                        break;
                    case "user":
                        langChainMessages.add(new dev.langchain4j.data.message.UserMessage((String) content));
                        break;
                    case "assistant":
                        langChainMessages.add(new dev.langchain4j.data.message.AiMessage((String) content));
                        break;
                }
            } else if (content instanceof List) {
                // Mixed content (text and images)
                List<?> contentList = (List<?>) content;
                List<Content> contents = new ArrayList<>();
                
                for (Object item : contentList) {
                    if (item instanceof Map) {
                        Map<?, ?> contentMap = (Map<?, ?>) item;
                        String type = (String) contentMap.get("type");

                        if ("text".equals(type) && contentMap.containsKey("text")) {
                            contents.add(new TextContent((String) contentMap.get("text")));
                        } else if ("image_url".equals(type) && contentMap.containsKey("image_url")) {
                            Map<?, ?> imageMap = (Map<?, ?>) contentMap.get("image_url");
                            String url = (String) imageMap.get("url");
                            contents.add(new ImageContent(url));
                        }
                    }
                }
                
                // Create appropriate message with all contents
                switch (role) {
                    case "system":
                        langChainMessages.add(dev.langchain4j.data.message.SystemMessage.from(contents));
                        break;
                    case "user":
                        langChainMessages.add(dev.langchain4j.data.message.UserMessage.from(contents));
                        break;
                    case "assistant":
                        langChainMessages.add(dev.langchain4j.data.message.AiMessage.from(contents));
                        break;
                }
            }
        }

        return langChainMessages;
    }*/

    private List<Map<String, Object>> buildAzureMessages(List<Map<String, Object>> chatHistory) {
        List<Map<String, Object>> azureMessages = new ArrayList<>();
    
        for (Map<String, Object> originalMessage : chatHistory) {
            Map<String, Object> azureMessage = new HashMap<>();
            azureMessage.put("role", originalMessage.get("role"));
    
            Object content = originalMessage.get("content");
    
            if (content instanceof String) {
                // Simple text-only message
                azureMessage.put("content", List.of(Map.of(
                    "type", "text",
                    "text", content
                )));
            } else if (content instanceof List) {
                List<?> contentList = (List<?>) content;
                List<Map<String, Object>> formattedContents = new ArrayList<>();
    
                for (Object item : contentList) {
                    if (item instanceof Map) {
                        Map<?, ?> itemMap = (Map<?, ?>) item;
                        String type = (String) itemMap.get("type");
    
                        if ("text".equals(type) && itemMap.containsKey("text")) {
                            formattedContents.add(Map.of(
                                "type", "text",
                                "text", itemMap.get("text")
                            ));
                        } else if (type.endsWith("_url") && itemMap.containsKey(type)) {
                            Map<?, ?> urlMap = (Map<?, ?>) itemMap.get(type);
                            formattedContents.add(Map.of(
                                "type", type,
                                type, Map.of(
                                    "url", urlMap.get("url")
                                )
                            ));
                        }
                    }
                }
    
                if (!formattedContents.isEmpty()) {
                    azureMessage.put("content", formattedContents);
                }
            }
    
            if (azureMessage.containsKey("content")) {
                azureMessages.add(azureMessage);
            }
        }
    
        return azureMessages;
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

        List<Message> messages = messageRepository.findByChatOrderByCreatedAtAsc(chat);
        for (Message message : messages) {
            List<MessageMedia> medias = message.getMedia();
            for (MessageMedia media : medias) {
                try {
                    //fileStorageService.deleteFile(media.getMediaUrl());
                } catch (Exception e) {
                    logger.warn("Failed to delete file: {}", media.getMediaUrl());
                }

                messageMediaRepository.delete(media);
            }
            messageRepository.delete(message);
        }

        chatRepository.delete(chat);

        logger.info("Deleted chat: {}", chatUuid);
    }

    private String safeSubstring(String text, int maxLength) {
        if (text == null) return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }
    

    private String abbreviate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }
}
