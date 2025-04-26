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

import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uninote.backend.config.AzureOpenAiConfig;
import com.uninote.backend.entity.Space;
import com.uninote.backend.repository.ChatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Service
public class ChatService {

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
        ResourceChat resourceChat = resourceChatRepository.findByChat_Uuid(chatUuid)
            .orElseThrow(() -> new RuntimeException("ResourceChat not found"));

        Resource resource = resourceChat.getResource();
        ResourceDTO resourceDTO;

        if (resource instanceof FileResource) {
            FileResource fr = (FileResource) resource;
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
            YouTubeResourceDTO dto = new YouTubeResourceDTO();
            dto.setId(yt.getId());
            dto.setTitle(yt.getTitle());
            dto.setCreatedAt(yt.getCreatedAt());
            dto.setSummary(yt.getSummary());
            dto.setContent(yt.getContent());
            dto.setYoutubeUrl(yt.getYoutubeUrl());
            resourceDTO = dto;

        } else {
            throw new IllegalStateException("Unsupported resource type: " + resource.getClass().getSimpleName());
        }

        List<MessageDTO> messageDtos = messageRepository.findAllByChatIdOrderByCreatedAtAsc(resourceChat.getChat().getId())
            .stream()
            .map(m -> new MessageDTO(m))
            .collect(Collectors.toList());

        return new ChatHistoryDto(
            resourceChat.getChat().getId(),
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
                    throw new RuntimeException("Unsupported chat type: " + baseChat.getClass().getName());
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
                        //List<Map<String, String>> topChunks = ragService.searchSimilarChunks(userMessage, resource.getId(), 5);
                        String resourcesSummary = "";//formatResourceChunks(topChunks);
                        systemPrompt = createLargeResourceSystemPrompt(resourceTitle, resourcesSummary);
                    }
                } else {
                    //SpaceChat spaceChat = (SpaceChat) specificChat;
                    //Space space = spaceChat.getSpace();
                    //List<Map<String, String>> topChunks = ragService.searchSimilarChunks(userMessage, space.getId(), 5);
                    String resourcesSummary = "";//formatResourceChunks(topChunks);
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
                currentMsgText.put("type", "text");
                currentMsgText.put("text", userMessage);
                currentMsgContent.add(currentMsgText);
                
                // Handle uploaded images
                List<Map<String, String>> uploadedMediaMeta = new ArrayList<>();
                
                if (uploadedImages != null && !uploadedImages.isEmpty()) {
                    for (MultipartFile image : uploadedImages) {
                        try {
                            String fileName = "chat_" + baseChat.getId() + "/" + UUID.randomUUID() + "_" + image.getOriginalFilename();
                            String imageUrl = "";//fileStorageService.uploadFile(image, fileName, baseChat.getId(), "chat_media");
                            String mediaType = determineMediaType(image.getOriginalFilename());
                            
                            Map<String, Object> mediaContent = new HashMap<>();
                            String key = mediaType + "_url";
                            mediaContent.put("type", key);
                            
                            Map<String, String> urlMap = new HashMap<>();
                            urlMap.put("url", imageUrl);
                            mediaContent.put(key, urlMap);
                            
                            currentMsgContent.add(mediaContent);
                            
                            Map<String, String> mediaMeta = new HashMap<>();
                            mediaMeta.put("url", imageUrl);
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
                    // Get chat model
                    ChatLanguageModel chatModel = AzureOpenAiChatModel.builder()
                            .endpoint(azureConfig.getAzureEndpoint())
                            .apiKey(azureConfig.getAzureApiKey())
                            .deploymentName(azureConfig.getChatDeployment())
                            .temperature(0.7)
                            .build();
                    
                    // Stream responses
                    // Note: This is simplified as the actual streaming would depend on your AI provider's API
                    String aiResponse = chatModel.generate(convertToLangChainMessages(chatHistory)).content().text();
                    
                    // Split into chunks to simulate streaming
                    String[] chunks = aiResponse.split("(?<=\\G.{50})");
                    for (String chunk : chunks) {
                        if (!chunk.isEmpty()) {
                            responseBuffer.append(chunk);
                            emitter.send(objectMapper.writeValueAsString(chunk));
                            Thread.sleep(50); // Simulate streaming delay
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error in AI chat: {}", e.getMessage());
                    ObjectNode errorNode = objectMapper.createObjectNode();
                    errorNode.put("status", "error");
                    errorNode.put("message", "Failed to process chat message: " + e.getMessage());
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
                    messageMedia.setMediaUrl(media.get("url"));
                    messageMedia.setMediaType(media.get("type"));
                    messageMedia.setOriginalFilename(media.get("filename"));
                    // Save message media (assuming you have a repository for this)
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
    
    private String createLargeResourceSystemPrompt(String resourceTitle, String resourcesSummary) {
        return "You are **Tutie**, the best AI tutor. Your goal is to provide accurate, well-structured, and insightful responses in **Markdown format**.\n\n" +
               "### **Resource Information (via Embeddings)**\n" +
               "- This chat is based on a resource titled **'" + resourceTitle + "'**.\n" +
               resourcesSummary + "\n\n" +
               "### **Response Guidelines**\n" +
               "1. **Make sure your response provides value** based on the excerpts.\n" +
               "2. **Use Markdown** formatting, include clear structure and context.\n" +
               "3. **Use direct quotes from chunks if applicable.**\n" +
               "4. **Use LaTeX** for any math equations:\n" +
               "   - Inline math should be wrapped in `$...$`\n" +
               "   - Block-level equations should be wrapped in `$$...$$`\n" +
               "You have a MathJax render environment.\n" +
               "- Any LaTeX text between single dollar sign ($) will be rendered as a TeX formula;\n" +
               "- Use $(tex_formula)$ in-line delimiters to display equations instead of backslash;\n" +
               "- The render environment only uses $ (single dollarsign) as a container delimiter.\n" +
               "Example: $x^2 + 3x$ is output for `x² + 3x` to appear as TeX.\n" +
               "5. **Ensure completeness**, but **do not hallucinate beyond the provided excerpts**.";
    }
    
    private String createSpaceSystemPrompt(String resourcesSummary) {
        return "You are Tutie, an AI tutor helping students in a study space. Use the provided chunks only.\n\n" +
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
    
    private List<dev.langchain4j.data.message.ChatMessage> convertToLangChainMessages(List<Map<String, Object>> messages) {
        List<dev.langchain4j.data.message.ChatMessage> langChainMessages = new ArrayList<>();
        
        for (Map<String, Object> message : messages) {
            String role = (String) message.get("role");
            Object content = message.get("content");
            String textContent = "";
            
            // Extract text content from various content formats
            if (content instanceof String) {
                textContent = (String) content;
            } else if (content instanceof List) {
                for (Object item : (List<?>) content) {
                    if (item instanceof Map) {
                        Map<?, ?> contentMap = (Map<?, ?>) item;
                        if (contentMap.containsKey("type") && "text".equals(contentMap.get("type")) 
                                && contentMap.containsKey("text")) {
                            textContent += contentMap.get("text");
                        }
                    }
                }
            }
            
            // Create appropriate message type
            switch (role) {
                case "system":
                    langChainMessages.add(new dev.langchain4j.data.message.SystemMessage(textContent));
                    break;
                case "user":
                    langChainMessages.add(new dev.langchain4j.data.message.UserMessage(textContent));
                    break;
                case "assistant":
                    langChainMessages.add(new dev.langchain4j.data.message.AiMessage(textContent));
                    break;
            }
        }
        
        return langChainMessages;
    }
}
