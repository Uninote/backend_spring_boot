package com.uninote.backend.service;

import com.uninote.backend.dto.ResourceChatSummaryDTO;
import com.uninote.backend.entity.Chat;
import com.uninote.backend.entity.FileResource;
import com.uninote.backend.entity.Note;
import com.uninote.backend.entity.NoteResource;
import com.uninote.backend.entity.Resource;
import com.uninote.backend.entity.ResourceChat;
import com.uninote.backend.entity.User;
import com.uninote.backend.entity.YouTubeResource;
import com.uninote.backend.repository.ChatRepository;
import com.uninote.backend.repository.NoteRepository;
import com.uninote.backend.repository.ResourceChatRepository;
import com.uninote.backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Autowired
    private NoteRepository noteRepository;

    @Transactional
    public ResourceChat createWithFileResource(MultipartFile file, String userUid) {
        Chat chat = new Chat(); 
        chat.setUuid(UUID.randomUUID().toString());

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
        chat.setUuid(UUID.randomUUID().toString());

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

    public List<ResourceChatSummaryDTO> getAllByUser(User user) {
        List<ResourceChat> chats = resourceChatRepository.findAllByChat_User(user);
        
        return chats.stream()
            .map(chat -> new ResourceChatSummaryDTO(
                chat.getChat().getId(),
                chat.getChat().getUuid(),
                chat.getResource().getTitle(),
                chat.getResource() instanceof FileResource ? "file" : "youtube",
                chat.getChat().getCreatedAt(),
                chat.getChat().getTitle()
            ))
            .collect(Collectors.toList());
    }

    public ResourceChat createWithNote(Long noteId, String userUid) {
        Chat chat = new Chat(); 
        chat.setUuid(UUID.randomUUID().toString());

        User user = userRepository.findByFirebaseUid(userUid)
            .orElseThrow(() -> new IllegalArgumentException("User not found for Firebase UID: " + userUid));
        chat.setUser(user);
        chat = chatRepository.save(chat);
        Note note = noteRepository.getById(noteId);
        NoteResource nr = resourceService.createNoteResource(note);
        ResourceChat resourceChat = new ResourceChat();
        resourceChat.setChat(chat);
        resourceChat.setResource(nr);

        return resourceChatRepository.save(resourceChat);    }


    @Transactional
    public ResourceChat createWithNoteResource(Note note, String userUid) {
        Chat chat = new Chat(); 
        chat.setUuid(UUID.randomUUID().toString());

        User user = userRepository.findByFirebaseUid(userUid)
            .orElseThrow(() -> new IllegalArgumentException("User not found for Firebase UID: " + userUid));
        chat.setUser(user);

        chat = chatRepository.save(chat);

        NoteResource noteResource = resourceService.createNoteResource(note);

        ResourceChat resourceChat = new ResourceChat();
        resourceChat.setChat(chat);
        resourceChat.setResource(noteResource);

        return resourceChatRepository.save(resourceChat);
    }

}

