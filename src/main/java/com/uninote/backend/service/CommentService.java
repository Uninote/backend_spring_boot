package com.uninote.backend.service;

import com.uninote.backend.entity.Comment;
import com.uninote.backend.entity.Note;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.CommentRepository;
import com.uninote.backend.repository.NoteRepository;
import com.uninote.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Comment addComment(Long noteId, Long userId, String content) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Comment comment = new Comment(note, user, content);
        return commentRepository.save(comment);
    }

    public List<Comment> getCommentsByNoteId(Long noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));
        return commentRepository.findByNote(note);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }
}
