package com.company.controller;

import com.company.security.SecurityUtils;
import com.company.service.ExportService;
import com.company.service.HomeVisitService;
import com.company.service.TalkRecordService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    private final ExportService exportService;
    private final TalkRecordService talkRecordService;
    private final HomeVisitService homeVisitService;

    public ExportController(ExportService exportService,
                            TalkRecordService talkRecordService,
                            HomeVisitService homeVisitService) {
        this.exportService = exportService;
        this.talkRecordService = talkRecordService;
        this.homeVisitService = homeVisitService;
    }

    @GetMapping("/talk-records")
    public ResponseEntity<byte[]> exportTalkRecords(
            @RequestParam(required = false) String targetJobNo,
            @RequestParam(required = false) String talkType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            Set<String> visibleJobNos = talkRecordService.getVisibleSubordinateJobNos();
            byte[] data = exportService.exportTalkRecords(targetJobNo, talkType, startDate, endDate, visibleJobNos);
            String filename = URLEncoder.encode("谈话记录.xlsx", StandardCharsets.UTF_8).replace("+", "%20");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/home-visits")
    public ResponseEntity<byte[]> exportHomeVisits(
            @RequestParam(required = false) String targetJobNo,
            @RequestParam(required = false) String visitType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            Set<String> visibleJobNos = homeVisitService.getVisibleSubordinateJobNos();
            byte[] data = exportService.exportHomeVisits(targetJobNo, visitType, startDate, endDate, visibleJobNos);
            String filename = URLEncoder.encode("家访记录.xlsx", StandardCharsets.UTF_8).replace("+", "%20");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/department-stats")
    public ResponseEntity<byte[]> exportDepartmentStats(
            @RequestParam(required = false) Integer deptId) {
        try {
            if (!SecurityUtils.canViewGlobal()) {
                com.company.entity.User user = SecurityUtils.getCurrentUser();
                if (user != null && user.getDepartment() != null) {
                    deptId = user.getDepartment().getId();
                }
            }
            byte[] data = exportService.exportDepartmentStats(deptId);
            String filename = URLEncoder.encode("部门统计.xlsx", StandardCharsets.UTF_8).replace("+", "%20");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
