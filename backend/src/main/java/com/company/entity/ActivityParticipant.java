package com.company.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "activity_participants")
public class ActivityParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "record_id", nullable = false)
    private Long recordId;

    @Column(name = "user_job_no", nullable = false)
    private String userJobNo;

    @Column(name = "user_name")
    private String userName;

    public ActivityParticipant() {}

    public ActivityParticipant(Long recordId, String userJobNo, String userName) {
        this.recordId = recordId;
        this.userJobNo = userJobNo;
        this.userName = userName;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }

    public String getUserJobNo() { return userJobNo; }
    public void setUserJobNo(String userJobNo) { this.userJobNo = userJobNo; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
}
