package com.uninote.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uninote.backend.entity.NoteType;
import com.uninote.backend.service.NoteTypeService;

import java.util.List;

@RestController
@RequestMapping("/note-types")
public class NoteTypeController {

    @Autowired
    private NoteTypeService noteTypeService;

    @GetMapping
    public List<NoteType> getNoteTypes() {
        return noteTypeService.getAllNoteTypes();
    }
}
