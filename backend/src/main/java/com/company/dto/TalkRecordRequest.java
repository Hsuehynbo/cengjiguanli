package com.company.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class TalkRecordRequest {

    @NotBlank(message = "谈话人工号不能为空")
    private String talkerJobNo;

    @NotBlank(message = "被谈话人工号不能为空")
    private String targetJobNo;

    @NotNull(message = "谈话时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime talkTime;

    @NotBlank(message = "地点不能为空")
    private String location;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String talkType;

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

    public String getTalkType() {
        return talkType;
    }

    public void setTalkType(String talkType) {
        this.talkType = talkType;
    }
}
