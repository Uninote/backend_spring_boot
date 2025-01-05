package com.uninote.backend.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "PROCESSED_NOTES")
public class ProcessedNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PROCESSED_NOTE_ID")
    private Long processedNoteId;

    @OneToOne
    @JoinColumn(name = "NOTE_ID", nullable = false)
    private Note note;

    @Lob
    @Column(name = "SUMMARY")
    private String summary;

    @Lob
    @Column(name = "QUIZ_JSON")
    private String quizJson;

    @Column(name = "PROCESSED_TIME", nullable = false, updatable = false)
    private LocalDateTime processedTime = LocalDateTime.now();

    public Long getProcessedNoteId() {
        return processedNoteId;
    }

    public void setProcessedNoteId(Long processedNoteId) {
        this.processedNoteId = processedNoteId;
    }

    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getQuizJson() {
        return quizJson;
    }

    public void setQuizJson(String quizJson) {
        this.quizJson = quizJson;
    }

    public LocalDateTime getProcessedTime() {
        return processedTime;
    }

    public void setProcessedTime(LocalDateTime processedTime) {
        this.processedTime = processedTime;
    }
}
