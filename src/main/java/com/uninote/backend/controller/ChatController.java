package com.uninote.backend.controller;

import com.uninote.backend.config.security.FirebaseAuthentication;
import com.uninote.backend.dto.ChatHistoryDto;
import com.uninote.backend.dto.ResourceChatSummaryDTO;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.UserRepository;
import com.uninote.backend.service.ChatService;
import com.uninote.backend.service.ResourceChatService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    @Autowired
    private ResourceChatService resourceChatService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

   /* @GetMapping("/{chatUuid}/history")
    public ResponseEntity<Map<String, Object>> getChatHistory(
            @PathVariable String chatUuid,
            @RequestParam String jwt) {
        
        Map<String, Object> chatHistory = chatService.getChatHistory(chatUuid, jwt);

        if (chatHistory != null && !chatHistory.isEmpty()) {
            return ResponseEntity.ok(chatHistory); 
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    */
    @GetMapping("/{chatUuid}/history")
    public ResponseEntity<ChatHistoryDto> getChatHistory(@PathVariable String chatUuid) {
        ChatHistoryDto history = chatService.getChatHistory(chatUuid);
        return ResponseEntity.ok(history);
    }


    @GetMapping
    public ResponseEntity<?> getUserChats() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseEntity<>("Authorization token missing or invalid.", HttpStatus.UNAUTHORIZED);
        }

        FirebaseAuthentication firebaseAuth = (FirebaseAuthentication) authentication;
        String userUid = firebaseAuth.getUid();

        User user = userRepository.findByFirebaseUid(userUid)
            .orElseThrow(() -> new RuntimeException("User not found"));

        List<ResourceChatSummaryDTO> chats = resourceChatService.getAllByUser(user);

        return ResponseEntity.ok(chats);
    }

    @PostMapping(value = "/{chatUuid}/message", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SseEmitter addMessageToChat(
            @PathVariable String chatUuid,
            @RequestPart("userMessage") String userMessage,
            @RequestPart(value = "image", required = false) List<MultipartFile> uploadedImages) {
        return chatService.addMessageToChat(chatUuid, userMessage, uploadedImages);
    }


    @DeleteMapping("/{chatUuid}/clear")
    public ResponseEntity<String> clearChat(@PathVariable String chatUuid) {
        chatService.clearChat(chatUuid);
        return ResponseEntity.ok("Chat cleared successfully");
    }
}

