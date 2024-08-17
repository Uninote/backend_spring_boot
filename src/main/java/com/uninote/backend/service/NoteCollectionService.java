package com.uninote.backend.service;

import com.uninote.backend.entity.CollectionLikeId;
import com.uninote.backend.entity.CollectionSave;
import com.uninote.backend.entity.CollectionSaveId;
import com.uninote.backend.entity.Note;
import com.uninote.backend.entity.NoteCollection;
import com.uninote.backend.entity.NoteCollectionItem;
import com.uninote.backend.entity.NoteLike;
import com.uninote.backend.entity.NoteSave;
import com.uninote.backend.entity.User;
import com.uninote.backend.converter.EntityToDTOConverter;
import com.uninote.backend.dto.NoteCollectionDTO;
import com.uninote.backend.entity.CollectionLike;
import com.uninote.backend.repository.CollectionLikeRepository;
import com.uninote.backend.repository.CollectionSaveRepository;
import com.uninote.backend.repository.NoteCollectionItemRepository;
import com.uninote.backend.repository.NoteCollectionRepository;
import com.uninote.backend.repository.NoteRepository;
import com.uninote.backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NoteCollectionService {
    
    
    @Autowired
    private NoteCollectionRepository noteCollectionRepository;


    @Autowired
    private CollectionLikeService collectionLikeService;
    



    @Autowired
    private NoteCollectionItemRepository noteCollectionItemRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private CollectionLikeRepository collectionLikeRepository;
    
    @Autowired
    private CollectionSaveRepository collectionSaveRepository;

    @Autowired
    private UserRepository userRepository;

    public NoteCollection createCollection(Long userId, String name, String description, Boolean isPublic) {
        NoteCollection collection = new NoteCollection();
        User admin = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User Not found"));
        collection.setAdmin(admin);
        collection.setName(name);
        collection.setDescription(description);
        collection.setIsPublic(isPublic);
        return noteCollectionRepository.save(collection);
    }
    public NoteCollection addNoteToCollection(Long collectionId, Long noteId) {
        NoteCollection collection = noteCollectionRepository.findById(collectionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid collection ID: " + collectionId));
        
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid note ID: " + noteId));

        if (!collection.getNotes().contains(note)) {
            collection.getNotes().add(note);
            return noteCollectionRepository.save(collection);
        } else {
            throw new IllegalArgumentException("Note already exists in the collection");
        }
    }
    public NoteCollection removeNoteFromCollection(Long collectionId, Long noteId) {
        NoteCollection collection = noteCollectionRepository.findById(collectionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid collection ID: " + collectionId));
        
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid note ID: " + noteId));

        if (collection.getNotes().contains(note)) {
            collection.getNotes().remove(note);
            return noteCollectionRepository.save(collection);
        } else {
            throw new IllegalArgumentException("Note does not exist in the collection");
        }
    }

    
    public List<NoteCollection> getCollectionsByUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User Note Found"));
        return noteCollectionRepository.findByAdmin(user);
    }

    public List<NoteCollectionItem> getNotesInCollection(Long collectionId) {
        return noteCollectionItemRepository.findByCollectionId(collectionId);
    }

    public List<NoteCollectionDTO> getPublicNoteCollections() {
        return noteCollectionRepository.findByIsPublicTrue().stream()
            .map(collection -> {
                NoteCollectionDTO dto = EntityToDTOConverter.convertCollectionToDTO(collection);
                Long likes = collectionLikeService.getTotalActiveLikesForCollection(collection.getCollectionId());
                dto.setLikes(likes);
                Long notes = noteCollectionItemRepository.countNotesInCollection(collection.getCollectionId());
                dto.setNoteNum(notes);
                return dto;
            })
            .collect(Collectors.toList());
    }
    
    

    public boolean hasUserLiked(Long collectionId, Long userId) {
        NoteCollection noteCollection = noteCollectionRepository.findById(collectionId)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found with ID: " + collectionId));
        
        CollectionLike like = collectionLikeRepository.findByIdCollectionIdAndIdUserId(collectionId, userId);
                return like!=null && like.isActive();
    }
    
    public boolean hasUserSaved(Long collectionId, Long userId) {
            NoteCollection noteCollection = noteCollectionRepository.findById(collectionId)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found with ID: " + collectionId));
            User user  = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID " + userId));
            CollectionSave save = collectionSaveRepository.findByIdCollectionIdAndIdUserId(collectionId, userId);
            return save!=null && save.isActive();
    }
}

