package com.uninote.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uninote.backend.entity.CollectionLike;
import com.uninote.backend.entity.CollectionLikeId;
import com.uninote.backend.entity.Note;
import com.uninote.backend.entity.NoteCollection;
import com.uninote.backend.entity.User;

public interface CollectionLikeRepository extends JpaRepository<CollectionLike, CollectionLikeId> {
    
    CollectionLike findByIdCollectionIdAndIdUserId(Long collectionId, Long userId);

}
