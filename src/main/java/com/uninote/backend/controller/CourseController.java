package com.uninote.backend.controller;

import com.uninote.backend.dto.CourseDTO;
import com.uninote.backend.dto.CourseNameDTO;
import com.uninote.backend.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    @Autowired
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> getCourse(@PathVariable Long id) {
        CourseDTO courseDTO = courseService.getCourse(id);
        return ResponseEntity.ok(courseDTO);
    }

    @GetMapping
    public ResponseEntity<List<CourseDTO>> getAllCourses() {
        List<CourseDTO> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
    }

    @PostMapping
    public ResponseEntity<CourseDTO> createCourse(@RequestBody CourseDTO courseDTO) {
        CourseDTO createdCourse = courseService.createCourse(courseDTO);
        return ResponseEntity.ok(createdCourse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{departmentId}/{semesterId}")
    public ResponseEntity<List<Map<String, String>>> getCoursesByDepartmentAndSemester(
            @PathVariable Long departmentId,
            @PathVariable int semesterId,
            @RequestParam String language) {
        List<Map<String, String>> courses = courseService.getCourseDetailsByDepartmentAndSemester(departmentId, semesterId, language);
        if (courses.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(courses);
    }

    @PostMapping("/{id}/names")
    public ResponseEntity<CourseNameDTO> addCourseName(@PathVariable Long id, @RequestBody CourseNameDTO courseNameDTO) {
        courseNameDTO.setId(id);
        CourseNameDTO createdName = courseService.addCourseName(courseNameDTO);
        return ResponseEntity.ok(createdName);
    }

    
}
