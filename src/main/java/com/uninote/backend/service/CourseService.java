package com.uninote.backend.service;

import com.uninote.backend.dto.CourseDTO;
import com.uninote.backend.dto.CourseNameDTO;
import com.uninote.backend.dto.DepartmentNameDTO;
import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.CourseName;
import com.uninote.backend.entity.CourseNameId;
import com.uninote.backend.entity.Department;
import com.uninote.backend.entity.Language;
import com.uninote.backend.interfaceProjection.CourseProjection;
import com.uninote.backend.repository.CourseNameRepository;
import com.uninote.backend.repository.CourseRepository;
import com.uninote.backend.repository.DepartmentRepository;
import com.uninote.backend.repository.LanguageRepository;
import com.uninote.backend.converter.EntityToDTOConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;
    @Autowired
    private CourseNameRepository courseNameRepository;
    @Autowired
    private LanguageRepository languageRepository;
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

    public CourseNameDTO addCourseName(CourseNameDTO nameDTO) {
        Course course = courseRepository.findById(nameDTO.getId()).orElseThrow(() -> new IllegalArgumentException(("Course note found")));
        Language language = languageRepository.findById(Long.parseLong(nameDTO.getLanguage()))
                .orElseThrow(() -> new IllegalArgumentException("Language not found"));
        CourseNameId id = new CourseNameId(nameDTO.getId(),Long.parseLong(nameDTO.getLanguage()));

        CourseName courseName = new CourseName();
        courseName.setId(id);
        courseName.setCourse(course);
        courseName.setName(nameDTO.getName());
            
        
        courseName.setLanguage(language);

        CourseName result = courseNameRepository.save(courseName);
        CourseNameDTO resultDTO = new CourseNameDTO();
        resultDTO.setId(result.getCourse().getId());
        resultDTO.setLanguage(language.getId().toString());
        resultDTO.setName(result.getName());
    
        return resultDTO;
    }

    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }

    public List<CourseProjection> getCourseDetailsByDepartmentAndSemester(Long departmentId, int semester, String language) {
        List<CourseProjection> courseProjections = courseRepository
            .findCoursesByDepartmentAndSemesterAndLanguage(departmentId, semester, language);
        return courseProjections;
    }

}
