package com.company.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "home_visits")
public class HomeVisit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "target_job_no", nullable = false)
    private String targetJobNo;

    @Column(name = "operator_job_no", nullable = false)
    private String operatorJobNo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "visit_time", nullable = false)
    private LocalDateTime visitTime;

    @Column(name = "visit_type", nullable = false)
    private String visitType;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "photo")
    private String photo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTargetJobNo() {
        return targetJobNo;
    }

    public void setTargetJobNo(String targetJobNo) {
        this.targetJobNo = targetJobNo;
    }

    public String getOperatorJobNo() {
        return operatorJobNo;
    }

    public void setOperatorJobNo(String operatorJobNo) {
        this.operatorJobNo = operatorJobNo;
    }

    public LocalDateTime getVisitTime() {
        return visitTime;
    }

    public void setVisitTime(LocalDateTime visitTime) {
        this.visitTime = visitTime;
    }

    public String getVisitType() {
        return visitType;
    }

    public void setVisitType(String visitType) {
        this.visitType = visitType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
