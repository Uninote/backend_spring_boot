package com.uninote.backend.controller;

import com.uninote.backend.dto.DepartmentDTO;
import com.uninote.backend.dto.DepartmentNameDTO;
import com.uninote.backend.entity.Department;
import com.uninote.backend.entity.DepartmentName;
import com.uninote.backend.entity.DepartmentSimilarity;
import com.uninote.backend.interfaceProjection.DepartmentProjection;
import com.uninote.backend.repository.DepartmentNameRepository;
import com.uninote.backend.repository.DepartmentRepository;
import com.uninote.backend.repository.DepartmentSimilarityRepository;
import com.uninote.backend.service.DepartmentNameService;
import com.uninote.backend.service.DepartmentService;
import com.uninote.backend.service.SimilarityService;

import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;


@RestController
@RequestMapping("/departments")
public class DepartmentController {

    @Autowired
    private SimilarityService<Department> departmentSimilarityService;


    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DepartmentNameRepository departmentNameRepository;
    
    @Autowired
    private DepartmentNameService departmentNameService;

    
    

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentDTO> getDepartment(@PathVariable Long id) {
        DepartmentDTO departmentDTO = departmentService.getDepartment(id);
        return ResponseEntity.ok(departmentDTO);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Long> getDepartmentIdByName(@PathVariable String name) {
        Long departmentId = departmentService.getDepartmentIdByName(name);
        return ResponseEntity.ok(departmentId);
    }

    @GetMapping
    public ResponseEntity<List<DepartmentDTO>> getAllDepartments() {
        List<DepartmentDTO> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }

    @PostMapping
    public ResponseEntity<DepartmentDTO> createDepartment(@RequestBody DepartmentDTO departmentDTO) {
        DepartmentDTO createdDepartment = departmentService.createDepartment(departmentDTO);
        return ResponseEntity.ok(createdDepartment);
    }

    @PostMapping("/{departmentId}/names")
    public ResponseEntity<DepartmentNameDTO> addDepartmentName(@PathVariable Long departmentId, @RequestBody DepartmentNameDTO departmentNameDTO) {
        departmentNameDTO.setId(departmentId);
        DepartmentNameDTO createdDepartmentName = departmentNameService.addDepartmentName(departmentNameDTO);
        return ResponseEntity.ok(createdDepartmentName);

    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/details-uni-lang")
    public ResponseEntity<List<DepartmentProjection>> getDepartmentsByUniversityIdAndLanguage(
            @RequestParam String universityId,
            @RequestParam(defaultValue =  "EN") String language) {
        try {
            Long universityIdLong = Long.parseLong(universityId);
            List<DepartmentProjection> departmentDetails = departmentService.getDepartmentsByUniversityIdAndLanguage(universityIdLong, language);
            return ResponseEntity.ok(departmentDetails);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{departmentId}/names")
    public ResponseEntity<List<DepartmentNameDTO>> getDepartmentNames(@PathVariable Long departmentId) {
        List<DepartmentNameDTO> departmentNames = departmentService.getDepartmentNames(departmentId);
        return ResponseEntity.ok(departmentNames);
    }


    @GetMapping("/with-questions")
    public List<DepartmentProjection> getDepartmentsWithQuestions(@RequestParam("universityId") Long universityId, @RequestParam("language") String language) {
        return departmentService.getDepartmentsWithQuestionsByUniversityAndLanguage(universityId, language);
    }

    @GetMapping("/{departmentId}/semesters-with-questions")
    public List<String> getSemestersWithQuestions(@PathVariable("departmentId") Long departmentId) {
        return departmentService.getSemestersWithQuestionsByDepartment(departmentId);
    }

    @GetMapping("/{id}/similar")
    public ResponseEntity<List<Map<String, Object>>> getSimilarDepartments(
        @PathVariable Long id,
        @RequestParam(defaultValue = "2") Long languageId
    ) {
        List<Map<String, Object>> result = departmentService.getSimilarDepartments(id, languageId);
        return ResponseEntity.ok(result);
    }




    

}