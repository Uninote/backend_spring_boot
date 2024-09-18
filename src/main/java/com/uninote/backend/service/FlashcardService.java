package com.uninote.backend.service;

import com.uninote.backend.dto.FlashcardDTO;
import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.Flashcard;
import com.uninote.backend.repository.FlashcardRepository;
import com.uninote.backend.repository.QuestionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class FlashcardService {

    @Autowired
    private FlashcardRepository flashcardRepository;

    @Autowired
    private QuestionRepository questionRepository;

    

    public List<Flashcard> getFlashcardsByCourse(Course course) {

        return flashcardRepository.findByQuestionCourse(course);
    }


    public List<FlashcardDTO> findReportedFlashcards() {
        List<Object[]> results = questionRepository.findReportedFlashcards();
        List<FlashcardDTO> reportedQuestions = new ArrayList<>();

        for (Object[] row : results) {
            Long questionId = ((BigDecimal) row[0]).longValue();
            Long courseId = ((BigDecimal) row[1]).longValue();
            String questionText = (String) row[3];
            Boolean isDifficult = ((BigDecimal) row[4]).intValue() == 1;
            String answer = (String) row[5];  

            
            FlashcardDTO dto = new FlashcardDTO();
            dto.setId(questionId);
            dto.setCourseId(courseId);
            dto.setQuestionText(questionText);
            dto.setIsDifficult(isDifficult);
            dto.setAnswer(answer);  

            reportedQuestions.add(dto);
        }

        return reportedQuestions;
    }
}
