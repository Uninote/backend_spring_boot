package com.uninote.backend.service;

import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uninote.backend.entity.Comment;
import com.uninote.backend.entity.CommentLike;
import com.uninote.backend.entity.CommentLikeId;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.CommentLikeRepository;
import com.uninote.backend.repository.CommentRepository;
import com.uninote.backend.repository.UserRepository;

@Service
public class CommentLikeService {

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Transactional
    public void likeComment(Long commentId, Long userId) {
        
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        User commentCreator = comment.getUser();

        Optional<CommentLike> existingLike = commentLikeRepository.findByComment_CommentIdAndUser_Id(commentId, userId);

        if (existingLike.isEmpty()) {
            
            CommentLikeId commentLikeId = new CommentLikeId(commentId, userId);
            CommentLike commentLike = new CommentLike(commentLikeId, comment, user);
            commentLikeRepository.save(commentLike); 

            userService.updateUniScore(commentCreator, 1L);
        }
    }

    @Transactional
    public void unlikeComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Optional<CommentLike> existingLike = commentLikeRepository.findByComment_CommentIdAndUser_Id(commentId, userId);

        if (existingLike.isPresent()) {
            commentLikeRepository.delete(existingLike.get());

            userService.updateUniScore(comment.getUser(), -1L);
        }
    }

    public boolean isCommentLikedByUser(Long commentId, Long userId) {
        CommentLikeId id = new CommentLikeId();
        id.setCommentId(commentId);
        id.setUserId(userId);
        return commentLikeRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public long getTotalCommentLikes(Long commentId) {
        return commentLikeRepository.countByComment_CommentId(commentId);
    }
}
