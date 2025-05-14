package com.uninote.backend.dto;

public class TranscriptSnippetDto {
    private String text;
    private double start;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public double getStart() {
        return start;
    }

    public void setStart(double start) {
        this.start = start;
    }
}
