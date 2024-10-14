package com.uninote.backend.repository;



import org.springframework.data.jpa.repository.JpaRepository;

import com.uninote.backend.entity.NoteRequest;

import java.util.List;

public interface NoteRequestRepository extends JpaRepository<NoteRequest, Long> {
    
    
    List<NoteRequest> findByUserId(Long usrId);
    
    List<NoteRequest> findByCourseId(Long courseId);
    
    List<NoteRequest> findByRequestStatus(Boolean requestStatus);
}
