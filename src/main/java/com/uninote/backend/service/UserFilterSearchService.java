package com.uninote.backend.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uninote.backend.entity.UserFilterSearch;
import com.uninote.backend.repository.UserFilterSearchRepository;

import java.time.LocalDateTime;

@Service
public class UserFilterSearchService {

    @Autowired
    private UserFilterSearchRepository repository;

    public void logSearch(Long userId, Long universityId, Long departmentId, Integer semester, Long courseId, Long sessionId) {
        UserFilterSearch search = new UserFilterSearch();
        search.setUserId(userId);
        search.setUniversityId(universityId);
        search.setDepartmentId(departmentId);
        search.setSemester(semester);
        search.setCourseId(courseId);
        search.setSearchedAt(LocalDateTime.now());
        search.setSessionId(sessionId);

        repository.save(search);
    }
}
