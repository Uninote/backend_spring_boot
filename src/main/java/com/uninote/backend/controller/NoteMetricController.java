package com.uninote.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uninote.backend.dto.NoteMetricDTO;
import com.uninote.backend.service.NoteMetricService;

@RestController
@RequestMapping("/notes-metrics")
public class NoteMetricController {

    @Autowired
    private NoteMetricService noteMetricService;

    

    @GetMapping("/notes-per-department")
    public List<NoteMetricDTO> getNoteMetrics() {
        return noteMetricService.getNoteMetrics();
    }

    @GetMapping("/notes-count-and-creators")
    public List<Object[]> getNotesCountAndUniqueCreators(@RequestParam Long departmentId) {
        return noteMetricService.getNotesCountAndUniqueCreators(departmentId);
    }
}
