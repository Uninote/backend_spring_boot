package com.uninote.backend.service;

import com.uninote.backend.converter.EntityToDTOConverter;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private FlashcardRepository flashcardRepository;

    @Autowired
    private TrueFalseQuestionRepository trueFalseQuestionRepository;

    @Autowired
    private QuestionTypeRepository questionTypeRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TrueFalseQuestionRepository tfqRepository;

    public List<QuestionDTO> getQuestionsByCourseId(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course Not found"));
        List<Question> questions = questionRepository.findByCourse(course);
        return questions.stream().map(EntityToDTOConverter::convertQuestionToDTO).collect(Collectors.toList());
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

    @Transactional
    public List<FlashcardDTO> getFlashcardsByCourseId(Long courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new IllegalArgumentException("Course not found"));
        List<Flashcard> flashcards = flashcardRepository.findByQuestionCourse(course);
        return flashcards.stream().map(EntityToDTOConverter::convertFlashcardToDTO).collect(Collectors.toList());
    }

    @Transactional
    public List<TrueFalseQuestionDTO> getTrueFalseQuestionsByCourseId(Long courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new IllegalArgumentException("Course not found"));
        List<TrueFalseQuestion> trueFalseQuestions = tfqRepository.findByQuestionCourse(course);
        return trueFalseQuestions.stream().map(EntityToDTOConverter::convertTrueFalseQuestionToDTO).collect(Collectors.toList());
    }

    public List<QuestionDTO> getRandomQuestionsByCourse(Long courseId, int count) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new IllegalArgumentException("Course not Found"));
        List<Question> questions = questionRepository.findByCourse(course);
        Collections.shuffle(questions); 
        return questions.stream().limit(count).map(EntityToDTOConverter::convertQuestionToDTO).collect(Collectors.toList());
    }

    public List<FlashcardDTO> getRandomFlashcardsByCourse(Long courseId, int count) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new IllegalArgumentException("Course not Found"));
        List<Flashcard> flashcards = flashcardRepository.findByCourse(course);
        Collections.shuffle(flashcards); 
        return flashcards.stream().limit(count).map(EntityToDTOConverter::convertFlashcardToDTO).collect(Collectors.toList());
    }

    public List<TrueFalseQuestionDTO> getRandomTrueFalseQuestionsByCourse(Long courseId, int count) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new IllegalArgumentException("Course not Found"));
        List<TrueFalseQuestion> trueFalseQuestions = trueFalseQuestionRepository.findByCourse(course);
        Collections.shuffle(trueFalseQuestions); 
        return trueFalseQuestions.stream().limit(count).map(EntityToDTOConverter::convertTrueFalseQuestionToDTO).collect(Collectors.toList());
    }
}