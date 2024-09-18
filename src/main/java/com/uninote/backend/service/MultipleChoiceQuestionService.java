package com.uninote.backend.service;

import com.uninote.backend.dto.ChoiceDTO;
import com.uninote.backend.dto.MultipleChoiceQuestionDTO;
import com.uninote.backend.entity.MultipleChoiceQuestion;
import com.uninote.backend.entity.Question;
import com.uninote.backend.repository.MultipleChoiceQuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MultipleChoiceQuestionService {

    @Autowired
    private MultipleChoiceQuestionRepository multipleChoiceQuestionRepository;

    public List<MultipleChoiceQuestion> getAllMultipleChoiceQuestions() {
        return multipleChoiceQuestionRepository.findAll();
    }

    public Optional<MultipleChoiceQuestion> getMultipleChoiceQuestionById(Long id) {
        return multipleChoiceQuestionRepository.findById(id);
    }

    public List<MultipleChoiceQuestion> getMultipleChoiceQuestionsByQuestion(Question question) {
        return multipleChoiceQuestionRepository.findByQuestion(question);
    }

    @Transactional
    public MultipleChoiceQuestion saveMultipleChoiceQuestion(MultipleChoiceQuestion multipleChoiceQuestion) {
        return multipleChoiceQuestionRepository.save(multipleChoiceQuestion);
    }

    @Transactional
    public void deleteMultipleChoiceQuestion(Long id) {
        multipleChoiceQuestionRepository.deleteById(id);
    }

    @Transactional
    public List<MultipleChoiceQuestion> getMultipleChoiceQuestionsByCourseId(Long courseId) {
        return multipleChoiceQuestionRepository.findByCourseId(courseId);
    }


    public List<MultipleChoiceQuestionDTO> findReportedMultipleChoiceQuestions() {
        List<Object[]> results = multipleChoiceQuestionRepository.findReportedMultipleChoiceQuestionsWithChoices();
        
        
       Map<Long, MultipleChoiceQuestionDTO> questionMap = new LinkedHashMap<>();

        for (Object[] result : results) {
            
            

            // Extract the question ID from the result
            Long questionId = ((BigDecimal) result[1]).longValue();

            // Fetch or create the MultipleChoiceQuestionDTO for the question ID
            MultipleChoiceQuestionDTO questionDTO = questionMap.computeIfAbsent(questionId, id -> {
                MultipleChoiceQuestionDTO dto = new MultipleChoiceQuestionDTO();
                dto.setId(id);
                dto.setQuestionId(((BigDecimal) result[1]).longValue());
                dto.setQuestionTypeId(((BigDecimal) result[2]).longValue());
                dto.setQuestionText((String) result[3]);
                dto.setIsDifficult(((BigDecimal) result[4]).intValue() == 1);
                dto.setImageUrl((String) result[9]);
                return dto;
            });

           

            
            Long choiceId = ((BigDecimal) result[6]).longValue();
            int choiceLabel = ((BigDecimal) result[8]).intValue();

            
            if (choiceId.equals(((BigDecimal) result[5]).longValue())) {
                questionDTO.setCorrectChoiceLabel(choiceLabel);
            }

            
            ChoiceDTO choiceDTO = new ChoiceDTO();
            choiceDTO.setId(choiceId);
            choiceDTO.setChoiceText((String) result[7]);
            choiceDTO.setChoiceLabel(choiceLabel);

           
            questionDTO.getChoices().add(choiceDTO);
        
        
    
        }
        return new ArrayList<>(questionMap.values());
    }
}