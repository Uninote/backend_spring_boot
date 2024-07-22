package com.uninote.backend.service;

import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.TrueFalseQuestion;
import com.uninote.backend.repository.TrueFalseQuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrueFalseQuestionService {

    @Autowired
    private TrueFalseQuestionRepository trueFalseQuestionRepository;

    public List<TrueFalseQuestion> getTrueFalseQuestionsByCourse(Course course) {

        return trueFalseQuestionRepository.findByQuestionCourse(course);
    }
}
