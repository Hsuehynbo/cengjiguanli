package com.company.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "violation_records")
public class ViolationRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "target_job_no", nullable = false)
    private String targetJobNo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "violation_time", nullable = false)
    private LocalDateTime violationTime;

    @Column(name = "reason", nullable = false, columnDefinition = "text")
    private String reason;

    @Column(name = "punishment", nullable = false)
    private String punishment;

    @Column(name = "created_by")
    private String createdBy; // Admin job no

    public ViolationRecord() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTargetJobNo() { return targetJobNo; }
    public void setTargetJobNo(String targetJobNo) { this.targetJobNo = targetJobNo; }
    public LocalDateTime getViolationTime() { return violationTime; }
    public void setViolationTime(LocalDateTime violationTime) { this.violationTime = violationTime; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getPunishment() { return punishment; }
    public void setPunishment(String punishment) { this.punishment = punishment; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
