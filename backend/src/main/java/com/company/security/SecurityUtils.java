package com.company.security;

import com.company.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Set;

public class SecurityUtils {

    private SecurityUtils() {}

    public static User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        }
        return null;
    }

    public static String getCurrentJobNo() {
        User user = getCurrentUser();
        return user != null ? user.getJobNo() : null;
    }

    public static boolean isAdminGlobal() {
        User user = getCurrentUser();
        return user != null && ("ADMIN_GLOBAL".equals(user.getRole()) || "admin".equals(user.getJobNo()));
    }

    public static boolean isAdminUnit() {
        User user = getCurrentUser();
        return user != null && "ADMIN_UNIT".equals(user.getRole());
    }

    public static boolean isBureauLeader() {
        User user = getCurrentUser();
        if (user == null) return false;
        String position = user.getPosition() == null ? "" : user.getPosition();
        return position.contains("局长");
    }

    public static boolean isDepartmentHead() {
        User user = getCurrentUser();
        if (user == null) return false;
        String position = user.getPosition() == null ? "" : user.getPosition();
        return (position.contains("所长") || position.contains("队长")
                || position.contains("科长") || position.contains("主任"))
                && !position.startsWith("副");
    }

    public static boolean canViewGlobal() {
        User user = getCurrentUser();
        if (user == null) return false;
        return isAdminGlobal() || isBureauLeader() || user.hasPermission("GLOBAL_DASHBOARD");
    }

    public static boolean hasPermission(String permissionCode) {
        User user = getCurrentUser();
        if (user == null) return false;
        // jobNo "admin" 超级管理员绕过
        if ("admin".equals(user.getJobNo())) return true;
        return user.hasPermission(permissionCode);
    }

    public static boolean hasAnyPermission(String... permissionCodes) {
        for (String code : permissionCodes) {
            if (hasPermission(code)) return true;
        }
        return false;
    }

    public static Set<String> getCurrentUserPermissions() {
        User user = getCurrentUser();
        if (user == null) return Collections.emptySet();
        if ("admin".equals(user.getJobNo())) {
            return Set.of("GLOBAL_DASHBOARD", "PERSONNEL_MANAGE", "HIERARCHY_MANAGE", "ACTIVITY_PUBLISH", "STAT_REPORTS");
        }
        return user.getPermissionCodes();
    }

    public static boolean isInDepartment(Integer deptId) {
        User user = getCurrentUser();
        return user != null && user.getDepartment() != null
                && user.getDepartment().getId().equals(deptId);
    }
}
