package com.uninote.backend.service;

import com.uninote.backend.dto.TestDTO;
import com.uninote.backend.entity.Test;
import com.uninote.backend.repository.CourseRepository;
import com.uninote.backend.repository.TestRepository;
import com.uninote.backend.repository.TestTypeRepository;
import com.uninote.backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestService {

    @Autowired
    private TestRepository testRepository;

     @Autowired
    private CourseRepository courseRepository;


    @Autowired
    private TestTypeRepository testTypeRepository;

    @Autowired
    private UserRepository userRepository;

    public Test saveTest(TestDTO testDTO) {
        Test test = new Test();
        test.setUser(userRepository.findById(testDTO.getUserId()).orElse(null));
        test.setCourse(courseRepository.findById(testDTO.getCourseId()).orElse(null));
        test.setTestType(testTypeRepository.findById(testDTO.getTestTypeId()).orElse(null));
        test.setDateTaken(testDTO.getDateTaken());
        return testRepository.save(test);
    }

    public Test getTestById(Long id) {
        return testRepository.findById(id).orElse(null);
    }

    public List<Test> getAllTests() {
        return testRepository.findAll();
    }
}
