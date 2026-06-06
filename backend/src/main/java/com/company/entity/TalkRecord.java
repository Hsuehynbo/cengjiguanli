package com.company.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "talk_records")
public class TalkRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "talker_job_no", nullable = false)
    private String talkerJobNo;

    @Column(name = "target_job_no", nullable = false)
    private String targetJobNo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "talk_time", nullable = false)
    private LocalDateTime talkTime;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "photo")
    private String photo;

    @Column(name = "talk_type")
    private String talkType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "create_time")
    private LocalDateTime createTime;

    // 构造方法
    public TalkRecord() {}

    public TalkRecord(String talkerJobNo, String targetJobNo, LocalDateTime talkTime, String location, String content, String photo, String talkType) {
        this.talkerJobNo = talkerJobNo;
        this.targetJobNo = targetJobNo;
        this.talkTime = talkTime;
        this.location = location;
        this.content = content;
        this.photo = photo;
        this.talkType = talkType;
        this.createTime = LocalDateTime.now();
    }

    // getter和setter方法
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTalkerJobNo() {
        return talkerJobNo;
    }

    public void setTalkerJobNo(String talkerJobNo) {
        this.talkerJobNo = talkerJobNo;
    }

    public String getTargetJobNo() {
        return targetJobNo;
    }

    public void setTargetJobNo(String targetJobNo) {
        this.targetJobNo = targetJobNo;
    }

    public LocalDateTime getTalkTime() {
        return talkTime;
    }

    public void setTalkTime(LocalDateTime talkTime) {
        this.talkTime = talkTime;
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

    public String getTalkType() {
        return talkType;
    }

    public void setTalkType(String talkType) {
        this.talkType = talkType;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
