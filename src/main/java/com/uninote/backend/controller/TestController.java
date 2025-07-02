package com.uninote.backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.uninote.backend.dto.TestDTO;
import com.uninote.backend.entity.Test;
import com.uninote.backend.service.ContentExtractionService;
import com.uninote.backend.service.LangChainContentService;
import com.uninote.backend.service.TestService;

@RestController
@RequestMapping("/tests")
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private TestService testService;

    @Autowired
    private LangChainContentService langChainContentService;

    @Autowired
    private ContentExtractionService contentExtractionService;

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

    @PostMapping("/summary")
    public ResponseEntity<?> generateSummaryFromFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title) {
        try {
            if (file == null || file.isEmpty()) {
                logger.warn("[TestEndpoint] No file uploaded or file is empty");
                return ResponseEntity.badRequest().body("File is required");
            }
            logger.debug("[TestEndpoint] Received file: {} ({} bytes)", file.getOriginalFilename(), file.getSize());
            String content = contentExtractionService.extractContentFromFile(file);
            logger.debug("[TestEndpoint] Extracted content from file: {} ({} chars)", file.getOriginalFilename(), content.length());
            if (title == null || title.isEmpty()) {
                title = file.getOriginalFilename();
            }
            logger.debug("[TestEndpoint] Using title: {}", title);
            String summary = langChainContentService.generateSummaryFromContent(content, title);
            logger.debug("[TestEndpoint] Generated summary ({} chars) for file: {}", summary.length(), file.getOriginalFilename());
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            logger.error("[TestEndpoint] Failed to generate summary: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to generate summary: " + e.getMessage());
        }
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
