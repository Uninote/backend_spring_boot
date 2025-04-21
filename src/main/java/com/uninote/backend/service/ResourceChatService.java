package com.uninote.backend.service;

import com.uninote.backend.entity.Chat;
import com.uninote.backend.entity.FileResource;
import com.uninote.backend.entity.Resource;
import com.uninote.backend.entity.ResourceChat;
import com.uninote.backend.entity.User;
import com.uninote.backend.entity.YouTubeResource;
import com.uninote.backend.repository.ChatRepository;
import com.uninote.backend.repository.ResourceChatRepository;
import com.uninote.backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;

@Service
public class ResourceChatService {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ResourceChatRepository resourceChatRepository;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public ResourceChat createWithFileResource(MultipartFile file, String userUid) {
        Chat chat = new Chat(); 
        User user = userRepository.findByFirebaseUid(userUid)
            .orElseThrow(() -> new IllegalArgumentException("User not found for Firebase UID: " + userUid));
        chat.setUser(user);

        chat = chatRepository.save(chat);

        FileResource fileResource = resourceService.createFileResource(file);

        ResourceChat resourceChat = new ResourceChat();
        resourceChat.setChat(chat);
        resourceChat.setResource(fileResource);

        return resourceChatRepository.save(resourceChat);
    }

    @Transactional
    public ResourceChat createWithYouTubeResource(String youtubeUrl, String userUid) {
        Chat chat = new Chat(); 
        User user = userRepository.findByFirebaseUid(userUid)
            .orElseThrow(() -> new IllegalArgumentException("User not found for Firebase UID: " + userUid));
        chat.setUser(user);
        chat = chatRepository.save(chat);

        YouTubeResource ytResource = resourceService.createYouTubeResource(youtubeUrl);

        ResourceChat resourceChat = new ResourceChat();
        resourceChat.setChat(chat);
        resourceChat.setResource(ytResource);

        return resourceChatRepository.save(resourceChat);
    }
}

