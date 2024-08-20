package com.uninote.backend.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uninote.backend.entity.CommentLike;
import com.uninote.backend.entity.CommentLikeId;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, CommentLikeId> {
    boolean existsById(CommentLikeId id);
    void deleteById(CommentLikeId id);
    Optional<CommentLike> findByComment_CommentIdAndUser_Id(Long commentId, Long userId);
    long countByComment_CommentId(Long commentId);


}