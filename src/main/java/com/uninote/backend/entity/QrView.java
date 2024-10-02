package com.uninote.backend.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "QR_VIEWS")  
public class QrView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  
    @Column(name = "VIEW_ID", nullable = false) 
    private Long viewId;

    @Column(name = "VIEW_TIME", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")  
    private LocalDateTime viewTime;

    
    public QrView() {}

    public Long getViewId() {
        return viewId;
    }

    public void setViewId(Long viewId) {
        this.viewId = viewId;
    }

    public LocalDateTime getViewTime() {
        return viewTime;
    }

    public void setViewTime(LocalDateTime viewTime) {
        this.viewTime = viewTime;
    }
}
