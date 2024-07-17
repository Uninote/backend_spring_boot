package com.uninote.backend.repository;

import com.uninote.backend.entity.NoteLike;
import com.uninote.backend.entity.NoteLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteLikeRepository extends JpaRepository<NoteLike, NoteLikeId> {
    boolean existsByNoteIdAndUserId(Long noteId, Long userId);
    NoteLike findByNoteIdAndUserId(Long noteId, Long userId);
}
