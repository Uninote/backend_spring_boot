package com.uninote.backend.controller;

import com.uninote.backend.converter.EntityToDTOConverter;
import com.uninote.backend.dto.TestResultDetailDTO;
import com.uninote.backend.entity.TestResultDetail;
import com.uninote.backend.service.TestResultDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/test-result-details")
public class TestResultDetailController {

     @Autowired
    private TestResultDetailService testResultDetailService;

    @PostMapping
    public ResponseEntity<TestResultDetailDTO> createTestResultDetail(@RequestBody TestResultDetailDTO testResultDetailDTO) {
        TestResultDetail createdTestResultDetail = testResultDetailService.saveTestResultDetail(testResultDetailDTO);
        return ResponseEntity.ok(EntityToDTOConverter.convertTestResultDetailToDto(createdTestResultDetail));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestResultDetailDTO> getTestResultDetailById(@PathVariable Long id) {
        TestResultDetail testResultDetail = testResultDetailService.getTestResultDetailById(id);
        return ResponseEntity.ok(EntityToDTOConverter.convertTestResultDetailToDto(testResultDetail));
    }

    @GetMapping
    public ResponseEntity<List<TestResultDetailDTO>> getAllTestResultDetails() {
        List<TestResultDetail> testResultDetails = testResultDetailService.getAllTestResultDetails();
        List<TestResultDetailDTO> testResultDetailDTOs = testResultDetails.stream().map(EntityToDTOConverter::convertTestResultDetailToDto).collect(Collectors.toList());
        return ResponseEntity.ok(testResultDetailDTOs);
    }
}

