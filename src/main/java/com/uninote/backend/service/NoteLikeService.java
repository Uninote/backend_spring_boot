package com.uninote.backend.service;

import com.uninote.backend.entity.Note;
import com.uninote.backend.entity.NoteLike;
import com.uninote.backend.entity.NoteLikeId;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.NoteLikeRepository;
import com.uninote.backend.repository.NoteRepository;
import com.uninote.backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NoteLikeService {

    @Autowired
    private NoteLikeRepository noteLikeRepository;

    @Autowired
    private NoteRepository noteRepository;
    @Autowired
    private UserRepository userRepository;
    /*@Transactional
    public void likeNote(Long noteId, Long userId) {
        Note noteD = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if the user has already liked the note
        if (!noteLikeRepository.existsByNoteIdAndUserId(noteId, userId)) {
            NoteLikeId noteLikeId = new NoteLikeId(noteId, userId);
            NoteLike noteLike = new NoteLike(noteLikeId, note, user);
            noteLikeRepository.save(noteLike);

            // Increment the likes counter
            note.setLikes(note.getLikes() + 1);
            noteRepository.save(note);
        }
    }

    @Transactional
    public void unlikeNote(Long noteId, Long userId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));

        // Check if the user has liked the note
        NoteLike noteLike = noteLikeRepository.findByNoteIdAndUserId(noteId, userId);
        if (noteLike != null) {
            // Remove the like
            noteLikeRepository.delete(noteLike);

            // Decrement the likes counter
            note.setLikes(note.getLikes() - 1);
            noteRepository.save(note);
        }
    }*/
}
