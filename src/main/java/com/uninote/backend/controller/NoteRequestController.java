package com.uninote.backend.controller;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.uninote.backend.entity.NoteRequest;
import com.uninote.backend.service.NoteRequestService;

import java.util.List;

@RestController
@RequestMapping("/note-requests")
public class NoteRequestController {

    @Autowired
    private NoteRequestService noteRequestService;

    
    @GetMapping
    public List<NoteRequest> getAllNoteRequests() {
        return noteRequestService.getAllRequests();
    }

   
    @PostMapping("/create")
    public NoteRequest createNoteRequest(@RequestParam Long courseId, @RequestParam Long userId) {
        return noteRequestService.createNoteRequest(courseId, userId);
    }

    @PutMapping("/update-status/{requestId}")
    public NoteRequest updateNoteRequestStatus(@PathVariable Long requestId, @RequestParam Boolean status) {
        return noteRequestService.updateRequestStatus(requestId, status);
    }

    @GetMapping("/pending")
    public List<NoteRequest> getPendingRequests() {
        return noteRequestService.getPendingRequests();
    }

    @GetMapping("/user/{usrId}")
    public List<NoteRequest> getRequestsByUser(@PathVariable Long userId) {
        return noteRequestService.getRequestsByUser(userId);
    }
}
