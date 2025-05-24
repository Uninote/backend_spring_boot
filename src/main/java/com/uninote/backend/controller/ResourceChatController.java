package com.uninote.backend.controller;

import com.uninote.backend.config.security.FirebaseAuthentication;
import com.uninote.backend.dto.ResourceChatResponseDTO;
import com.uninote.backend.dto.SimpleChatResponseDTO;
import com.uninote.backend.entity.Chat;
import com.uninote.backend.entity.FileResource;
import com.uninote.backend.entity.Resource;
import com.uninote.backend.entity.ResourceChat;
import com.uninote.backend.entity.User;
import com.uninote.backend.entity.YouTubeResource;
import com.uninote.backend.exceptions.EmptyContentException;
import com.uninote.backend.repository.UserRepository;
import com.uninote.backend.service.ResourceChatService;
import com.uninote.backend.service.ResourceService;
import com.uninote.backend.service.UsageLimitService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/chat")
public class ResourceChatController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UsageLimitService usageLimitService;

    @Autowired
    private ResourceChatService resourceChatService;


    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> createResourceChat(
            @RequestParam String type,
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false) String url,
            @RequestParam(required = false) Long noteId
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body("Authorization token missing or invalid.");
        }

        FirebaseAuthentication firebaseAuth = (FirebaseAuthentication) authentication;
        String userUid = firebaseAuth.getUid();
        User user = userRepository.findByFirebaseUid(userUid)
            .orElseThrow(() -> new RuntimeException("User not found"));
        usageLimitService.checkDailyChatLimit(user);


        try {
            ResourceChat created;

            switch (type.toLowerCase()) {
                case "file":
                    if (file == null) {
                        return ResponseEntity.badRequest().body("File is required for type=file");
                    }
                    created = resourceChatService.createWithFileResource(file, userUid);
                    break;

                case "youtube":
                    if (url == null || url.trim().isEmpty()) {
                        return ResponseEntity.badRequest().body("youtubeUrl is required for type=youtube");
                    }
                    created = resourceChatService.createWithYouTubeResource(url, userUid);
                    break;
                case "note":
                    if(noteId == null) {
                        return ResponseEntity.badRequest().body("notId is required for type=note");
                    }
                    created = resourceChatService.createWithNote(noteId, userUid);
                    break;
                case "chat":
                    Chat chat = resourceChatService.createSimpleChat(userUid);
                    SimpleChatResponseDTO dto = new SimpleChatResponseDTO(
                        chat.getId(),
                        chat.getTitle(),
                        chat.getUuid(),
                        chat.getCreatedAt(),
                        chat.getUpdatedAt()
                    );
                    return ResponseEntity.ok(dto);


                    

                default:
                    return ResponseEntity.badRequest().body("Invalid type. Must be 'file' or 'youtube' or 'note' or 'chat'.");
            }

            Resource resource = created.getResource(); 
            String resourceType;
            String resourceUrl;

            if (resource instanceof YouTubeResource) {
                resourceType = "youtube";
                resourceUrl = ((YouTubeResource) resource).getYoutubeUrl();
            } else if (resource instanceof FileResource) {
                resourceType = "file";
                resourceUrl = ((FileResource) resource).getFileUrl();
            } else {
                resourceType = "unknown";
                resourceUrl = null;
            }

            ResourceChatResponseDTO dto = new ResourceChatResponseDTO(
                    created.getId(),
                    created.getChat().getTitle(),
                    created.getChat().getUuid(),
                    created.getChat().getCreatedAt(),
                    created.getChat().getUpdatedAt(),
                    resource.getSummary(),
                    resourceUrl,
                    resourceType
            );

            return ResponseEntity.ok(dto);

        } catch (EmptyContentException e) {
            throw e;
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Failed to create resource chat: " + e.getMessage());
        }
    }


}