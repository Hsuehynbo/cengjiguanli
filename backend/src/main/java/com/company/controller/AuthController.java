package com.company.controller;

import com.company.entity.User;
import com.company.repository.UserRepository;
import com.company.security.JwtTokenProvider;
import com.company.security.TokenBlacklist;
import com.company.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private TokenBlacklist tokenBlacklist;

    @Autowired
    private AuditLogService auditLogService;

    static class LoginRequest {
        private String jobNo;
        private String password;

        public String getJobNo() { return jobNo; }
        public void setJobNo(String jobNo) { this.jobNo = jobNo; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String jobNo = request.getJobNo();
        String password = request.getPassword();

        if (jobNo == null || password == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "工号和密码不能为空"));
        }

        User user = userRepository.findByJobNo(jobNo);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "工号或密码错误"));
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "工号或密码错误"));
        }

        List<String> permissionCodes = new ArrayList<>(user.getPermissionCodes());
        String token = tokenProvider.generateToken(user.getJobNo(), user.getRole(), user.getPosition(), permissionCodes);

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("jobNo", user.getJobNo());
        userInfo.put("name", user.getName());
        userInfo.put("position", user.getPosition());
        userInfo.put("superiorJobNo", user.getSuperiorJobNo());
        userInfo.put("role", user.getRole());
        userInfo.put("isKeyPersonnel", user.getIsKeyPersonnel());
        userInfo.put("riskLevel", user.getRiskLevel());
        userInfo.put("permissions", permissionCodes);
        if (user.getDepartment() != null) {
            Map<String, Object> deptInfo = new HashMap<>();
            deptInfo.put("id", user.getDepartment().getId());
            deptInfo.put("deptName", user.getDepartment().getDeptName());
            userInfo.put("department", deptInfo);
        }

        response.put("user", userInfo);

        auditLogService.log("LOGIN", "USER", user.getJobNo(),
                "用户登录：" + user.getName() + "（" + user.getJobNo() + "）");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklist.blacklist(token);
        }
        return ResponseEntity.ok(Map.of("message", "已退出登录"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                             @RequestBody Map<String, String> request) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "未登录"));
        }
        String token = authHeader.substring(7);
        String jobNo;
        try {
            jobNo = tokenProvider.getJobNo(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "token无效"));
        }

        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        if (oldPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "旧密码和新密码不能为空"));
        }
        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "新密码长度不能少于6位"));
        }

        User user = userRepository.findByJobNo(jobNo);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "用户不存在"));
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("error", "旧密码错误"));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        auditLogService.log("CHANGE_PASSWORD", "USER", user.getJobNo(),
                "修改密码：" + user.getName() + "（" + user.getJobNo() + "）");

        return ResponseEntity.ok(Map.of("message", "密码修改成功"));
    }
}
