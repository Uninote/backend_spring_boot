package com.uninote.backend.utils;

import java.security.SecureRandom;
import java.util.Base64;

public class KeyGenerator {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

    public static String generateKey() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    /*public static void main(String[] args) {
        System.out.println("Generated API Key: " + generateKey());
    }*/
}
