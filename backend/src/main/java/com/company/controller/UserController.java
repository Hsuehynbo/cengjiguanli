package com.company.controller;

import com.company.entity.HierarchyHistory;
import com.company.entity.User;
import com.company.entity.ViolationRecord;
import com.company.security.SecurityUtils;
import com.company.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/list")
    public List<Map<String, Object>> getUsers(
            @RequestParam(required = false) Integer deptId,
            @RequestParam(required = false) String riskLevel) {
        return userService.getUsers(deptId, riskLevel);
    }

    @PutMapping("/{jobNo}")
    public ResponseEntity<?> updateUser(@PathVariable String jobNo, @RequestBody Map<String, Object> updates) {
        try {
            Map<String, Object> result = userService.updateUser(jobNo, updates);
            if (result == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(result);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transferUser(@RequestBody Map<String, Object> request) {
        try {
            String jobNo = (String) request.get("jobNo");
            Object newDeptIdObj = request.get("newDeptId");
            Integer newDeptId = (newDeptIdObj instanceof Integer) ? (Integer) newDeptIdObj
                    : Integer.parseInt(newDeptIdObj.toString());
            return ResponseEntity.ok(userService.transferUser(jobNo, newDeptId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/assign-superior")
    public ResponseEntity<?> assignSuperior(@RequestBody Map<String, Object> request) {
        try {
            String jobNo = (String) request.get("jobNo");
            String superiorJobNo = request.get("superiorJobNo") == null ? null
                    : request.get("superiorJobNo").toString();
            return ResponseEntity.ok(userService.assignSuperior(jobNo, superiorJobNo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @GetMapping("/{jobNo}/hierarchy-history")
    public ResponseEntity<?> getHierarchyHistory(@PathVariable String jobNo) {
        if (!canViewUserData(jobNo)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无权查看该人员的层级历史");
        }
        return ResponseEntity.ok(userService.getHierarchyHistory(jobNo));
    }

    @GetMapping("/{jobNo}/violation-records")
    public ResponseEntity<?> getViolationRecords(@PathVariable String jobNo) {
        if (!canViewUserData(jobNo)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无权查看该人员的违规记录");
        }
        return ResponseEntity.ok(userService.getViolationRecords(jobNo));
    }

    @PostMapping("/violation-records")
    public ResponseEntity<?> addViolationRecord(@RequestBody Map<String, Object> request) {
        User operator = SecurityUtils.getCurrentUser();
        if (operator == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (!"ADMIN_GLOBAL".equals(operator.getRole()) && !"admin".equals(operator.getJobNo())
                && !operator.hasPermission("PERSONNEL_MANAGE") && !operator.hasPermission("HIERARCHY_MANAGE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无权添加违规记录");
        }
        return ResponseEntity.ok(userService.addViolationRecord(request));
    }

    private boolean canViewUserData(String targetJobNo) {
        User operator = SecurityUtils.getCurrentUser();
        if (operator == null) return false;
        if (operator.getJobNo().equals(targetJobNo)) return true;
        if ("ADMIN_GLOBAL".equals(operator.getRole()) || "admin".equals(operator.getJobNo())) return true;
        if (operator.hasPermission("PERSONNEL_MANAGE") || operator.hasPermission("HIERARCHY_MANAGE")) return true;
        return isSubordinateOf(operator.getJobNo(), targetJobNo);
    }

    private boolean isSubordinateOf(String operatorJobNo, String targetJobNo) {
        java.util.List<User> allUsers = userService.findAllUsers();
        return isSubordinateRecursive(operatorJobNo, targetJobNo, allUsers);
    }

    private boolean isSubordinateRecursive(String superiorJobNo, String targetJobNo, java.util.List<User> allUsers) {
        for (User u : allUsers) {
            if (superiorJobNo.equals(u.getSuperiorJobNo())) {
                if (u.getJobNo().equals(targetJobNo)) return true;
                if (isSubordinateRecursive(u.getJobNo(), targetJobNo, allUsers)) return true;
            }
        }
        return false;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody Map<String, Object> request) {
        try {
            return ResponseEntity.ok(userService.createUser(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{jobNo}")
    public ResponseEntity<?> deleteUser(@PathVariable String jobNo) {
        try {
            userService.deleteUser(jobNo);
            return ResponseEntity.ok(Map.of("message", "用户已停用"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/batch-import")
    public ResponseEntity<?> batchImport(@RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(userService.batchImportUsers(file));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
