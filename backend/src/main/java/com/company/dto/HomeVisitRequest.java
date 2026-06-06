package com.company.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class HomeVisitRequest {

    @NotBlank(message = "操作人工号不能为空")
    private String operatorJobNo;

    @NotBlank(message = "被家访人工号不能为空")
    private String targetJobNo;

    @NotNull(message = "家访时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime visitTime;

    private String visitType;

    @NotBlank(message = "地点不能为空")
    private String location;

    @NotBlank(message = "内容不能为空")
    private String content;

    public String getOperatorJobNo() {
        return operatorJobNo;
    }

    public void setOperatorJobNo(String operatorJobNo) {
        this.operatorJobNo = operatorJobNo;
    }

    public String getTargetJobNo() {
        return targetJobNo;
    }

    public void setTargetJobNo(String targetJobNo) {
        this.targetJobNo = targetJobNo;
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
}
