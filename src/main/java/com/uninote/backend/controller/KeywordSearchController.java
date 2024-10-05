package com.uninote.backend.controller;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.uninote.backend.entity.KeywordSearch;
import com.uninote.backend.repository.UserSessionRepository;
import com.uninote.backend.service.KeywordSearchService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/keyword-search")
public class KeywordSearchController {

    @Autowired
    private KeywordSearchService keywordSearchService;

    @Autowired 
    private UserSessionRepository userSessionRepository;

    @GetMapping
    public List<KeywordSearch> getAllKeywordSearches() {
        return keywordSearchService.getAllKeywordSearches();
    }

    @PostMapping
    public KeywordSearch createKeywordSearch(@RequestParam String keyword, @RequestParam Long userId ) {
        KeywordSearch keywordSearch =  new KeywordSearch();
        keywordSearch.setKeyword(keyword);
        keywordSearch.setUserId(userId);
        Long sessionId = userSessionRepository.findLastActiveSessionIdByUserId(userId).orElseThrow(() -> new IllegalArgumentException("Use has no active sessions"));
        keywordSearch.setSessionId(sessionId);
        keywordSearch.setSearchTime(LocalDateTime.now());
        return keywordSearchService.saveKeywordSearch(keywordSearch);
    }

    @DeleteMapping("/{id}")
    public void deleteKeywordSearch(@PathVariable Long id) {
        keywordSearchService.deleteKeywordSearch(id);
    }
}
