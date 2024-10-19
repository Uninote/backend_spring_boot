package com.uninote.backend.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uninote.backend.entity.NoteRequest;
import com.uninote.backend.repository.NoteRequestRepository;

import java.util.List;

@Service
public class NoteRequestService {

    @Autowired
    private NoteRequestRepository noteRequestRepository;

    public List<NoteRequest> getAllRequests() {
        return noteRequestRepository.findAll();
    }

    public NoteRequest createNoteRequest(Long courseId, Long usrId) {
        NoteRequest noteRequest = new NoteRequest(courseId, usrId);
        return noteRequestRepository.save(noteRequest);
    }

    public NoteRequest updateRequestStatus(Long requestId, Boolean status) {
        NoteRequest request = noteRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        request.setRequestStatus(status);
        return noteRequestRepository.save(request);
    }

    public List<NoteRequest> getRequestsByUser(Long usrId) {
        return noteRequestRepository.findByUserId(usrId);
    }

    public List<NoteRequest> getPendingRequests() {
        return noteRequestRepository.findByRequestStatus(false);  
    }
}