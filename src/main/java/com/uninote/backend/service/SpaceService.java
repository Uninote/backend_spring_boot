package com.uninote.backend.service;

import com.uninote.backend.entity.Space;
import com.uninote.backend.entity.SpaceChat;
import com.uninote.backend.entity.SpaceResource;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uninote.backend.dto.FileResourceDTO;
import com.uninote.backend.dto.MessageDTO;
import com.uninote.backend.dto.ResourceDTO;
import com.uninote.backend.dto.SpaceSummaryDTO;
import com.uninote.backend.dto.TranscriptSnippetDto;
import com.uninote.backend.dto.YouTubeResourceDTO;
import com.uninote.backend.entity.Chat;
import com.uninote.backend.entity.Resource;
import com.uninote.backend.entity.User;
import com.uninote.backend.entity.YouTubeResource;
import com.uninote.backend.entity.FileResource;
import com.uninote.backend.entity.Note;
import com.uninote.backend.entity.NoteResource;
import com.uninote.backend.repository.SpaceRepository;
import com.uninote.backend.repository.SpaceResourceRepository;
import com.uninote.backend.repository.UserRepository;
import com.uninote.backend.repository.YouTubeResourceRepository;
import com.uninote.backend.repository.SpaceChatRepository;
import com.uninote.backend.repository.ChatRepository;
import com.uninote.backend.repository.FileResourceRepository;
import com.uninote.backend.repository.NoteRepository;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

@Service
public class SpaceService {

    @Autowired
    private SpaceRepository spaceRepository;
    
    @Autowired
    private SpaceChatRepository spaceChatRepository;
    
    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatService chatService;

    @Autowired
    private YouTubeResourceRepository youTubeResourceRepository;
    
    @Autowired
    private FileResourceRepository fileResourceRepository;

    @Autowired
    private SpaceResourceRepository spaceResourceRepository;

    @Autowired
    private ResourceService resourceService;
    
    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private MixPanelService mixPanelService;

    public Space createSpaceAndChat(String name, String userUid) {
        Space space = new Space();
        User user = userRepository.findByFirebaseUid(userUid)
            .orElseThrow(() -> new IllegalArgumentException("User not found for Firebase UID: " + userUid));        
        space.setTitle(name);
        space.setUuid(UUID.randomUUID().toString());
        space.setUser(user);
        space.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        space.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));

        Space savedSpace = spaceRepository.save(space);

        SpaceChat spaceChat = new SpaceChat();
        spaceChat.setSpace(savedSpace); 
        spaceChat.setUser(user);
        spaceChat.setUuid(UUID.randomUUID().toString());
        spaceChatRepository.save(spaceChat);

        savedSpace.getSpaceChats().add(spaceChat);

        spaceRepository.save(savedSpace);
        mixPanelService.trackEvent(user.getId(), "Space Creation", new JSONObject());

        return savedSpace; 
    }



    public Space findSpaceByUuidAndUser(String uuid, String userUid) {
        return spaceRepository.findByUuidAndUser_FirebaseUid(uuid, userUid).orElse(null);
    }

    public SpaceChat getFirstSpaceChat(Space space) {
        return spaceChatRepository.findFirstBySpace(space);
    }

    public List<MessageDTO> getChatMessagesById(Long chatId) {
        Chat chat = chatRepository.findById(chatId).orElse(null);
        if (chat == null) return List.of();
    
        return chatService.getChatMessagesByChat(chat)
                          .stream()
                          .map(MessageDTO::new)
                          .collect(Collectors.toList());
    }

    public Map<String, Object> getSpaceResources(String spaceUuid) {
        Map<String, Object> response = new HashMap<>();

        try {
            

            List<SpaceResource> spaceResources = spaceResourceRepository.findAllBySpace_Uuid(spaceUuid);
            if (spaceResources.isEmpty()) {
                response.put("resources", List.of());
                return response;
            }

            List<ResourceDTO> resourceDTOs = new ArrayList<>();

            for (SpaceResource spaceResource : spaceResources) {
                Resource resource = spaceResource.getResource();
                Long resourceId = resource.getId();

                FileResource fileResource = fileResourceRepository.findById(resourceId).orElse(null);
                if (fileResource != null && fileResource.getFileUrl() != null) {
                    String url = fileResource.getFileUrl();
                    String fileName = extractFileName(url);
                    //String signedUrl = getSignedUrl(fileName);

                    FileResourceDTO dto = new FileResourceDTO();
                    mapCommonFields(dto, resource);
                    //dto.setSupabaseFileUrl(signedUrl);
                   dto.setSupabaseFileUrl(url);

                    resourceDTOs.add(dto);
                    continue;
                }

                YouTubeResource ytResource = youTubeResourceRepository.findById(resourceId).orElse(null);
                if (ytResource != null) {
                    YouTubeResourceDTO dto = new YouTubeResourceDTO();
                    mapCommonFields(dto, resource);
                    dto.setYoutubeUrl(ytResource.getYoutubeUrl());
                    try {
                        String snippetsJson = ytResource.getSnippets();
                        if (snippetsJson != null && !snippetsJson.trim().isEmpty()) {
                            ObjectMapper objectMapper = new ObjectMapper();
                            List<TranscriptSnippetDto> snippetList = objectMapper.readValue(
                                snippetsJson,
                                new TypeReference<List<TranscriptSnippetDto>>() {}
                            );
                            dto.setTranscriptSnippets(snippetList);
                        } else {
                            dto.setTranscriptSnippets(Collections.emptyList());
                        }
                    } catch (Exception e) {
                        dto.setTranscriptSnippets(Collections.emptyList());
                    }

                    
                    resourceDTOs.add(dto);
                }
            }

            response.put("resources", resourceDTOs);
            return response;

        } catch (IllegalArgumentException e) {
            return Map.of("error", "Invalid UUID format.");
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", "Something went wrong.");
        }
    }

    private void mapCommonFields(ResourceDTO dto, Resource resource) {
        dto.setId(resource.getId());
        dto.setTitle(resource.getTitle());
        dto.setCreatedAt(resource.getCreatedAt());
        dto.setSummary(resource.getSummary());
        dto.setContent(resource.getContent());
        dto.setChapters(resource.getChapters());
        dto.setFlashcards(resource.getFlashcards());
        dto.setQuizzes(resource.getQuiz());
    }

    private String extractFileName(String url) {
        try {
            URI uri = new URI(url);
            String[] segments = uri.getPath().split("/");
            return segments.length > 0 ? segments[segments.length - 1] : null;
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public Resource addResourceToSpace(String type, MultipartFile file, String youtubeUrl, String spaceId, String userUid,Long noteId) {
        User user = userRepository.findByFirebaseUid(userUid)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Space space = spaceRepository.findByUuid(spaceId)
            .orElseThrow(() -> new IllegalArgumentException("Space not found"));

        Resource resource;

        switch (type.toLowerCase()) {
            case "file":
                if (file == null) throw new IllegalArgumentException("File is required for type=file");
                FileResource fileResource = resourceService.createFileResource(file);
                resource = fileResource;
                break;

            case "youtube":
                if (youtubeUrl == null) throw new IllegalArgumentException("youtubeUrl is required for type=youtube");
                YouTubeResource youTubeResource = resourceService.createYouTubeResource(youtubeUrl);
                resource = youTubeResource;
                break;
            case "note":
                if (noteId == null) throw new IllegalArgumentException("noteId is required for type=noteId");
                Note note = noteRepository.findById(noteId).orElseThrow(() -> new IllegalArgumentException("Note note found"));
                NoteResource noteResource = resourceService.createNoteResource(note);
                resource = noteResource;
                break;

            default:
                throw new IllegalArgumentException("Invalid type. Must be 'file' or 'youtube'.");
        }

        SpaceResource spaceResource = new SpaceResource();
        space.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        spaceResource.setSpace(space);
        spaceResource.setResource(resource);
        spaceResourceRepository.save(spaceResource);
        mixPanelService.trackEvent(user.getId(), "Add resource to space", new JSONObject());

        return resource;
    }

    public List<SpaceSummaryDTO> getSpacesByUser(String firebaseUid) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        return spaceRepository.findAllSummariesByUser(user);
    }

    public void removeResourceFromSpace(String spaceUuid, Long resourceId) {
        Space space = spaceRepository.findByUuid(spaceUuid)
                .orElseThrow(() -> new EntityNotFoundException("Space not found with UUID: " + spaceUuid));

        SpaceResource spaceResource = spaceResourceRepository
                .findBySpace_IdAndResource_Id(space.getId(), resourceId)
                .orElseThrow(() -> new EntityNotFoundException("Resource not found in the specified space"));

        spaceResourceRepository.delete(spaceResource);
    }

}
