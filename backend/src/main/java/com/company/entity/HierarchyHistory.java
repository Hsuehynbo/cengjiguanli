package com.company.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "hierarchy_history")
public class HierarchyHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "target_job_no", nullable = false)
    private String targetJobNo;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "unit_name")
    private String unitName;

    @Column(name = "manager_job_no")
    private String managerJobNo;

    @Column(name = "manager_name")
    private String managerName;

    public HierarchyHistory() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTargetJobNo() { return targetJobNo; }
    public void setTargetJobNo(String targetJobNo) { this.targetJobNo = targetJobNo; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }
    public String getManagerJobNo() { return managerJobNo; }
    public void setManagerJobNo(String managerJobNo) { this.managerJobNo = managerJobNo; }
    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }
}
