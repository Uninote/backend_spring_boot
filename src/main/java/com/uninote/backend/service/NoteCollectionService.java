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
import com.uninote.backend.interfaceProjection.CollectionProjection;
import com.uninote.backend.interfaceProjection.NoteProjection;
import com.uninote.backend.converter.EntityToDTOConverter;
import com.uninote.backend.dto.CollectionDTO;
import com.uninote.backend.dto.NoteCollectionDTO;
import com.uninote.backend.dto.NoteDTO;
import com.uninote.backend.entity.CollectionLike;
import com.uninote.backend.repository.CollectionLikeRepository;
import com.uninote.backend.repository.CollectionSaveRepository;
import com.uninote.backend.repository.NoteCollectionItemRepository;
import com.uninote.backend.repository.NoteCollectionRepository;
import com.uninote.backend.repository.NoteRepository;
import com.uninote.backend.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NoteCollectionService {
    
    private static final Logger logger = LoggerFactory.getLogger(NoteCollectionService.class);
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

    public List<NoteProjection> getNotesInCollection(Long collectionId) {
        List<NoteProjection> items=  noteCollectionItemRepository.findNoteProjectionsByCollectionId(collectionId);
        
        return items;
    }

     public List<CollectionDTO> getPublicCollections() {
        List<CollectionProjection> collections = noteCollectionRepository.findPublicCollections();
        List<CollectionDTO> collectionDTOs = new ArrayList<>();

        for (CollectionProjection collection : collections) {
            List<NoteProjection> notes = noteCollectionItemRepository.findNoteProjectionsByCollectionId(collection.getCollectionId());
            NoteProjection firstNote = notes.isEmpty() ? null : notes.get(0);
            CollectionDTO collectionDTO = new CollectionDTO(
                    collection.getCollectionId(),
                    collection.getName(),
                    collection.getIsPublic(),
                    collection.getAdminUsername(),
                    collection.getTotalLikes(),
                    collection.getNoteNum(),
                    firstNote
            );
            collectionDTOs.add(collectionDTO);
        }
        return collectionDTOs;
    }

    public List<CollectionProjection> getUserCollections(Long userId) {
        return noteCollectionRepository.findCollectionsByUser(userId);
    }
    
    public CollectionProjection getCollectionDetails(Long collectionId) {
        return noteCollectionRepository.findCollectionProjectionById(collectionId);
    }
    

    public boolean hasUserLiked(Long collectionId, Long userId) {
        
        return collectionLikeRepository.existsByCollectionIdAndUserIdAndIsActive(collectionId, userId);
    }
    
    public boolean hasUserSaved(Long collectionId, Long userId) {
            return collectionSaveRepository.existsByCollectionIdAndUserIdAndIsActive(collectionId, userId);
    }


    @Transactional
        public void softDeleteCollection(Long collectionId) {
            NoteCollection collection = noteCollectionRepository.findById(collectionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid collection ID"));
            logger.debug("deleting collection {}", collectionId);
            collection.setDeleted(true);
            logger.debug("deleting collection {}", collection.getDeleted());
            noteCollectionRepository.save(collection);
    }
}

