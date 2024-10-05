package com.uninote.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uninote.backend.entity.KeywordSearch;
import com.uninote.backend.repository.KeywordSearchRepository;

import java.util.List;

@Service
public class KeywordSearchService {

    @Autowired
    private KeywordSearchRepository keywordSearchRepository;

    public List<KeywordSearch> getAllKeywordSearches() {
        return keywordSearchRepository.findAll();
    }

    public KeywordSearch saveKeywordSearch(KeywordSearch keywordSearch) {
        return keywordSearchRepository.save(keywordSearch);
    }

    public void deleteKeywordSearch(Long id) {
        keywordSearchRepository.deleteById(id);
    }
}