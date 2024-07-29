package com.uninote.backend.service;

import com.uninote.backend.dto.CourseDTO;
import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.CourseName;
import com.uninote.backend.entity.Department;
import com.uninote.backend.repository.CourseRepository;
import com.uninote.backend.repository.DepartmentRepository;
import com.uninote.backend.converter.EntityToDTOConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;

    @Autowired
    public CourseService(CourseRepository courseRepository, DepartmentRepository departmentRepository) {
        this.courseRepository = courseRepository;
        this.departmentRepository = departmentRepository;
    }

    public CourseDTO getCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        return EntityToDTOConverter.convertCourseToDTO(course);
    }

    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(EntityToDTOConverter::convertCourseToDTO)
                .collect(Collectors.toList());
    }

    public CourseDTO createCourse(CourseDTO courseDTO) {
        Course course = new Course();
        course.setDepartment(departmentRepository.findById(courseDTO.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Department not found")));
        course.setCode(courseDTO.getCode());
        course.setSemester(courseDTO.getSemester());

        Course savedCourse = courseRepository.save(course);
        return EntityToDTOConverter.convertCourseToDTO(savedCourse);
    }

    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }

    public List<Map<String, String>> getCourseDetailsByDepartmentAndSemester(Long departmentId, int semester, String language) {
        Department department = departmentRepository.findById(departmentId).orElseThrow(() -> new IllegalArgumentException(" Department not found"));
        List<Course> courses = courseRepository.findByDepartmentAndSemester(department, semester);
        return courses.stream().map(course -> {
            CourseName courseName = course.getCourseNames().stream()
                .filter(name -> name.getLanguage().getCode().equals(language))
                .findFirst()
                .orElse(null);
            String name = courseName != null ? courseName.getName() : "Name not found";
            return Map.of("id", course.getId().toString(), "name", name);
        }).collect(Collectors.toList());
    }

}
