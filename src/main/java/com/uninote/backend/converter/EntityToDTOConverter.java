package com.uninote.backend.converter;

import java.util.stream.Collectors;

import com.uninote.backend.dto.CourseDTO;
import com.uninote.backend.dto.CourseNameDTO;
import com.uninote.backend.dto.DepartmentDTO;
import com.uninote.backend.dto.DepartmentNameDTO;
import com.uninote.backend.dto.UserDTO;
import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.CourseName;
import com.uninote.backend.entity.Department;
import com.uninote.backend.entity.DepartmentName;
import com.uninote.backend.entity.User;

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
        userDTO.setEmail(user.getEmail());
        userDTO.setUsername(user.getUsername());
        userDTO.setProfileImageUrl(user.getProfileImageUrl());
        return userDTO;
    }
}
