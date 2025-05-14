package com.uninote.backend.utils;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.uninote.backend.service.ContentExtractionService;

@Component
public class StartupRunner implements ApplicationRunner {

    private final ContentExtractionService contentExtractionService;

    public StartupRunner(ContentExtractionService contentExtractionService) {
        this.contentExtractionService = contentExtractionService;
    }

    @Override
    public void run(ApplicationArguments args) {
        contentExtractionService.extractContentInBatches(400);
    }
}
