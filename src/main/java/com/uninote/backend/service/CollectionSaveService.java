package com.uninote.backend.service;

import com.uninote.backend.entity.*;
import com.uninote.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CollectionSaveService {

    @Autowired
    private CollectionSaveRepository collectionSaveRepository;

    @Autowired
    private NoteCollectionRepository noteCollectionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UniscoreIncreaseTypeRepository uniscoreIncreaseTypeRepository;

    @Autowired
    private UniscoreIncreaseLogRepository uniscoreIncreaseLogRepository;

    @Autowired
    private UserService userService;

    @Transactional
    public void saveCollection(Long collectionId, Long userId) {
        NoteCollection collection = noteCollectionRepository.findById(collectionId)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        User collectionAdmin = collection.getAdmin();
        CollectionSave collectionSave = collectionSaveRepository.findByIdCollectionIdAndIdUserId(collectionId, userId);
        if (collectionSave == null) {
            CollectionSaveId collectionSaveId = new CollectionSaveId(collectionId, userId);
            collectionSave = new CollectionSave(collection, user);
            collectionSaveRepository.save(collectionSave);
            userService.updateUniScore(collectionAdmin, 5L);
        } else if (!collectionSave.isActive()) {
            collectionSave.setActive(true);
        }
    }

    @Transactional
    public void unsaveCollection(Long collectionId, Long userId) {
        CollectionSave collectionSave = collectionSaveRepository.findByIdCollectionIdAndIdUserId(collectionId, userId);
        if (collectionSave != null && collectionSave.isActive()) {
            collectionSave.setActive(false);
        }
    }
}
