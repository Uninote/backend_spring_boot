package com.uninote.backend.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

     public List<Map<String, Object>> getNoteViewsLast30Days() {
        List<Object[]> results = noteMetricRepository.countLast30daysNoteViews();
        
        
        return results.stream().map(result -> {
            Map<String, Object> map = new HashMap<>();
            map.put("day", result[0].toString());  
            map.put("count", result[1]);           
            return map;
        }).collect(Collectors.toList());
    }
}
