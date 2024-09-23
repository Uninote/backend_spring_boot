package com.uninote.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uninote.backend.dto.NoteMetricDTO;
import com.uninote.backend.repository.NoteMetricRepository;

@Service
public class NoteMetricService {

    private final NoteMetricRepository noteMetricRepository;

    @Autowired
    public NoteMetricService(NoteMetricRepository noteMetricRepository) {
        this.noteMetricRepository = noteMetricRepository;
    }

    public List<NoteMetricDTO> getNoteMetrics() {
        return noteMetricRepository.findNoteMetrics();
    }

    public List<Object[]> getNotesCountAndUniqueCreators(Long departmentId) {
        return noteMetricRepository.getNotesCountAndUniqueCreatorsByDepartment(departmentId);
    }
}
