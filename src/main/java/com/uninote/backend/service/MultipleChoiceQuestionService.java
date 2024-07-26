package com.uninote.backend.service;

import com.uninote.backend.entity.MultipleChoiceQuestion;
import com.uninote.backend.entity.Question;
import com.uninote.backend.repository.MultipleChoiceQuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
}
