package com.uninote.backend.controller;

import com.uninote.backend.dto.ChatHistoryDto;
import com.uninote.backend.service.ChatService;
import com.uninote.backend.service.ResourceChatService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    @Autowired
    private ResourceChatService resourceChatService;

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

}

