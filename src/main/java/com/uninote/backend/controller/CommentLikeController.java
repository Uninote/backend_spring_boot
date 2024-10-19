package com.uninote.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.uninote.backend.service.CommentLikeService;

@RestController
@RequestMapping("/comment-likes")
public class CommentLikeController {

    @Autowired
    private CommentLikeService commentLikeService;

    
    @PostMapping("/like")
    public ResponseEntity<String> likeComment(@RequestParam Long commentId, @RequestParam Long userId) {
        commentLikeService.likeComment(commentId, userId);
        return ResponseEntity.ok("Comment liked successfully.");
    }

    
    @PostMapping("/unlike")
    public ResponseEntity<String> unlikeComment(@RequestParam Long commentId, @RequestParam Long userId) {
        commentLikeService.unlikeComment(commentId, userId);
        return ResponseEntity.ok("Comment unliked successfully.");
    }

    @GetMapping("/is-liked/{commentId}/{userId}")
    public ResponseEntity<Boolean> isCommentLikedByUser(@PathVariable Long commentId, @PathVariable Long userId) {
        boolean isLiked = commentLikeService.isCommentLikedByUser(commentId, userId);
        return ResponseEntity.ok(isLiked);
    }   

    @GetMapping("/total-likes/{commentId}")
    public ResponseEntity<Long> getTotalCommentLikes(@PathVariable Long commentId) {
        long totalLikes = commentLikeService.getTotalCommentLikes(commentId);
        return ResponseEntity.ok(totalLikes);
    }

    @PostMapping("/toggle-like/{userId}/{commentId}")
    public ResponseEntity<String> toggleLikeComment(@PathVariable Long commentId, @PathVariable Long userId) {
        boolean isLiked = commentLikeService.isCommentLikedByUser(commentId, userId);

        if (isLiked) {
            commentLikeService.unlikeComment(commentId, userId);
            return ResponseEntity.ok("Comment unliked successfully.");
        } else {
            commentLikeService.likeComment(commentId, userId);
            return ResponseEntity.ok("Comment liked successfully.");
        }
    }
}