package com.uninote.backend.service;

import com.uninote.backend.dto.TrueFalseQuestionDTO;
import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.TrueFalseQuestion;
import com.uninote.backend.repository.QuestionRepository;
import com.uninote.backend.repository.TrueFalseQuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class TrueFalseQuestionService {

    @Autowired
    private TrueFalseQuestionRepository trueFalseQuestionRepository;

    @Autowired
    private QuestionRepository questionRepository;

    public List<TrueFalseQuestion> getTrueFalseQuestionsByCourse(Course course) {

        return trueFalseQuestionRepository.findByQuestionCourse(course);
    }


    public List<TrueFalseQuestionDTO> findReportedTrueFalseQuestions() {
        List<Object[]> results = questionRepository.findReportedTrueFalseQuestions();
        List<TrueFalseQuestionDTO> reportedQuestions = new ArrayList<>();

        for (Object[] row : results) {
            Long questionId = ((BigDecimal) row[0]).longValue();
            Long courseId = ((BigDecimal) row[1]).longValue();
            String questionText = (String) row[3];
            Boolean isDifficult = ((BigDecimal) row[4]).intValue() == 1;
            Boolean correctAnswer = ((BigDecimal) row[5]).intValue() == 1;  
            
            TrueFalseQuestionDTO dto = new TrueFalseQuestionDTO();
            dto.setId(questionId);
            dto.setCourseId(courseId);
            dto.setQuestionText(questionText);
            dto.setIsDifficult(isDifficult);
            dto.setCorrectAnswer(correctAnswer);  

            reportedQuestions.add(dto);
        }

        return reportedQuestions;
    }
}
