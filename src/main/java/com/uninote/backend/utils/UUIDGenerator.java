package com.uninote.backend.utils;



import java.util.UUID;

public class UUIDGenerator {
    
    
    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }
    
    
    public static String generateUUIDWithoutDashes() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
}