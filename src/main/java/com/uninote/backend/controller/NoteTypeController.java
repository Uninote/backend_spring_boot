package com.uninote.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uninote.backend.dto.NoteTypeNameDTO;
import com.uninote.backend.entity.NoteType;
import com.uninote.backend.entity.NoteTypeName;
import com.uninote.backend.service.NoteTypeService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/note-types")
public class NoteTypeController {

    @Autowired
    private NoteTypeService noteTypeService;

    @GetMapping
    public List<NoteTypeNameDTO> getNoteTypes(@RequestParam(defaultValue = "EN") String language) {
        List<NoteTypeName> res = noteTypeService.getNoteTypesByLanguageCode(language);
        List<NoteTypeNameDTO> dtos = new ArrayList<NoteTypeNameDTO>();
        for (NoteTypeName name: res) {
            NoteTypeNameDTO dto = new NoteTypeNameDTO();
            dto.setTypeId(name.getTypeId());
            dto.setTypeName(name.getTypeName());
            dtos.add(dto);
        }
        return dtos;
    }

    
    public ResponseEntity<List<NoteTypeName>> getNoteTypesByLanguageCode(@RequestParam("languageCode") String languageCode) {
        List<NoteTypeName> noteTypes = noteTypeService.getNoteTypesByLanguageCode(languageCode);
        return ResponseEntity.ok(noteTypes);
    }
}
