package com.uninote.backend.interfaces;

public interface SimilarityStrategy<T> {
    double computeSimilarity(T a, T b);
}
