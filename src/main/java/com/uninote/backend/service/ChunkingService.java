package com.uninote.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ChunkingService {

    private static final int MAX_CHUNK_SIZE = 500; // characters or words

    public List<String> splitIntoChunks(String text) {
        List<String> chunks = new ArrayList<>();

        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + MAX_CHUNK_SIZE, text.length());
            chunks.add(text.substring(start, end));
            start = end;
        }

        return chunks;
    }
}
