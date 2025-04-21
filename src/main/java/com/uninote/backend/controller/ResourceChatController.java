package com.uninote.backend.controller;

import com.uninote.backend.config.security.FirebaseAuthentication;
import com.uninote.backend.entity.ResourceChat;
import com.uninote.backend.entity.User;
import com.uninote.backend.service.ResourceChatService;
import com.uninote.backend.service.ResourceService;

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
    private ResourceChatService resourceChatService;


    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> createResourceChat(
            @RequestParam String type,
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false) String url
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseEntity<>("Authorization token missing or invalid.", HttpStatus.UNAUTHORIZED);
        }

        FirebaseAuthentication firebaseAuth = (FirebaseAuthentication) authentication;
        String userUid = firebaseAuth.getUid();

        ResourceChat created;

        switch (type.toLowerCase()) {
            case "file":
                if (file == null) return ResponseEntity.badRequest().body("File is required for type=file");
                created = resourceChatService.createWithFileResource(file, userUid);
                break;

            case "youtube":
                if (url == null) return ResponseEntity.badRequest().body("youtubeUrl is required for type=youtube");
                created = resourceChatService.createWithYouTubeResource(url, userUid);
                break;

            default:
                return ResponseEntity.badRequest().body("Invalid type. Must be 'file' or 'youtube'.");
        }

        return ResponseEntity.ok(created);
    }
}