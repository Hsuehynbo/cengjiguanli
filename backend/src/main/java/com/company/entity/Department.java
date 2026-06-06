package com.company.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "depts")
public class Department {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "dept_name")
    private String deptName;
    
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
    private List<User> users;
    
    // 构造方法
    public Department() {}
    
    public Department(String deptName) {
        this.deptName = deptName;
    }
    
    // getter和setter方法
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getDeptName() {
        return deptName;
    }
    
    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }
    
    public List<User> getUsers() {
        return users;
    }
    
    public void setUsers(List<User> users) {
        this.users = users;
    }
}