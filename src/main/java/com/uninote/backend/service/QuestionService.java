package com.uninote.backend.service;

import com.uninote.backend.dto.FlashcardDTO;
import com.uninote.backend.dto.QuestionDTO;
import com.uninote.backend.dto.TrueFalseQuestionDTO;
import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.Flashcard;
import com.uninote.backend.entity.Question;
import com.uninote.backend.entity.QuestionType;
import com.uninote.backend.entity.TrueFalseQuestion;
import com.uninote.backend.repository.CourseRepository;
import com.uninote.backend.repository.FlashcardRepository;
import com.uninote.backend.repository.QuestionRepository;
import com.uninote.backend.repository.QuestionTypeRepository;
import com.uninote.backend.repository.TrueFalseQuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import javax.transaction.Transactional;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private FlashcardRepository flashcardRepository;

    @Autowired
    private QuestionTypeRepository questionTypeRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TrueFalseQuestionRepository tfqRepository;

    public List<Question> getQuestionsByCourseId(Long courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new IllegalArgumentException("Course Not found"));
        return questionRepository.findByCourse(course);
    }

    public List<Flashcard> getFlashcardsByCourseId(Long courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new IllegalArgumentException("Course Not found"));
        return flashcardRepository.findByQuestionCourse(course);
    }

    public List<TrueFalseQuestion> getTrueFalseQuestionsByCourseId(Long courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new IllegalArgumentException("Course Not found"));
        return tfqRepository.findByQuestionCourse(course);
    }

    @Transactional
    public Question createQuestion(QuestionDTO questionDTO) {
        Course course = courseRepository.findById(questionDTO.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + questionDTO.getCourseId()));

        QuestionType questionType = questionTypeRepository.findById(questionDTO.getQuestionTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Question type not found with ID: " + questionDTO.getQuestionTypeId()));

        Question question = new Question();
        question.setCourse(course);
        question.setQuestionType(questionType);
        question.setQuestionText(questionDTO.getQuestionText());
        question.setIsDifficult(questionDTO.getIsDifficult());
        return questionRepository.save(question);
    }

    @Transactional
    public Flashcard createFlashcard(FlashcardDTO flashcardDTO) {
        Question question = createQuestion(flashcardDTO);

        Flashcard flashcard = new Flashcard();
        flashcard.setQuestion(question);
        flashcard.setAnswer(flashcardDTO.getAnswer());

        return flashcardRepository.save(flashcard);
    }

    @Transactional
    public TrueFalseQuestion createTrueFalseQuestion(TrueFalseQuestionDTO trueFalseQuestionDTO) {
        Question question = createQuestion(trueFalseQuestionDTO);

        TrueFalseQuestion trueFalseQuestion = new TrueFalseQuestion();
        trueFalseQuestion.setQuestion(question);
        trueFalseQuestion.setCorrectAnswer(trueFalseQuestionDTO.getCorrectAnswer());

        return tfqRepository.save(trueFalseQuestion);
    }
}
