package com.uninote.backend.service;

import com.uninote.backend.entity.Choice;
import com.uninote.backend.entity.MultipleChoiceQuestion;
import com.uninote.backend.repository.ChoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ChoiceService {

    @Autowired
    private ChoiceRepository choiceRepository;

    public List<Choice> getAllChoices() {
        return choiceRepository.findAll();
    }

    public Optional<Choice> getChoiceById(Long id) {
        return choiceRepository.findById(id);
    }

    public List<Choice> getChoicesByMultipleChoiceQuestion(MultipleChoiceQuestion multipleChoiceQuestion) {
        return choiceRepository.findByMultipleChoiceQuestion(multipleChoiceQuestion);
    }

    @Transactional
    public Choice saveChoice(Choice choice) {
        return choiceRepository.save(choice);
    }

    @Transactional
    public void deleteChoice(Long id) {
        choiceRepository.deleteById(id);
    }

    @Transactional
    public List<Choice> getChoicesByCourseId(Long courseId) {
        return choiceRepository.findByCourseId(courseId);
    }
}
