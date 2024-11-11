package com.uninote.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uninote.backend.repository.NoteRepository;


@Service
public class PersonalizedRecommendationService {

    
    @Autowired
    private NoteRepository noteRepository;
}
