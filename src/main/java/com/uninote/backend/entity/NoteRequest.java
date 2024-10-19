package com.uninote.backend.entity;


import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "note_request")
public class NoteRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "usr_id", nullable = false)
    private Long userId;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "request_status", nullable = false)
    private Boolean requestStatus = false; // false for pending, true for completed

    // Constructors, Getters, and Setters

    public NoteRequest() {}

    public NoteRequest(Long courseId, Long usrId) {
        this.courseId = courseId;
        this.userId = usrId;
        this.requestedAt = LocalDateTime.now();
        this.requestStatus = false;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getUsrId() {
        return userId;
    }

    public void setUsrId(Long usrId) {
        this.userId = usrId;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public Boolean getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(Boolean requestStatus) {
        this.requestStatus = requestStatus;
    }
}