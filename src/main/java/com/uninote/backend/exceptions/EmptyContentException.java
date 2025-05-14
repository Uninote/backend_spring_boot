package com.uninote.backend.exceptions;

public class EmptyContentException extends RuntimeException {
    public EmptyContentException(String message) {
        super(message);
    }
}

