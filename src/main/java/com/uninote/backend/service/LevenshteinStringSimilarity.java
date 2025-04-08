package com.uninote.backend.service;

import com.uninote.backend.interfaces.SimilarityStrategy;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

@Component
public class LevenshteinStringSimilarity implements SimilarityStrategy<String> {

    private final Analyzer analyzer = new StandardAnalyzer();

    @Override
    public double computeSimilarity(String a, String b) {
        try {
            Map<String, Integer> tf1 = tokenize(a);
            Map<String, Integer> tf2 = tokenize(b);

            Set<String> allTokens = new HashSet<>();
            allTokens.addAll(tf1.keySet());
            allTokens.addAll(tf2.keySet());

            double dot = 0.0, normA = 0.0, normB = 0.0;

            for (String token : allTokens) {
                int v1 = tf1.getOrDefault(token, 0);
                int v2 = tf2.getOrDefault(token, 0);
                dot += v1 * v2;
                normA += v1 * v1;
                normB += v2 * v2;
            }

            return normA > 0 && normB > 0 ? dot / (Math.sqrt(normA) * Math.sqrt(normB)) : 0.0;

        } catch (IOException e) {
            throw new RuntimeException("Failed to compute similarity", e);
        }
    }

    private Map<String, Integer> tokenize(String text) throws IOException {
        Map<String, Integer> freq = new HashMap<>();
        try (StringReader reader = new StringReader(text.toLowerCase())) {
            var stream = analyzer.tokenStream(null, reader);
            stream.reset();
            while (stream.incrementToken()) {
                String token = stream.getAttribute(CharTermAttribute.class).toString();
                freq.put(token, freq.getOrDefault(token, 0) + 1);
            }
            stream.end();
            stream.close();
        }
        return freq;
    }
}
