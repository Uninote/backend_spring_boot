package com.uninote.backend.controller;

import com.uninote.backend.dto.DepartmentDTO;
import com.uninote.backend.dto.DepartmentNameDTO;
import com.uninote.backend.interfaceProjection.DepartmentProjection;
import com.uninote.backend.service.DepartmentNameService;
import com.uninote.backend.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/departments")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

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
            @RequestParam String language) {
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
    
    }
