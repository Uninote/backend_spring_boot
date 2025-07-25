package com.uninote.backend.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.transaction.Transactional;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.uninote.backend.dto.ResourceChatSummaryDTO;
import com.uninote.backend.dto.SimpleChatSummaryDTO;
import com.uninote.backend.entity.Chat;
import com.uninote.backend.entity.FileResource;
import com.uninote.backend.entity.Note;
import com.uninote.backend.entity.NoteResource;
import com.uninote.backend.entity.ResourceChat;
import com.uninote.backend.entity.User;
import com.uninote.backend.entity.YouTubeResource;
import com.uninote.backend.repository.ChatRepository;
import com.uninote.backend.repository.NoteRepository;
import com.uninote.backend.repository.ResourceChatRepository;
import com.uninote.backend.repository.UserRepository;

@Service
public class ResourceChatService {

    private static final Logger logger = LoggerFactory.getLogger(ResourceChatService.class);

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

    @Autowired
    private PosthogService posthogService;

    @Transactional
    public ResourceChat createWithFileResource(MultipartFile file, String userUid) {
        long overallStart = System.currentTimeMillis();
        logger.info("[createWithFileResource] Start for userUid={}", userUid);
        long t0 = System.currentTimeMillis();
        Chat chat = new Chat(); 
        chat.setUuid(UUID.randomUUID().toString());
        User user = userRepository.findByFirebaseUid(userUid)
            .orElseThrow(() -> new IllegalArgumentException("User not found for Firebase UID: " + userUid));
        chat.setUser(user);
        chat.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        chat.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        chat = chatRepository.save(chat);
        long t1 = System.currentTimeMillis();
        logger.info("[createWithFileResource] Chat created in {} ms", (t1-t0));
        FileResource fileResource = resourceService.createFileResource(file);
        long t2 = System.currentTimeMillis();
        logger.info("[createWithFileResource] FileResource created in {} ms", (t2-t1));
        chat.setTitle(fileResource.getTitle());
        ResourceChat resourceChat = new ResourceChat();
        resourceChat.setChat(chat);
        resourceChat.setResource(fileResource);
        JSONObject props = new JSONObject();
        props.put("type", "file");
        mixPanelService.trackEvent(user.getId(), "Chat Creation", props);
        long t3 = System.currentTimeMillis();
        logger.info("[createWithFileResource] MixPanel tracked in {} ms", (t3-t2));
        ResourceChat saved = resourceChatRepository.save(resourceChat);
        long t4 = System.currentTimeMillis();
        logger.info("[createWithFileResource] ResourceChat saved in {} ms", (t4-t3));
        // PostHog event
        Map<String, Object> phProps = new HashMap<>();
        phProps.put("type", "file");
        phProps.put("chat_id", chat.getId());
        phProps.put("chat_uuid", chat.getUuid());
        posthogService.captureEvent("chat_created", user.getId().toString(), phProps);
        logger.info("[createWithFileResource] Total time: {} ms", (t4-overallStart));
        return saved;
    }

    @Transactional
    public ResourceChat createWithYouTubeResource(String youtubeUrl, String userUid) {
        long overallStart = System.currentTimeMillis();
        logger.info("[createWithYouTubeResource] Start for userUid={}", userUid);
        long t0 = System.currentTimeMillis();
        Chat chat = new Chat(); 
        chat.setUuid(UUID.randomUUID().toString());
        User user = userRepository.findByFirebaseUid(userUid)
            .orElseThrow(() -> new IllegalArgumentException("User not found for Firebase UID: " + userUid));
        chat.setUser(user);
        chat = chatRepository.save(chat);
        long t1 = System.currentTimeMillis();
        logger.info("[createWithYouTubeResource] Chat created in {} ms", (t1-t0));
        YouTubeResource ytResource = resourceService.createYouTubeResource(youtubeUrl);
        long t2 = System.currentTimeMillis();
        logger.info("[createWithYouTubeResource] YouTubeResource created in {} ms", (t2-t1));
        chat.setTitle(ytResource.getTitle());
        ResourceChat resourceChat = new ResourceChat();
        chat.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        chat.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        resourceChat.setChat(chat);
        resourceChat.setResource(ytResource);
        JSONObject props = new JSONObject();
        props.put("type", "youtube");
        mixPanelService.trackEvent(user.getId(), "Chat Creation", props);
        long t3 = System.currentTimeMillis();
        logger.info("[createWithYouTubeResource] MixPanel tracked in {} ms", (t3-t2));
        ResourceChat saved = resourceChatRepository.save(resourceChat);
        long t4 = System.currentTimeMillis();
        logger.info("[createWithYouTubeResource] ResourceChat saved in {} ms", (t4-t3));
        // PostHog event
        Map<String, Object> phProps = new HashMap<>();
        phProps.put("type", "youtube");
        phProps.put("chat_id", chat.getId());
        phProps.put("chat_uuid", chat.getUuid());
        posthogService.captureEvent("chat_created", user.getId().toString(), phProps);
        logger.info("[createWithYouTubeResource] Total time: {} ms", (t4-overallStart));
        return saved;
    }

    public List<Object> getAllByUser(User user) {
        List<ResourceChatSummaryDTO> resourceChats = resourceChatRepository.findAllSummaryByUser(user);
        List<SimpleChatSummaryDTO> simpleChats = chatRepository.findSimpleChatSummariesByUser(user);

        List<Object> all = new ArrayList<>();
        all.addAll(resourceChats);
        all.addAll(simpleChats);
        all.sort((a, b) -> {
            Timestamp aTime = (a instanceof ResourceChatSummaryDTO)
                    ? ((ResourceChatSummaryDTO) a).getCreatedAt()
                    : ((SimpleChatSummaryDTO) a).getCreatedAt();

            Timestamp bTime = (b instanceof ResourceChatSummaryDTO)
                    ? ((ResourceChatSummaryDTO) b).getCreatedAt()
                    : ((SimpleChatSummaryDTO) b).getCreatedAt();

            if (aTime == null && bTime == null) return 0;
            if (aTime == null) return 1;  // nulls last
            if (bTime == null) return -1;

            return bTime.compareTo(aTime); // Descending
        });

        return all;    
    }


    public ResourceChat createWithNote(Long noteId, String userUid) {
        long overallStart = System.currentTimeMillis();
        logger.info("[createWithNote] Start for userUid={}", userUid);
        long t0 = System.currentTimeMillis();
        Chat chat = new Chat(); 
        chat.setUuid(UUID.randomUUID().toString());
        User user = userRepository.findByFirebaseUid(userUid)
            .orElseThrow(() -> new IllegalArgumentException("User not found for Firebase UID: " + userUid));
        chat.setUser(user);
        Note note = noteRepository.getById(noteId);
        chat.setTitle(note.getTitle());
        chat.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        chat.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        chat = chatRepository.save(chat);
        long t1 = System.currentTimeMillis();
        logger.info("[createWithNote] Chat created in {} ms", (t1-t0));
        NoteResource nr = resourceService.createNoteResource(note);
        long t2 = System.currentTimeMillis();
        logger.info("[createWithNote] NoteResource created in {} ms", (t2-t1));
        ResourceChat resourceChat = new ResourceChat();
        resourceChat.setChat(chat);
        resourceChat.setResource(nr);
        JSONObject props = new JSONObject();
        props.put("type", "note");
        mixPanelService.trackEvent(user.getId(), "Chat Creation", props);
        long t3 = System.currentTimeMillis();
        logger.info("[createWithNote] MixPanel tracked in {} ms", (t3-t2));
        ResourceChat saved = resourceChatRepository.save(resourceChat);
        long t4 = System.currentTimeMillis();
        logger.info("[createWithNote] ResourceChat saved in {} ms", (t4-t3));
        // PostHog event
        Map<String, Object> phProps = new HashMap<>();
        phProps.put("type", "note");
        phProps.put("chat_id", chat.getId());
        phProps.put("chat_uuid", chat.getUuid());
        posthogService.captureEvent("chat_created", user.getId().toString(), phProps);
        logger.info("[createWithNote] Total time: {} ms", (t4-overallStart));
        return saved;
    }


    @Transactional
    public ResourceChat createWithNoteResource(Note note, String userUid) {
        long overallStart = System.currentTimeMillis();
        logger.info("[createWithNoteResource] Start for userUid={}", userUid);
        long t0 = System.currentTimeMillis();
        Chat chat = new Chat(); 
        chat.setUuid(UUID.randomUUID().toString());
        User user = userRepository.findByFirebaseUid(userUid)
            .orElseThrow(() -> new IllegalArgumentException("User not found for Firebase UID: " + userUid));
        chat.setUser(user);
        chat.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        chat.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        chat = chatRepository.save(chat);
        long t1 = System.currentTimeMillis();
        logger.info("[createWithNoteResource] Chat created in {} ms", (t1-t0));
        NoteResource noteResource = resourceService.createNoteResource(note);
        long t2 = System.currentTimeMillis();
        logger.info("[createWithNoteResource] NoteResource created in {} ms", (t2-t1));
        ResourceChat resourceChat = new ResourceChat();
        resourceChat.setChat(chat);
        resourceChat.setResource(noteResource);
        ResourceChat saved = resourceChatRepository.save(resourceChat);
        long t3 = System.currentTimeMillis();
        logger.info("[createWithNoteResource] ResourceChat saved in {} ms", (t3-t2));
        logger.info("[createWithNoteResource] Total time: {} ms", (t3-overallStart));
        return saved;
    }

    public Chat createSimpleChat(String userUid) {
        Chat chat = new Chat(); 
        chat.setUuid(UUID.randomUUID().toString());

        User user = userRepository.findByFirebaseUid(userUid)
            .orElseThrow(() -> new IllegalArgumentException("User not found for Firebase UID: " + userUid));
        chat.setUser(user);

        return chat = chatRepository.save(chat);
        
    }

}

