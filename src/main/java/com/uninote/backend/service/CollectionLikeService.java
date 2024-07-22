package com.uninote.backend.service;

import com.uninote.backend.entity.CollectionLike;
import com.uninote.backend.entity.NoteCollection;
import com.uninote.backend.entity.UniscoreIncreaseLog;
import com.uninote.backend.entity.UniscoreIncreaseType;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.CollectionLikeRepository;
import com.uninote.backend.repository.NoteCollectionRepository;
import com.uninote.backend.repository.UniscoreIncreaseLogRepository;
import com.uninote.backend.repository.UniscoreIncreaseTypeRepository;
import com.uninote.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CollectionLikeService {

    @Autowired
    private CollectionLikeRepository collectionLikeRepository;

    @Autowired
    private NoteCollectionRepository noteCollectionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UniscoreIncreaseTypeRepository uniscoreIncreaseTypeRepository;

    @Autowired
    private UniscoreIncreaseLogRepository uniscoreIncreaseLogRepository;

    @Transactional
    public void likeCollection(Long collectionId, Long userId) {
        NoteCollection collection = noteCollectionRepository.findById(collectionId)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        User collectionAdmin = collection.getAdmin();
        CollectionLike collectionLike = collectionLikeRepository.findByIdCollectionIdAndIdUserId(collectionId, userId);
        if (collectionLike == null) {
            collectionLike = new CollectionLike(collection, user);
            collectionLikeRepository.save(collectionLike);

            UniscoreIncreaseType likeIncreaseType = uniscoreIncreaseTypeRepository.findById(4L)
                    .orElseThrow(() -> new IllegalArgumentException("Increase type not found"));
            collectionAdmin.setUniscore(user.getUniscore() + likeIncreaseType.getIncreaseAmount());

            
            UniscoreIncreaseLog increaseLog = new UniscoreIncreaseLog(collectionAdmin, likeIncreaseType);
            uniscoreIncreaseLogRepository.save(increaseLog);
        } else if (!collectionLike.isActive()) {
            
            collectionLike.setActive(true);

            
            
        }
    }

    @Transactional
    public void unlikeCollection(Long collectionId, Long userId) {
        CollectionLike collectionLike = collectionLikeRepository.findByIdCollectionIdAndIdUserId(collectionId, userId);
        if (collectionLike != null && collectionLike.isActive()) {
            collectionLike.setActive(false);
        }
    }
}
