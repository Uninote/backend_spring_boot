package com.uninote.backend.service;

import com.uninote.backend.converter.EntityToDTOConverter;
import com.uninote.backend.dto.ChoiceDTO;
import com.uninote.backend.dto.FlashcardDTO;
import com.uninote.backend.dto.MultipleChoiceQuestionDTO;
import com.uninote.backend.dto.QuestionDTO;
import com.uninote.backend.dto.QuestionTypeDTO;
import com.uninote.backend.dto.TrueFalseQuestionDTO;
import com.uninote.backend.entity.Choice;
import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.Flashcard;
import com.uninote.backend.entity.MultipleChoiceQuestion;
import com.uninote.backend.entity.Question;
import com.uninote.backend.entity.QuestionType;
import com.uninote.backend.entity.TrueFalseQuestion;
import com.uninote.backend.interfaceProjection.FlashcardProjection;
import com.uninote.backend.interfaceProjection.TrueFalseQuestionProjection;
import com.uninote.backend.repository.ChoiceRepository;
import com.uninote.backend.repository.CourseRepository;
import com.uninote.backend.repository.FlashcardRepository;
import com.uninote.backend.repository.MultipleChoiceQuestionRepository;
import com.uninote.backend.repository.QuestionRepository;
import com.uninote.backend.repository.QuestionTypeRepository;
import com.uninote.backend.repository.TrueFalseQuestionRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

@Service
public class QuestionService {

    @PersistenceContext
    private EntityManager entityManager;

    


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
        if(trueFalseQuestionDTO.getImageUrl() != null) {
            logger.debug(trueFalseQuestionDTO.getImageUrl());
            trueFalseQuestion.setImageUrl(trueFalseQuestionDTO.getImageUrl());
        }
        return tfqRepository.save(trueFalseQuestion);
    }

    
    public MultipleChoiceQuestion createMultipleChoiceQuestion(MultipleChoiceQuestionDTO multipleChoiceQuestionDTO) {
   
        Question question = createQuestion(multipleChoiceQuestionDTO);
        MultipleChoiceQuestion multipleChoiceQuestion = new MultipleChoiceQuestion();
        multipleChoiceQuestion.setQuestion(question);
        if(multipleChoiceQuestionDTO.getImageUrl() !=null) {
            multipleChoiceQuestion.setImageUrl(multipleChoiceQuestionDTO.getImageUrl());
        }
        
        MultipleChoiceQuestion saved = multipleChoiceQuestionRepository.saveAndFlush(multipleChoiceQuestion);
        logger.info("MultipleChoiceQuestion saved with ID: {}", saved.getId());
        return saved;
    
    }   

    

    @Transactional
    public MultipleChoiceQuestion addChoicesMultipleChoice(MultipleChoiceQuestion multipleChoiceQuestion, List<ChoiceDTO> choiceDTOs) {
        logger.debug("mcq{}",multipleChoiceQuestion.getId());
        List<Choice> choices = choiceDTOs.stream()
        .map(choiceDTO -> {
            Choice choice = new Choice();
            choice.setChoiceText(choiceDTO.getChoiceText());
            choice.setChoiceLabel(choiceDTO.getChoiceLabel());
            choice.setMultipleChoiceQuestion(multipleChoiceQuestion);
            logger.debug("{}",multipleChoiceQuestion==choice.getMultipleChoiceQuestion());
            return choice;
        })
        .collect(Collectors.toList());
        
        List<Object[]> batchData = choices.stream()
            .map(choice -> new Object[]{choice.getChoiceLabel(), choice.getChoiceText(), choice.getMultipleChoiceQuestion().getId()})
            .collect(Collectors.toList());

            choices.forEach(choice -> {
                choiceRepository.batchInsertChoices(
                    choice.getChoiceLabel(),
                    choice.getChoiceText(),
                    choice.getMultipleChoiceQuestion().getId()
                );
            });
        


        multipleChoiceQuestion.setChoices(choices);
        
        return multipleChoiceQuestion;
    }



    @Transactional
    public MultipleChoiceQuestion setCorrectChoiceForMultipleChoiceQuestion(Long multipleChoiceQuestionId, int correctChoiceLabel) {
        MultipleChoiceQuestion multipleChoiceQuestion = multipleChoiceQuestionRepository.findById(multipleChoiceQuestionId)
                .orElseThrow(() -> new IllegalArgumentException("Multiple Choice Question not found"));
        logger.debug("label{}",correctChoiceLabel);
        logger.debug("id{}",multipleChoiceQuestion.getId() );
        Choice correctChoice = choiceRepository.findByMultipleChoiceQuestionAndChoiceLabel(multipleChoiceQuestion, correctChoiceLabel)
                .orElseThrow(() -> new IllegalArgumentException("Correct choice not found"));

        multipleChoiceQuestion.setCorrectChoice(correctChoice);
        return multipleChoiceQuestionRepository.save(multipleChoiceQuestion);
    }

    @Transactional
    public MultipleChoiceQuestion fullMultipleChoiceQuestionCreation(MultipleChoiceQuestionDTO multipleChoiceQuestionDTO) {
        multipleChoiceQuestionDTO.setQuestionTypeId(3L);
        MultipleChoiceQuestion createdMultipleChoiceQuestion = createMultipleChoiceQuestion(multipleChoiceQuestionDTO);
    
    // Step 2: Add choices to the multiple-choice question
        createdMultipleChoiceQuestion = addChoicesMultipleChoice(createdMultipleChoiceQuestion, multipleChoiceQuestionDTO.getChoices());

    // Step 3: Set the correct choice for the multiple-choice question
        createdMultipleChoiceQuestion = setCorrectChoiceForMultipleChoiceQuestion(
                createdMultipleChoiceQuestion.getId(), 
                multipleChoiceQuestionDTO.getCorrectChoiceLabel()
        );
        return createdMultipleChoiceQuestion;
    }

    @Transactional
    public List<QuestionDTO> getRandomQuestionsByCourse(Long courseId, int count) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new IllegalArgumentException("Course not Found"));
        List<Question> questions = questionRepository.findByCourse(course);
        Collections.shuffle(questions); 
        return questions.stream().limit(count).map(EntityToDTOConverter::convertQuestionToDTO).collect(Collectors.toList());
    }


    @Transactional
    public List<FlashcardProjection> getRandomFlashcardsByCourseId(Long courseId, int limit) {
        return flashcardRepository.findRandomFlashcardsByCourseId(courseId, limit);
    }


    @Transactional
    public List<TrueFalseQuestionProjection> getRandomTrueFalseQuestionsByCourseId(Long courseId, int limit) {
        return tfqRepository.findRandomTrueFalseQuestionsByCourseId(courseId, limit);
    }

    public List<MultipleChoiceQuestionDTO> getMultipleChoiceQuestionsByCourseId(Long courseId) {
        List<MultipleChoiceQuestion> multipleChoiceQuestions = multipleChoiceQuestionRepository.findByCourseId(courseId);
        return multipleChoiceQuestions.stream()
                .map(EntityToDTOConverter::convertToMultipleChoiceQuestionDTO)
                .collect(Collectors.toList());
    }


        @Transactional
        public List<MultipleChoiceQuestionDTO> getRandomMultipleChoiceQuestions(Long courseId, int limit) {
            List<Object[]> rawResults = multipleChoiceQuestionRepository.findRandomMultipleChoiceQuestionsWithChoicesByCourseId(courseId, limit);
    
    Map<Long, MultipleChoiceQuestionDTO> questionMap = new LinkedHashMap<>();

    for (Object[] result : rawResults) {
        logger.debug(""+result);
        Long questionId = ((BigDecimal) result[0]).longValue();

        // Fetch or create the MultipleChoiceQuestionDTO
        MultipleChoiceQuestionDTO questionDTO = questionMap.computeIfAbsent(questionId, id -> {
            MultipleChoiceQuestionDTO dto = new MultipleChoiceQuestionDTO();
            dto.setId(id);
            dto.setQuestionId(((BigDecimal) result[1]).longValue());
            dto.setQuestionTypeId(((BigDecimal) result[2]).longValue());
            dto.setQuestionText((String) result[3]);
            dto.setIsDifficult(((BigDecimal) result[4]).intValue() == 1);
            //dto.setCorrectChoiceLabel(((BigDecimal) result[5]).intValue());
            dto.setImageUrl((String) result[9]);
            return dto;
        });
        logger.debug("processing question " + questionId);
        Long choiceId = ((BigDecimal) result[6]).longValue();
        int choiceLabel = ((BigDecimal) result[8]).intValue();
        
        
        if (choiceId.equals(((BigDecimal) result[5]).longValue())) {
            questionDTO.setCorrectChoiceLabel(choiceLabel); 
        }
        ChoiceDTO choiceDTO = new ChoiceDTO();
        choiceDTO.setId(((BigDecimal) result[6]).longValue());
        choiceDTO.setChoiceText((String) result[7]);
        choiceDTO.setChoiceLabel(((BigDecimal) result[8]).intValue());

        questionDTO.getChoices().add(choiceDTO);
    }

        return new ArrayList<MultipleChoiceQuestionDTO>(questionMap.values());
        }


    public List<MultipleChoiceQuestionDTO> getRandomMultipleChoiceQuestionsByCourse(Long courseId, int count) {
        List<MultipleChoiceQuestion> multipleChoiceQuestions = multipleChoiceQuestionRepository.findByCourseId(courseId);
        return multipleChoiceQuestions.stream().limit(count).map(EntityToDTOConverter::convertToMultipleChoiceQuestionDTO).collect(Collectors.toList());
    }

    public List<QuestionTypeDTO> getDistinctQuestionTypesByCourseId(Long courseId) {
        List<Object[]> results = questionRepository.findDistinctQuestionTypeIdsAndNamesByCourseId(courseId);

        
        return results.stream()
                .map(result -> new QuestionTypeDTO((Long) result[0], (String) result[1]))
                .collect(Collectors.toList());
    }

    
}