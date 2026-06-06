package com.company.controller;

import com.company.security.SecurityUtils;
import com.company.service.ActivityTaskService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/activity-tasks")
public class ActivityTaskController {

    private final ActivityTaskService activityTaskService;

    public ActivityTaskController(ActivityTaskService activityTaskService) {
        this.activityTaskService = activityTaskService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createTask(@RequestBody Map<String, Object> request) {
        try {
            return ResponseEntity.ok(activityTaskService.createTask(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/list")
    public List<Map<String, Object>> getTasks() {
        return activityTaskService.getTasks();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTaskDetail(@PathVariable Long id) {
        Map<String, Object> result = activityTaskService.getTaskDetail(id);
        if (result == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/close")
    public ResponseEntity<?> closeTask(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(activityTaskService.closeTask(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(activityTaskService.deleteTask(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/submit-record")
    public ResponseEntity<?> submitRecord(@RequestBody Map<String, Object> request) {
        try {
            return ResponseEntity.ok(activityTaskService.submitRecord(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my-record/{taskId}")
    public ResponseEntity<?> getMyRecord(@PathVariable Long taskId) {
        Map<String, Object> result = activityTaskService.getRecordForEdit(taskId);
        if (result == null) return ResponseEntity.ok(Map.of("exists", false));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/record-detail/{recordId}")
    public ResponseEntity<?> getRecordDetail(@PathVariable Long recordId) {
        Map<String, Object> result = activityTaskService.getRecordDetail(recordId);
        if (result == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/export")
    public ResponseEntity<?> exportParticipants(@PathVariable Long id) {
        if (!SecurityUtils.canViewGlobal() && !SecurityUtils.hasPermission("ACTIVITY_PUBLISH")) {
            return ResponseEntity.status(403).body(Map.of("error", "无权导出活动数据"));
        }
        try {
            byte[] data = activityTaskService.exportParticipants(id);
            String filename = URLEncoder.encode("活动参与人员.xlsx", StandardCharsets.UTF_8).replace("+", "%20");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
