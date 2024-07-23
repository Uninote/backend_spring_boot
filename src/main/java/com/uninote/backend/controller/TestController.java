package com.uninote.backend.controller;

import com.uninote.backend.entity.Test;
import com.uninote.backend.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tests")
public class TestController {

    @Autowired
    private TestService testService;

    @PostMapping
    public ResponseEntity<Test> createTest(@RequestBody Test test) {
        Test createdTest = testService.saveTest(test);
        return ResponseEntity.ok(createdTest);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Test> getTestById(@PathVariable Long id) {
        Test test = testService.getTestById(id);
        return ResponseEntity.ok(test);
    }

    @GetMapping
    public ResponseEntity<List<Test>> getAllTests() {
        List<Test> tests = testService.getAllTests();
        return ResponseEntity.ok(tests);
    }
}
