package com.company.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "job_no", unique = true, nullable = false)
    private String jobNo;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "dept_id")
    private Department department;

    @Column(name = "position", nullable = false)
    private String position;

    @Column(name = "superior_job_no")
    private String superiorJobNo;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "phone")
    private String phone;

    @Column(name = "is_key_personnel", nullable = false)
    private Boolean isKeyPersonnel = false;

    @Column(name = "risk_level", nullable = false)
    private String riskLevel = "NORMAL"; // NORMAL, KEY, RISK, ATTENTION

    @Column(name = "role", nullable = false)
    private String role = "USER"; // ADMIN_GLOBAL, ADMIN_UNIT, USER

    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_permissions",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();

    @JsonIgnore
    @Column(name = "password", nullable = false)
    private String password;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "create_time")
    private LocalDateTime createTime;

    // 构造方法
    public User() {}

    public User(String jobNo, String name, Department department, String position, String superiorJobNo, String password) {
        this.jobNo = jobNo;
        this.name = name;
        this.department = department;
        this.position = position;
        this.superiorJobNo = superiorJobNo;
        this.password = password;
        this.createTime = LocalDateTime.now();
    }

    // getter和setter方法
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getJobNo() {
        return jobNo;
    }

    public void setJobNo(String jobNo) {
        this.jobNo = jobNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getSuperiorJobNo() {
        return superiorJobNo;
    }

    public void setSuperiorJobNo(String superiorJobNo) {
        this.superiorJobNo = superiorJobNo;
    }

    public Boolean getIsKeyPersonnel() {
        return isKeyPersonnel;
    }

    public void setIsKeyPersonnel(Boolean keyPersonnel) {
        isKeyPersonnel = keyPersonnel;
        if (Boolean.TRUE.equals(keyPersonnel)) {
            this.riskLevel = "KEY";
        } else if ("KEY".equals(this.riskLevel)) {
            this.riskLevel = "NORMAL";
        }
    }

    public String getRiskLevel() {
        if (riskLevel == null || riskLevel.isBlank()) {
            return Boolean.TRUE.equals(isKeyPersonnel) ? "KEY" : "NORMAL";
        }
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        String normalized = (riskLevel == null || riskLevel.isBlank()) ? "NORMAL" : riskLevel.toUpperCase();
        this.riskLevel = normalized;
        this.isKeyPersonnel = "KEY".equals(normalized);
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public Set<String> getPermissionCodes() {
        if (permissions == null) return Collections.emptySet();
        return permissions.stream().map(Permission::getCode).collect(java.util.stream.Collectors.toSet());
    }

    public boolean hasPermission(String permissionCode) {
        return getPermissionCodes().contains(permissionCode);
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
