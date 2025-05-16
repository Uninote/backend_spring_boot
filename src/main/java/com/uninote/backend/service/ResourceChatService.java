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

import org.json.JSONObject;
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

    @Autowired
    private MixPanelService mixPanelService;

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
        JSONObject props = new JSONObject();
        props.put("type", "file");
        mixPanelService.trackEvent(user.getId(), "Chat Creation", props);
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
        JSONObject props = new JSONObject();
        props.put("type", "youtube");
        mixPanelService.trackEvent(user.getId(), "Chat Creation", props);
        return resourceChatRepository.save(resourceChat);
    }

    public List<ResourceChatSummaryDTO> getAllByUser(User user) {
        List<ResourceChatSummaryDTO> chats = resourceChatRepository.findAllSummaryByUser(user);
        
        return chats;
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
        JSONObject props = new JSONObject();
        props.put("type", "note");
        mixPanelService.trackEvent(user.getId(), "Chat Creation", props);

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

