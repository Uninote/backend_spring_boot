package com.uninote.backend.converter;

import java.util.stream.Collectors;

import com.uninote.backend.dto.BadgeDTO;
import com.uninote.backend.dto.CommentDTO;
import com.uninote.backend.dto.CourseDTO;
import com.uninote.backend.dto.CourseNameDTO;
import com.uninote.backend.dto.DepartmentDTO;
import com.uninote.backend.dto.DepartmentNameDTO;
import com.uninote.backend.dto.FlashcardDTO;
import com.uninote.backend.dto.QuestionDTO;
import com.uninote.backend.dto.TrueFalseQuestionDTO;
import com.uninote.backend.dto.UserDTO;
import com.uninote.backend.entity.Badge;
import com.uninote.backend.entity.Comment;
import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.CourseName;
import com.uninote.backend.entity.Department;
import com.uninote.backend.entity.DepartmentName;
import com.uninote.backend.entity.Flashcard;
import com.uninote.backend.entity.Question;
import com.uninote.backend.entity.TrueFalseQuestion;
import com.uninote.backend.entity.User;
import com.uninote.backend.entity.UserBadge;

public class EntityToDTOConverter {
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
    private static DepartmentNameDTO convertDepartmentNameToDTO(DepartmentName departmentName) {
        return new DepartmentNameDTO(
            departmentName.getDepartment().getId(),
            departmentName.getName(),
            departmentName.getLanguage().getCode()
        );
    }  
    public static CourseDTO convertCourseToDTO(Course course) {
        return new CourseDTO(
            course.getId(),
            course.getDepartment().getId(),
            course.getCode(),
            course.getmSemester(),
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
        userDTO.setDepartmentId(user.getDepartment().getId());
        userDTO.setUniversityId(user.getUniversity().getId());
        userDTO.setUniscore(user.getUniscore());
        userDTO.setEmail(user.getEmail());
        userDTO.setUsername(user.getUsername());
        userDTO.setProfileImageUrl(user.getProfileImageUrl());
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
        return new CommentDTO(
            comment.getCommentId(),
            comment.getNote().getId(),
            comment.getUser().getId(),
            comment.getContent(),
            comment.getCreatedAt()
        );
    }
}
