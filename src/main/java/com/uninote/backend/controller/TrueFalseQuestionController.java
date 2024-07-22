package com.uninote.backend.controller;

import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.TrueFalseQuestion;
import com.uninote.backend.repository.CourseRepository;
import com.uninote.backend.service.TrueFalseQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/true_false_questions")
public class TrueFalseQuestionController {

    @Autowired
    private TrueFalseQuestionService trueFalseQuestionService;

    @Autowired
    private CourseRepository courseRepository;

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<TrueFalseQuestion>> getTrueFalseQuestionsByCourseId(@PathVariable Long courseId) {
        Course course =  courseRepository.findById(courseId).orElseThrow(() -> new IllegalArgumentException("Course not found"));
        return ResponseEntity.ok(trueFalseQuestionService.getTrueFalseQuestionsByCourse(course));
    }
}
