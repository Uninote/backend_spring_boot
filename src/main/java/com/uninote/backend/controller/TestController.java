package com.uninote.backend.controller;

import com.uninote.backend.dto.TestDTO;
import com.uninote.backend.entity.Test;
import com.uninote.backend.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tests")
public class TestController {

    @Autowired
    private TestService testService;

    @PostMapping
    public ResponseEntity<TestDTO> createTest(@RequestBody TestDTO testDTO) {
        Test createdTest = testService.saveTest(testDTO);
        return ResponseEntity.ok(convertToDto(createdTest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestDTO> getTestById(@PathVariable Long id) {
        Test test = testService.getTestById(id);
        return ResponseEntity.ok(convertToDto(test));
    }

    @GetMapping
    public ResponseEntity<List<TestDTO>> getAllTests() {
        List<Test> tests = testService.getAllTests();
        List<TestDTO> testDTOs = tests.stream().map(this::convertToDto).collect(Collectors.toList());
        return ResponseEntity.ok(testDTOs);
    }

    private TestDTO convertToDto(Test test) {
        TestDTO testDTO = new TestDTO();
        testDTO.setId(test.getId());
        testDTO.setUserId(test.getUser().getId());
        testDTO.setCourseId(test.getCourse().getId());
        testDTO.setTestTypeId(test.getTestType().getId());
        testDTO.setDateTaken(test.getDateTaken());
        return testDTO;
    }
}
