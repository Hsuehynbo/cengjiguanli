package com.company.controller;

import com.company.dto.HomeVisitRequest;
import com.company.service.HomeVisitService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/home-visits")
public class HomeVisitController {

    private final HomeVisitService homeVisitService;

    public HomeVisitController(HomeVisitService homeVisitService) {
        this.homeVisitService = homeVisitService;
    }

    @GetMapping("/list")
    public List<Map<String, Object>> getHomeVisits(
            @RequestParam(required = false) String targetJobNo,
            @RequestParam(required = false) String visitType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return homeVisitService.getHomeVisits(targetJobNo, visitType, startDate, endDate);
    }

    @PostMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addHomeVisit(
            @RequestParam("operatorJobNo") String operatorJobNo,
            @RequestParam("targetJobNo") String targetJobNo,
            @RequestParam("visitTime") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime visitTime,
            @RequestParam("visitType") String visitType,
            @RequestParam("location") String location,
            @RequestParam("content") String content,
            @RequestParam(value = "photo", required = false) MultipartFile photoFile) {
        try {
            return ResponseEntity.ok(homeVisitService.addHomeVisit(
                    operatorJobNo, targetJobNo, visitTime, visitType, location, content, photoFile));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("文件上传失败: " + e.getMessage());
        }
    }

    @PostMapping(consumes = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addHomeVisitByJson(@Valid @RequestBody HomeVisitRequest request) {
        try {
            return ResponseEntity.ok(homeVisitService.addHomeVisitByJson(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateHomeVisit(@PathVariable Long id, @Valid @RequestBody HomeVisitRequest request) {
        var result = homeVisitService.updateHomeVisit(id, request);
        if (result == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteHomeVisit(@PathVariable Long id) {
        if (homeVisitService.deleteHomeVisit(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getHomeVisitById(@PathVariable Long id) {
        Map<String, Object> result = homeVisitService.getHomeVisitById(id);
        if (result == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }
}
