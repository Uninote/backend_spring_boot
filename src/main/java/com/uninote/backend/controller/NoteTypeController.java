package com.uninote.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uninote.backend.entity.NoteType;
import com.uninote.backend.entity.NoteTypeName;
import com.uninote.backend.service.NoteTypeService;

import java.util.List;

@RestController
@RequestMapping("/note-types")
public class NoteTypeController {

    @Autowired
    private NoteTypeService noteTypeService;

    @GetMapping
    public List<NoteTypeName> getNoteTypes(@RequestParam(defaultValue = "EN") String languageCode) {
        return noteTypeService.getNoteTypesByLanguageCode(languageCode);
    }

    
    public ResponseEntity<List<NoteTypeName>> getNoteTypesByLanguageCode(@RequestParam("languageCode") String languageCode) {
        List<NoteTypeName> noteTypes = noteTypeService.getNoteTypesByLanguageCode(languageCode);
        return ResponseEntity.ok(noteTypes);
    }
}
