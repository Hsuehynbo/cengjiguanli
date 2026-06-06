package com.company.service;

import com.company.entity.Permission;
import com.company.entity.User;
import com.company.repository.PermissionRepository;
import com.company.repository.UserRepository;
import com.company.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public PermissionService(PermissionRepository permissionRepository,
                             UserRepository userRepository,
                             AuditLogService auditLogService) {
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    public List<Map<String, Object>> getAllPermissions() {
        return permissionRepository.findAll().stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("code", p.getCode());
            map.put("name", p.getName());
            map.put("description", p.getDescription());
            map.put("category", p.getCategory());
            return map;
        }).collect(Collectors.toList());
    }

    public List<String> getUserPermissionCodes(String jobNo) {
        User user = userRepository.findByJobNo(jobNo);
        if (user == null) throw new IllegalArgumentException("用户不存在");
        return new ArrayList<>(user.getPermissionCodes());
    }

    @Transactional
    public void setUserPermissions(String jobNo, List<String> permissionCodes) {
        requireGlobalAdmin();
        User user = userRepository.findByJobNo(jobNo);
        if (user == null) throw new IllegalArgumentException("用户不存在");
        // 不能修改 admin 超级管理员的权限
        if ("admin".equals(jobNo)) throw new SecurityException("无法修改超级管理员的权限");

        Set<Permission> newPermissions = new HashSet<>();
        for (String code : permissionCodes) {
            Permission perm = permissionRepository.findByCode(code)
                    .orElseThrow(() -> new IllegalArgumentException("权限代码不存在: " + code));
            newPermissions.add(perm);
        }
        user.setPermissions(newPermissions);
        userRepository.save(user);

        auditLogService.log("SET_PERMISSIONS", "USER", jobNo,
                "设置权限：" + String.join(", ", permissionCodes));
    }

    @Transactional
    public void grantPermissions(String jobNo, List<String> permissionCodes) {
        requireGlobalAdmin();
        User user = userRepository.findByJobNo(jobNo);
        if (user == null) throw new IllegalArgumentException("用户不存在");
        if ("admin".equals(jobNo)) throw new SecurityException("无法修改超级管理员的权限");

        Set<Permission> current = new HashSet<>(user.getPermissions());
        for (String code : permissionCodes) {
            Permission perm = permissionRepository.findByCode(code)
                    .orElseThrow(() -> new IllegalArgumentException("权限代码不存在: " + code));
            current.add(perm);
        }
        user.setPermissions(current);
        userRepository.save(user);

        auditLogService.log("GRANT_PERMISSIONS", "USER", jobNo,
                "授予权限：" + String.join(", ", permissionCodes));
    }

    @Transactional
    public void revokePermissions(String jobNo, List<String> permissionCodes) {
        requireGlobalAdmin();
        User user = userRepository.findByJobNo(jobNo);
        if (user == null) throw new IllegalArgumentException("用户不存在");
        if ("admin".equals(jobNo)) throw new SecurityException("无法修改超级管理员的权限");

        Set<Permission> current = new HashSet<>(user.getPermissions());
        current.removeIf(p -> permissionCodes.contains(p.getCode()));
        user.setPermissions(current);
        userRepository.save(user);

        auditLogService.log("REVOKE_PERMISSIONS", "USER", jobNo,
                "撤销权限：" + String.join(", ", permissionCodes));
    }

    private void requireGlobalAdmin() {
        if (!SecurityUtils.isAdminGlobal()) {
            throw new SecurityException("只有全局管理员可以管理权限");
        }
    }
}
