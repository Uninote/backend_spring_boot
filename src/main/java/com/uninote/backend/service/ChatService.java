package com.uninote.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.uninote.backend.dto.ChatHistoryDto;
import com.uninote.backend.dto.FileResourceDTO;
import com.uninote.backend.dto.MessageDTO;
import com.uninote.backend.dto.ResourceDTO;
import com.uninote.backend.dto.YouTubeResourceDTO;
import com.uninote.backend.entity.Chat;
import com.uninote.backend.entity.FileResource;
import com.uninote.backend.entity.Message;
import com.uninote.backend.entity.Resource;
import com.uninote.backend.entity.ResourceChat;
import com.uninote.backend.entity.YouTubeResource;
import com.uninote.backend.repository.MessageRepository;
import com.uninote.backend.repository.ResourceChatRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final RestTemplate restTemplate = new RestTemplate();
    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ResourceChatRepository resourceChatRepository;

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
            dto.setSupabaseFileUrl(fr.getSupabaseFileUrl());
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

}
