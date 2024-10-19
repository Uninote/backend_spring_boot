package com.uninote.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uninote.backend.entity.NoteType;
import com.uninote.backend.repository.NoteTypeRepository;

import java.util.List;

@Service
public class NoteTypeService {

    @Autowired
    private NoteTypeRepository noteTypeRepository;

    public List<NoteType> getAllNoteTypes() {
        return noteTypeRepository.findAll();
    }
}
