package com.uninote.backend.converter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;



import com.uninote.backend.dto.BadgeDTO;
import com.uninote.backend.dto.ChoiceDTO;
import com.uninote.backend.dto.CommentDTO;
import com.uninote.backend.dto.CourseDTO;
import com.uninote.backend.dto.CourseNameDTO;
import com.uninote.backend.dto.DepartmentDTO;
import com.uninote.backend.dto.DepartmentNameDTO;
import com.uninote.backend.dto.FlashcardDTO;
import com.uninote.backend.dto.InviteDTO;
import com.uninote.backend.dto.MultipleChoiceQuestionDTO;
import com.uninote.backend.dto.NoteCollectionDTO;
import com.uninote.backend.dto.NoteDTO;
import com.uninote.backend.dto.QuestionDTO;
import com.uninote.backend.dto.TestDTO;
import com.uninote.backend.dto.TestResultDTO;
import com.uninote.backend.dto.TestResultDetailDTO;
import com.uninote.backend.dto.TrueFalseQuestionDTO;
import com.uninote.backend.dto.UniversityDTO;
import com.uninote.backend.dto.UniversityNameDTO;
import com.uninote.backend.dto.UserDTO;
import com.uninote.backend.entity.Badge;
import com.uninote.backend.entity.Choice;
import com.uninote.backend.entity.Comment;
import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.CourseName;
import com.uninote.backend.entity.Department;
import com.uninote.backend.entity.DepartmentName;
import com.uninote.backend.entity.Flashcard;
import com.uninote.backend.entity.Invite;
import com.uninote.backend.entity.MultipleChoiceQuestion;
import com.uninote.backend.entity.Note;
import com.uninote.backend.entity.NoteCollection;
import com.uninote.backend.entity.Question;
import com.uninote.backend.entity.Test;
import com.uninote.backend.entity.TestResult;
import com.uninote.backend.entity.TestResultDetail;
import com.uninote.backend.entity.TrueFalseQuestion;
import com.uninote.backend.entity.University;
import com.uninote.backend.entity.UniversityName;
import com.uninote.backend.entity.User;
import com.uninote.backend.entity.UserBadge;
import com.uninote.backend.service.CollectionLikeService;
import com.uninote.backend.service.QuestionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
public class EntityToDTOConverter {

    @Autowired
    private  CollectionLikeService collectionLikeService ;


     private static final Logger logger = LoggerFactory.getLogger(EntityToDTOConverter.class);
    public static DepartmentDTO convertDepartmentToDTO(Department department){
        return new DepartmentDTO(
            department.getId(),
            department.getUniversity().getId(),
            department.getCode(),
            department.getmSemesters(),
            department.getCourses().stream()
                      .map(Course::getId)
                      .collect(Collectors.toSet()),
            department.getDepartmentNames().stream()
                      .map(EntityToDTOConverter::convertDepartmentNameToDTO)
                      .collect(Collectors.toSet())        );
    }

    public static UniversityDTO convertUniversityToDTO(University university) {
        return new UniversityDTO(
            university.getId(),
            university.getLocation(),
            university.getUniversityNames().stream()
                      .map(EntityToDTOConverter::convertUniversityNameToDTO)
                      .collect(Collectors.toSet()),
            university.getDepartments().stream().map(Department::getId).collect(Collectors.toSet())
        );
    }

    public static UniversityNameDTO convertUniversityNameToDTO(UniversityName universityName) {
        return new UniversityNameDTO(
            universityName.getUniversity().getId(),
            universityName.getLanguage().getId(),
            universityName.getName(),
            universityName.getFullName()
        );
    }
    public static DepartmentNameDTO convertDepartmentNameToDTO(DepartmentName departmentName) {
        return new DepartmentNameDTO(
            departmentName.getDepartment().getId(),
            departmentName.getName(),
            departmentName.getLanguage().getCode(),
            departmentName.getFullName()
        );
    }
    public static CourseDTO convertCourseToDTO(Course course) {
        return new CourseDTO(
            course.getId(),
            course.getDepartment().getId(),
            course.getCode(),
            course.getSemester(),
            course.getCourseNames().stream()
                  .map(EntityToDTOConverter::convertCourseNameToDTO)
                  .collect(Collectors.toSet())
        );
    }

    private static CourseNameDTO convertCourseNameToDTO(CourseName courseName) {
        return new CourseNameDTO(
            courseName.getCourse().getId(),
            courseName.getLanguage().getCode(),
            courseName.getName()
        );
    }  
    public static UserDTO convertUserToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setFirebaseUid(user.getFirebaseUid());
        userDTO.setName(user.getName());
        userDTO.setDepartmentId(user.getDepartment() != null ? user.getDepartment().getId() : null);
        userDTO.setUniversityId(user.getUniversity() != null ? user.getUniversity().getId() : null);
        userDTO.setUniscore(user.getUniscore());
        userDTO.setEmail(user.getEmail());
        userDTO.setUsername(user.getUsername());
        userDTO.setProfileImageUrl(user.getProfileImageUrl());
        userDTO.setRoleId(user.getRole() != null ? user.getRole().getId() : null);
        userDTO.setBio(user.getBio());
        userDTO.setRank(user.getRank() != null ? user.getRank().getRankName() : null);
        userDTO.setStreak(user.getStreak());
        return userDTO;
    }

    public static QuestionDTO convertQuestionToDTO(Question question) {
        QuestionDTO questionDTO = new QuestionDTO();
        questionDTO.setId(question.getId());
        questionDTO.setCourseId(question.getCourse().getId());
        questionDTO.setQuestionTypeId(question.getQuestionType().getId());
        questionDTO.setQuestionText(question.getQuestionText());
        questionDTO.setIsDifficult(question.getIsDifficult());
        return questionDTO;
    }

    public static BadgeDTO convertBadgeToBadgeDTO(Badge badge) {
        BadgeDTO dto = new BadgeDTO();
        dto.setId(badge.getId());
        dto.setName(badge.getName());
        dto.setDescription(badge.getDescription());
        dto.setImageUrl(badge.getImageUrl());
        dto.setRequirement(badge.getRequirement());
        dto.setTypeName(badge.getType().getName());
        return dto;
    }


    public static FlashcardDTO convertFlashcardToDTO(Flashcard flashcard) {
        FlashcardDTO flashcardDTO = new FlashcardDTO();
        flashcardDTO.setId(flashcard.getQuestion().getId());
        flashcardDTO.setCourseId(flashcard.getQuestion().getCourse().getId());
        flashcardDTO.setQuestionTypeId(flashcard.getQuestion().getQuestionType().getId());
        flashcardDTO.setQuestionText(flashcard.getQuestion().getQuestionText());
        flashcardDTO.setIsDifficult(flashcard.getQuestion().getIsDifficult());
        flashcardDTO.setAnswer(flashcard.getAnswer());
        return flashcardDTO;
    }

    public static TrueFalseQuestionDTO convertTrueFalseQuestionToDTO(TrueFalseQuestion trueFalseQuestion) {
        TrueFalseQuestionDTO trueFalseQuestionDTO = new TrueFalseQuestionDTO();
        trueFalseQuestionDTO.setId(trueFalseQuestion.getQuestion().getId());
        trueFalseQuestionDTO.setCourseId(trueFalseQuestion.getQuestion().getCourse().getId());
        trueFalseQuestionDTO.setQuestionTypeId(trueFalseQuestion.getQuestion().getQuestionType().getId());
        trueFalseQuestionDTO.setQuestionText(trueFalseQuestion.getQuestion().getQuestionText());
        trueFalseQuestionDTO.setIsDifficult(trueFalseQuestion.getQuestion().getIsDifficult());
        trueFalseQuestionDTO.setCorrectAnswer(trueFalseQuestion.getCorrectAnswer());
        return trueFalseQuestionDTO;
    }

    public static BadgeDTO convertUserBadgeToBadgeDTO(UserBadge userBadge) {
        Badge badge = userBadge.getBadge();
        BadgeDTO dto = new BadgeDTO();
        dto.setId(badge.getId());
        dto.setName(badge.getName());
        dto.setDescription(badge.getDescription());
        dto.setImageUrl(badge.getImageUrl());
        dto.setTypeName(badge.getType().getName());
        return dto;
    }

    public static CommentDTO convertCommentToDTO(Comment comment) {
        return  new CommentDTO(
            comment.getCommentId(),
            comment.getNote().getId(),
            comment.getUser().getId(),
            comment.getContent(),
            comment.getCreatedAt()
        );
        
    }

    public static InviteDTO convertInviteToDTO(Invite invite) {
        return new InviteDTO(
                invite.getId(),
                invite.getUser().getId(),
                invite.getInvitee() != null ? invite.getInvitee().getId() : null,
                invite.getDateOfInvite(),
                invite.getDateOfSignUp()
        );
    }

    public static TestResultDTO convertTestResultToDto(TestResult testResult) {
        TestResultDTO testResultDTO = new TestResultDTO();
        testResultDTO.setId(testResult.getId());
        testResultDTO.setTestId(testResult.getTest().getId());
        testResultDTO.setScore(testResult.getScore());
        return testResultDTO;
    }

    public static TestDTO convertTestToDto(Test test) {
        TestDTO testDTO = new TestDTO();
        testDTO.setId(test.getId());
        testDTO.setUserId(test.getUser().getId());
        testDTO.setCourseId(test.getCourse().getId());
        testDTO.setTestTypeId(test.getTestType().getId());
        testDTO.setDateTaken(test.getDateTaken());
        return testDTO;
    }

    public static TestResultDetailDTO convertTestResultDetailToDto(TestResultDetail testResultDetail) {
        TestResultDetailDTO testResultDetailDTO = new TestResultDetailDTO();
        testResultDetailDTO.setId(testResultDetail.getId());
        testResultDetailDTO.setTestResultId(testResultDetail.getTestResult().getId());
        testResultDetailDTO.setQuestionId(testResultDetail.getQuestion().getId());
        testResultDetailDTO.setCorrect(testResultDetail.isCorrect());
        return testResultDetailDTO;
    }

    public static NoteDTO convertNoteToDTO(Note note) {
        NoteDTO dto = new NoteDTO(
            note.getId(),
            note.getCourse().getId(),
            note.getUser().getId(),
            note.getTitle(),
            note.getDescription(),
            note.getPdfUrl(),
            note.getFilename(),
            note.getIsPublic()
        );
        String englishCourseName = note.getCourse().getCourseNames().stream()
            .filter(courseName -> "EN".equals(courseName.getLanguage().getCode()))
            .map(CourseName::getName)
            .findFirst()
            .orElse("Unknown Course Name");  

        dto.setCourseName(englishCourseName);   
        String englishDepartmentName = note.getCourse().getDepartment().getDepartmentNames().stream()
            .filter(departmentName -> "EN".equals(departmentName.getLanguage().getCode()))
            .map(DepartmentName::getName)
            .findFirst()
            .orElse("Unknown Department Name"); 
        dto.setDepartmentName(englishDepartmentName);     

        String englishUniversityName = note.getCourse().getDepartment().getUniversity().getUniversityNames().stream()
            .filter(universityName -> "EN".equals(universityName.getLanguage().getCode()))
            .map(UniversityName::getName)
            .findFirst()
            .orElse("Unknown University Name");  
        dto.setUniversityName(englishUniversityName);
        return dto;
    }

    public static MultipleChoiceQuestionDTO convertToMultipleChoiceQuestionDTO(MultipleChoiceQuestion multipleChoiceQuestion) {
        MultipleChoiceQuestionDTO multipleChoiceQuestionDTO = new MultipleChoiceQuestionDTO();
        multipleChoiceQuestionDTO.setId(multipleChoiceQuestion.getId());
        multipleChoiceQuestionDTO.setQuestionId(multipleChoiceQuestion.getQuestion().getId());
        multipleChoiceQuestionDTO.setCorrectChoiceLabel(multipleChoiceQuestion.getCorrectChoice().getChoiceLabel());
        multipleChoiceQuestionDTO.setQuestionText(multipleChoiceQuestion.getQuestion().getQuestionText());
        logger.debug("{}",multipleChoiceQuestionDTO.getQuestionText());
        List<ChoiceDTO> choiceDTOs = multipleChoiceQuestion.getChoices().stream()
                .map(EntityToDTOConverter::convertToChoiceDTO)
                .collect(Collectors.toList());
        multipleChoiceQuestionDTO.setChoices(choiceDTOs);

        return multipleChoiceQuestionDTO;
    }

    public static ChoiceDTO convertToChoiceDTO(Choice choice) {
        ChoiceDTO choiceDTO = new ChoiceDTO();
        choiceDTO.setId(choice.getId());
        choiceDTO.setChoiceText(choice.getChoiceText());
        choiceDTO.setChoiceLabel(choice.getChoiceLabel());
        return choiceDTO;
    }

    public  static NoteCollectionDTO convertCollectionToDTO(NoteCollection noteCollection) {
        NoteCollectionDTO dto = new NoteCollectionDTO();

        if (noteCollection == null) {
            throw new IllegalArgumentException("NoteCollection cannot be null");
        }

        dto.setCollectionId(noteCollection.getCollectionId());

        if (noteCollection.getAdmin() != null) {
            dto.setAdminId(noteCollection.getAdmin().getId());
        }

        dto.setTitle(noteCollection.getName());
        dto.setDescription(noteCollection.getDescription());
        dto.setIsPublic(noteCollection.getIsPublic());

       

        return dto;
    }
}
