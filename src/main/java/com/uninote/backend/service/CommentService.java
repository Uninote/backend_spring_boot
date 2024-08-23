package com.uninote.backend.service;

import com.uninote.backend.converter.EntityToDTOConverter;
import com.uninote.backend.dto.CommentDTO;
import com.uninote.backend.entity.Comment;
import com.uninote.backend.entity.Note;
import com.uninote.backend.entity.User;
import com.uninote.backend.interfaceProjection.CommentProjection;
import com.uninote.backend.repository.CommentRepository;
import com.uninote.backend.repository.NoteRepository;
import com.uninote.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentLikeService commentLikeService;
    
    @Transactional
    public Comment addComment(Long noteId, Long userId, String content) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Comment comment = new Comment(note, user, content);
        return commentRepository.save(comment);
    }

    public List<CommentProjection> getCommentsByNoteId(Long noteId) {
        return commentRepository.findCommentProjectionsByNoteId(noteId);
    }
    
    @Transactional
    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }
}
