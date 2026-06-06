package com.company.controller;

import com.company.security.SecurityUtils;
import com.company.service.PermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllPermissions() {
        return ResponseEntity.ok(permissionService.getAllPermissions());
    }

    @GetMapping("/user/{jobNo}")
    public ResponseEntity<?> getUserPermissions(@PathVariable String jobNo) {
        try {
            List<String> codes = permissionService.getUserPermissionCodes(jobNo);
            return ResponseEntity.ok(Map.of("jobNo", jobNo, "permissions", codes));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/user/{jobNo}")
    public ResponseEntity<?> setUserPermissions(@PathVariable String jobNo,
                                                 @RequestBody Map<String, Object> body) {
        try {
            @SuppressWarnings("unchecked")
            List<String> codes = (List<String>) body.get("permissionCodes");
            if (codes == null) return ResponseEntity.badRequest().body(Map.of("error", "缺少 permissionCodes"));
            permissionService.setUserPermissions(jobNo, codes);
            return ResponseEntity.ok(Map.of("message", "权限设置成功"));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/user/{jobNo}/grant")
    public ResponseEntity<?> grantPermissions(@PathVariable String jobNo,
                                               @RequestBody Map<String, Object> body) {
        try {
            @SuppressWarnings("unchecked")
            List<String> codes = (List<String>) body.get("permissionCodes");
            if (codes == null) return ResponseEntity.badRequest().body(Map.of("error", "缺少 permissionCodes"));
            permissionService.grantPermissions(jobNo, codes);
            return ResponseEntity.ok(Map.of("message", "权限授予成功"));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/user/{jobNo}/revoke")
    public ResponseEntity<?> revokePermissions(@PathVariable String jobNo,
                                                @RequestBody Map<String, Object> body) {
        try {
            @SuppressWarnings("unchecked")
            List<String> codes = (List<String>) body.get("permissionCodes");
            if (codes == null) return ResponseEntity.badRequest().body(Map.of("error", "缺少 permissionCodes"));
            permissionService.revokePermissions(jobNo, codes);
            return ResponseEntity.ok(Map.of("message", "权限撤销成功"));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
