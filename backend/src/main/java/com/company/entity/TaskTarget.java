package com.company.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_targets")
public class TaskTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "dept_id", nullable = false)
    private Integer deptId;

    @Column(name = "dept_name")
    private String deptName;

    @Column(name = "status", nullable = false)
    private String status = "PENDING";

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "submit_time")
    private LocalDateTime submitTime;

    public TaskTarget() {}

    public TaskTarget(Long taskId, Integer deptId, String deptName) {
        this.taskId = taskId;
        this.deptId = deptId;
        this.deptName = deptName;
        this.status = "PENDING";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public Integer getDeptId() { return deptId; }
    public void setDeptId(Integer deptId) { this.deptId = deptId; }

    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getSubmitTime() { return submitTime; }
    public void setSubmitTime(LocalDateTime submitTime) { this.submitTime = submitTime; }
}
