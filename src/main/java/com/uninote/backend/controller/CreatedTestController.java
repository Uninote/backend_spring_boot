package com.uninote.backend.controller;





import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.uninote.backend.entity.CreatedTest;
import com.uninote.backend.repository.CreatedTestRepository;
import com.uninote.backend.service.CreatedTestService;

@RestController
@RequestMapping("/tests")
public class CreatedTestController {

    @Autowired
    private CreatedTestRepository createdTestRepository;
    
    @Autowired
    private CreatedTestService createdTestService;

    @PostMapping("/create")
    public ResponseEntity<CreatedTest> createTest(@RequestBody CreatedTest createdTest) {
        CreatedTest savedTest = createdTestRepository.save(createdTest);
        return new ResponseEntity<>(savedTest, HttpStatus.CREATED);
    }

     @GetMapping("/count")
    public ResponseEntity<Long> getTotalTestsCount() {
        Long count = createdTestService.getTotalTestsCount();
        return ResponseEntity.ok(count);
    }

    // Endpoint to get the number of tests created by a specific user
    @GetMapping("/count/user/{userId}")
    public ResponseEntity<Long> getTestsCountByUser(@PathVariable Long userId) {
        Long count = createdTestService.getTestsCountByUser(userId);
        return ResponseEntity.ok(count);
    }

    // Endpoint to get the number of tests by type (corrected parameter type to Long)
    @GetMapping("/count/type/{testTypeId}")
    public ResponseEntity<Long> getTestsCountByType(@PathVariable Long testTypeId) {
        Long count = createdTestService.getTestsCountByType(testTypeId);
        return ResponseEntity.ok(count);
    }

    // Endpoint to get the number of tests created within a specific date range (corrected to use Date)
    @GetMapping("/count/date-range")
    public ResponseEntity<Long> getTestsCountByDateRange(
            @RequestParam("start") String start,
            @RequestParam("end") String end) throws ParseException {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date startDate = formatter.parse(start);
        Date endDate = formatter.parse(end);

        Long count = createdTestService.getTestsCountByDateRange(startDate, endDate);
        return ResponseEntity.ok(count);
    }

    // Endpoint to get the most common test type (corrected return type to Long)
    @GetMapping("/most-common-type")
    public ResponseEntity<Long> getMostCommonTestType() {
        Long mostCommonType = createdTestService.getMostCommonTestType();
        return ResponseEntity.ok(mostCommonType);
    }
}
