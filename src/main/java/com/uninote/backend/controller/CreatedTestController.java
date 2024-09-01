package com.uninote.backend.controller;




import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.uninote.backend.entity.CreatedTest;
import com.uninote.backend.repository.CreatedTestRepository;

@RestController
@RequestMapping("/tests")
public class CreatedTestController {

    @Autowired
    private CreatedTestRepository createdTestRepository;

    @PostMapping("/create")
    public ResponseEntity<CreatedTest> createTest(@RequestBody CreatedTest createdTest) {
        CreatedTest savedTest = createdTestRepository.save(createdTest);
        return new ResponseEntity<>(savedTest, HttpStatus.CREATED);
    }
}
