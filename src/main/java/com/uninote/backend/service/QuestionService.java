package com.uninote.backend.service;

import com.uninote.backend.converter.EntityToDTOConverter;
import com.uninote.backend.dto.ChoiceDTO;
import com.uninote.backend.dto.FlashcardDTO;
import com.uninote.backend.dto.MultipleChoiceQuestionDTO;
import com.uninote.backend.dto.QuestionDTO;
import com.uninote.backend.dto.TrueFalseQuestionDTO;
import com.uninote.backend.entity.Choice;
import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.Flashcard;
import com.uninote.backend.entity.MultipleChoiceQuestion;
import com.uninote.backend.entity.Question;
import com.uninote.backend.entity.QuestionType;
import com.uninote.backend.entity.TrueFalseQuestion;
import com.uninote.backend.repository.ChoiceRepository;
import com.uninote.backend.repository.CourseRepository;
import com.uninote.backend.repository.FlashcardRepository;
import com.uninote.backend.repository.MultipleChoiceQuestionRepository;
import com.uninote.backend.repository.QuestionRepository;
import com.uninote.backend.repository.QuestionTypeRepository;
import com.uninote.backend.repository.TrueFalseQuestionRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private MultipleChoiceQuestionRepository multipleChoiceQuestionRepository;

    @Autowired
    private TrueFalseQuestionRepository tfqRepository;

    @Autowired
    private ChoiceRepository choiceRepository;

    private static final Logger logger = LoggerFactory.getLogger(QuestionService.class);

    public List<QuestionDTO> getQuestionsByCourseId(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course Not found"));
        List<Question> questions = questionRepository.findByCourse(course);
        return questions.stream().map(EntityToDTOConverter::convertQuestionToDTO).collect(Collectors.toList());
    }

    @Transactional
    public Question createQuestion(QuestionDTO questionDTO) {
        logger.info("Creating q with data: {}", questionDTO);

        Course course = courseRepository.findById(questionDTO.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + questionDTO.getCourseId()));
        Question question = new Question();

        if(questionDTO.getQuestionTypeId()!=null){
        QuestionType questionType = questionTypeRepository.findById(questionDTO.getQuestionTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Question type not found with ID: " + questionDTO.getQuestionTypeId()));
        question.setQuestionType(questionType);

        }
        logger.info("here");
        question.setCourse(course);
        question.setQuestionText(questionDTO.getQuestionText());
        question.setIsDifficult(questionDTO.getIsDifficult());
        logger.debug("created question{}",question.getId());
        Question saved = questionRepository.saveAndFlush(question);
        return saved;
    }

    @Transactional
    public Flashcard createFlashcard(FlashcardDTO flashcardDTO) {
        logger.info("Creating flashcard with data: {}", flashcardDTO);

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
    public MultipleChoiceQuestion createMultipleChoiceQuestion(MultipleChoiceQuestionDTO multipleChoiceQuestionDTO) {
        Question question = questionRepository.findById(multipleChoiceQuestionDTO.getQuestionId())
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        MultipleChoiceQuestion multipleChoiceQuestion = new MultipleChoiceQuestion();
        multipleChoiceQuestion.setQuestion(question);

        return multipleChoiceQuestionRepository.save(multipleChoiceQuestion);
    }

    @Transactional
    public MultipleChoiceQuestion addChoicesToMultipleChoiceQuestion(Long multipleChoiceQuestionId, List<ChoiceDTO> choicesDTO) {
        MultipleChoiceQuestion multipleChoiceQuestion = multipleChoiceQuestionRepository.findById(multipleChoiceQuestionId)
                .orElseThrow(() -> new IllegalArgumentException("Multiple Choice Question not found"));

        for (ChoiceDTO choiceDTO : choicesDTO) {
            Choice choice = new Choice();
            choice.setChoiceText(choiceDTO.getChoiceText());
            choice.setChoiceLabel(choiceDTO.getChoiceLabel());
            choice.setMultipleChoiceQuestion(multipleChoiceQuestion);
            choiceRepository.save(choice);
        }

        return multipleChoiceQuestion;
    }

    @Transactional
    public MultipleChoiceQuestion setCorrectChoiceForMultipleChoiceQuestion(Long multipleChoiceQuestionId, int correctChoiceLabel) {
        MultipleChoiceQuestion multipleChoiceQuestion = multipleChoiceQuestionRepository.findById(multipleChoiceQuestionId)
                .orElseThrow(() -> new IllegalArgumentException("Multiple Choice Question not found"));

        Choice correctChoice = choiceRepository.findByMultipleChoiceQuestionAndChoiceLabel(multipleChoiceQuestion, correctChoiceLabel)
                .orElseThrow(() -> new IllegalArgumentException("Correct choice not found"));

        multipleChoiceQuestion.setCorrectChoice(correctChoice);
        return multipleChoiceQuestionRepository.save(multipleChoiceQuestion);
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

    public List<MultipleChoiceQuestionDTO> getMultipleChoiceQuestionsByCourseId(Long courseId) {
        List<MultipleChoiceQuestion> multipleChoiceQuestions = multipleChoiceQuestionRepository.findByCourseId(courseId);
        return multipleChoiceQuestions.stream()
                .map(EntityToDTOConverter::convertToMultipleChoiceQuestionDTO)
                .collect(Collectors.toList());
    }


    public List<MultipleChoiceQuestionDTO> getRandomMultipleChoiceQuestionsByCourse(Long courseId, int count) {
        List<MultipleChoiceQuestion> multipleChoiceQuestions = multipleChoiceQuestionRepository.findByCourseId(courseId);
        return multipleChoiceQuestions.stream().map(EntityToDTOConverter::convertToMultipleChoiceQuestionDTO).collect(Collectors.toList());
    }
}