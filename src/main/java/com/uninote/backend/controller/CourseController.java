package com.uninote.backend.controller;

import com.uninote.backend.dto.CourseDTO;
import com.uninote.backend.dto.CourseNameDTO;
import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.CourseName;
import com.uninote.backend.entity.CourseSimilarity;
import com.uninote.backend.interfaceProjection.CourseProjection;
import com.uninote.backend.repository.CourseNameRepository;
import com.uninote.backend.repository.CourseRepository;
import com.uninote.backend.repository.CourseSimilarityRepository;
import com.uninote.backend.service.CourseService;
import com.uninote.backend.service.SimilarityService;

import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    @Autowired
    private CourseNameRepository courseNameRepository;

    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private CourseSimilarityRepository courseSimilarityRepository;

    @Autowired
    private SimilarityService<CourseName> similarityService;


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
    public ResponseEntity<List<CourseProjection>> getCoursesByDepartmentAndSemester(
            @PathVariable Long departmentId,
            @PathVariable int semesterId,
            @RequestParam String language) {    
        List<CourseProjection> courses = courseService.getCourseDetailsByDepartmentAndSemester(departmentId, semesterId, language);
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


    @GetMapping("/with-questions")
    public List<CourseProjection> getCoursesWithQuestions(
            @RequestParam("departmentId") Long departmentId, 
            @RequestParam("semester") int semester,
            @RequestParam("language") String language) {
        return courseService.getCoursesWithQuestionsByDepartmentSemesterAndLanguage(departmentId, semester, language);
    }
    
    @GetMapping("/{id}/similar")
    public ResponseEntity<List<Map<String, Object>>> getSimilarCourses(
        @PathVariable Long id,
        @RequestParam(defaultValue = "2") Long languageId
    ) {
        Course course = courseRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Course not found"));

        List<CourseSimilarity> similarities = courseSimilarityRepository
            .findByCourseA_IdOrderBySimilarityScoreDesc(id);

        List<Map<String, Object>> result = similarities.stream()
            .map(sim -> {
                Course courseB = sim.getCourseB();

                String name = "";
                courseNameRepository.findByCourseIdAndLanguageId(courseB.getId(), languageId)
                .map(CourseName::getName)
                .orElse("");

                Map<String, Object> map = new HashMap<>();
                map.put("id", courseB.getId());
                map.put("name", name);
                map.put("score", sim.getSimilarityScore());
                return map;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }



}
