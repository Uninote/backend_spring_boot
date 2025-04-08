package com.uninote.backend.service;

import com.uninote.backend.interfaces.SimilarityStrategy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.AbstractMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.Map;

@Service
public class SimilarityService<T> {

    private final SimilarityStrategy<String> stringSimilarity;

    public SimilarityService(SimilarityStrategy<String> stringSimilarity) {
        this.stringSimilarity = stringSimilarity;
    }

    public List<T> findSimilarItems(
        T target,
        List<T> candidates,
        Function<? super T, String> nameExtractor,
        double threshold,
        int limit
    ) {
        String targetName = nameExtractor.apply(target);

        return candidates.stream()
            .filter(c -> !c.equals(target))
            .map(c -> new AbstractMap.SimpleEntry<>(c, stringSimilarity.computeSimilarity(
                targetName, nameExtractor.apply(c))))
            .filter(e -> e.getValue() > threshold)
            .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
            .limit(limit)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
}
