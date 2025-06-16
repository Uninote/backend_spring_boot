package com.uninote.backend.controller;

import com.azure.ai.openai.models.ChatResponseMessage;
import com.uninote.backend.config.security.FirebaseAuthentication;
import com.uninote.backend.dto.ChatHistoryDto;
import com.uninote.backend.dto.ChatRequest;
import com.uninote.backend.dto.ResourceChatSummaryDTO;
import com.uninote.backend.entity.Chat;
import com.uninote.backend.entity.Resource;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.ChatRepository;
import com.uninote.backend.repository.ResourceRepository;
import com.uninote.backend.repository.UserRepository;
import com.uninote.backend.service.ChatService;
import com.uninote.backend.service.LangChainContentService;
import com.uninote.backend.service.ResourceChatService;
import com.uninote.backend.service.UsageLimitService;

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
    private ChatRepository chatRepository;

    @Autowired
    private UsageLimitService usageLimitService;

    @Autowired
    private LangChainContentService langChainContentService;

    @Autowired
    private ResourceRepository resourceRepository;

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

        List<Object> chats = resourceChatService.getAllByUser(user);

        return ResponseEntity.ok(chats);
    }

    @PostMapping(value = "/{chatUuid}/message", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SseEmitter> addMessageToChat(
            @PathVariable String chatUuid,
            @RequestPart("userMessage") String userMessage,
            @RequestPart(value = "image", required = false) List<MultipartFile> uploadedImages) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        FirebaseAuthentication firebaseAuth = (FirebaseAuthentication) authentication;
        String userUid = firebaseAuth.getUid();

        User user = userRepository.findByFirebaseUid(userUid)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Chat chat = chatRepository.findByUuid(chatUuid)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        if (!chat.getUser().getId().equals(user.getId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        usageLimitService.checkDailyMessageLimit(user, chat);

        SseEmitter emitter = chatService.addMessageToChat(chatUuid, userMessage, uploadedImages);
        return ResponseEntity.ok(emitter);
    }



    @DeleteMapping("/{chatUuid}/clear")
    public ResponseEntity<String> clearChat(@PathVariable String chatUuid) {
        chatService.clearChat(chatUuid);
        return ResponseEntity.ok("Chat cleared successfully");
    }


    @DeleteMapping("/{chatUuid}")
    public ResponseEntity<String> deleteChat(@PathVariable String chatUuid) {
        chatService.deleteChat(chatUuid);
        return ResponseEntity.ok("Chat deleted successfully");
    }

    @PutMapping("/{chatUuid}")
    public ResponseEntity<ChatRequest> updateChat(@PathVariable String chatUuid, @RequestBody ChatRequest chatDto) {
        ChatRequest req = chatService.updateChat(chatUuid, chatDto);
        return ResponseEntity.ok(req);
    }

    @PostMapping("/{resourceId}/generate")
    public ResponseEntity<?> generateContent(
            @PathVariable Long resourceId,
            @RequestParam String type,
            @RequestParam(defaultValue = "replace") String mode) {

        try {
            Resource resource = resourceRepository.findById(resourceId)
                    .orElseThrow(() -> new RuntimeException("Resource not found with ID: " + resourceId));

            switch (type.toLowerCase()) {
                case "summary":
                    return ResponseEntity.ok(langChainContentService.generateSummary(resourceId));

                case "flashcards": {
                    String flashcardsJson;

                    if ("append".equalsIgnoreCase(mode)) {
                        Resource updated = langChainContentService.generateAdditionalFlashcards(resource);
                        flashcardsJson = updated.getFlashcards();
                    } else {
                        Resource updated = langChainContentService.generateFlashcards(resource);
                        flashcardsJson = updated.getFlashcards();
                    }

                    return ResponseEntity.ok()
                            .header("Content-Type", "application/json")
                            .body(flashcardsJson);
                }


                case "chapters":
                    return ResponseEntity.ok(langChainContentService.generateChapters(resourceId));

                case "quiz":
                    return ResponseEntity.ok()
                            .header("Content-Type", "application/json")
                            .body(langChainContentService.generateQuiz(resourceId));

                case "all":
                    return ResponseEntity.ok(langChainContentService.generateAllContent(resourceId));

                default:
                    return ResponseEntity.badRequest().body("Unknown content type: " + type);
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error generating content: " + e.getMessage());
        }
    }


    
}

