package com.company.controller;

import com.company.entity.User;
import com.company.security.SecurityUtils;
import com.company.service.OrganizationService;
import com.company.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/organization")
public class OrganizationController {

    private final OrganizationService organizationService;
    private final UserService userService;

    public OrganizationController(OrganizationService organizationService, UserService userService) {
        this.organizationService = organizationService;
        this.userService = userService;
    }

    @GetMapping("/tree")
    public Map<String, Object> getOrganizationTree(@RequestParam(required = false) Integer deptId) {
        return organizationService.getOrganizationTree(deptId);
    }

    @GetMapping("/departments")
    public List<Map<String, Object>> getDepartments() {
        return organizationService.getDepartments();
    }

    @GetMapping("/children/{id}")
    public List<Map<String, Object>> getChildren(@PathVariable String id) {
        return organizationService.getChildren(id);
    }

    @GetMapping("/search")
    public List<Map<String, Object>> searchUsers(@RequestParam String keyword) {
        return organizationService.searchUsers(keyword);
    }

    @GetMapping("/user/{jobNo}")
    public ResponseEntity<?> getUserInfo(@PathVariable String jobNo) {
        User operator = SecurityUtils.getCurrentUser();
        if (operator == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        // 自己、管理员、有管理权限的人可以查看
        if (!operator.getJobNo().equals(jobNo)
                && !"ADMIN_GLOBAL".equals(operator.getRole()) && !"admin".equals(operator.getJobNo())
                && !operator.hasPermission("PERSONNEL_MANAGE") && !operator.hasPermission("HIERARCHY_MANAGE")) {
            // 检查是否是下属关系
            boolean isSub = isSubordinateOf(operator.getJobNo(), jobNo);
            if (!isSub) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无权查看该人员信息");
            }
        }
        return ResponseEntity.ok(organizationService.getUserInfo(jobNo));
    }

    private boolean isSubordinateOf(String operatorJobNo, String targetJobNo) {
        List<User> allUsers = userService.findAllUsers();
        return isSubordinateRecursive(operatorJobNo, targetJobNo, allUsers);
    }

    private boolean isSubordinateRecursive(String superiorJobNo, String targetJobNo, List<User> allUsers) {
        for (User u : allUsers) {
            if (superiorJobNo.equals(u.getSuperiorJobNo())) {
                if (u.getJobNo().equals(targetJobNo)) return true;
                if (isSubordinateRecursive(u.getJobNo(), targetJobNo, allUsers)) return true;
            }
        }
        return false;
    }

    @GetMapping("/stats/{deptId}")
    public Map<String, Object> getDepartmentStats(@PathVariable Integer deptId) {
        return organizationService.getDepartmentStats(deptId);
    }

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard() {
        return organizationService.getDashboard();
    }
}
