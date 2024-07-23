package com.uninote.backend.controller;

import com.uninote.backend.dto.CommentDTO;
import com.uninote.backend.entity.Comment;
import com.uninote.backend.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping
    public ResponseEntity<Comment> addComment(@RequestParam Long noteId, @RequestParam Long userId, @RequestParam String content) {
        Comment comment = commentService.addComment(noteId, userId, content);
        return ResponseEntity.ok(comment);
    }

    @GetMapping("/{noteId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByNoteId(@PathVariable Long noteId) {
        List<CommentDTO> comments = commentService.getCommentsByNoteId(noteId);
        return ResponseEntity.ok(comments);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
