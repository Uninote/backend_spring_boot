package com.uninote.backend.utils;

import java.text.Normalizer;
import java.util.Locale;

public class GreekTextNormalizer {

    public static String normalize(String input) {
        if (input == null) return "";

        String lower = input.toLowerCase(Locale.forLanguageTag("el"));

        String normalized = Normalizer.normalize(lower, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}
