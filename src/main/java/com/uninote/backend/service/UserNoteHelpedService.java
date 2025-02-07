package com.uninote.backend.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.api.gax.rpc.InvalidArgumentException;
import com.uninote.backend.entity.Note;
import com.uninote.backend.entity.User;
import com.uninote.backend.entity.UserNoteHelped;
import com.uninote.backend.repository.NoteRepository;
import com.uninote.backend.repository.UserNoteHelpedRepository;
import com.uninote.backend.repository.UserRepository;

@Service
public class UserNoteHelpedService {

    @Autowired
    private  UserNoteHelpedRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NoteRepository noteRepository;

    public UserNoteHelped saveInteraction(Long userId, Long noteId, boolean helped) {
        UserNoteHelped interaction = repository.findByUserIdAndNoteId(userId, noteId);

        if (interaction == null) {
            User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User note found"));
            Note note = noteRepository.findById(noteId).orElseThrow(() -> new IllegalArgumentException("Note not found"));

            interaction = new UserNoteHelped();
            interaction.setUserId(userId);  
            interaction.setUser(user);
            interaction.setNote(note);
            interaction.setNoteId(noteId);
        }

        interaction.setHelped(helped);

        return repository.save(interaction);
    }



}
