package com.company.controller;

import com.company.dto.TalkRecordRequest;
import com.company.service.TalkRecordService;
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
@RequestMapping("/api/talk-records")
public class TalkRecordController {

    private final TalkRecordService talkRecordService;

    public TalkRecordController(TalkRecordService talkRecordService) {
        this.talkRecordService = talkRecordService;
    }

    @GetMapping("/list")
    public List<Map<String, Object>> getTalkRecords(
            @RequestParam(required = false) String targetJobNo,
            @RequestParam(required = false) String talkType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return talkRecordService.getTalkRecords(targetJobNo, talkType, startDate, endDate);
    }

    @GetMapping("/trend")
    public Map<String, Object> getTrend(@RequestParam(defaultValue = "7") int days) {
        return talkRecordService.getTrend(days);
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getTalkRecordDetail(@PathVariable Integer id) {
        Map<String, Object> result = talkRecordService.getTalkRecordDetail(id);
        if (result == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }

    @PostMapping("")
    public ResponseEntity<?> addTalkRecord(@Valid @RequestBody TalkRecordRequest request) {
        try {
            return ResponseEntity.ok(talkRecordService.addTalkRecord(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> addTalkRecordWithPhoto(
            @RequestParam String talkerJobNo,
            @RequestParam String targetJobNo,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime talkTime,
            @RequestParam String location,
            @RequestParam String content,
            @RequestParam(required = false) String talkType,
            @RequestParam(required = false) MultipartFile photo) {
        try {
            return ResponseEntity.ok(talkRecordService.addTalkRecordWithPhoto(
                    talkerJobNo, targetJobNo, talkTime, location, content, talkType, photo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("文件上传失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTalkRecord(@PathVariable Integer id, @Valid @RequestBody TalkRecordRequest request) {
        var result = talkRecordService.updateTalkRecord(id, request);
        if (result == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteTalkRecord(@PathVariable Integer id) {
        if (talkRecordService.deleteTalkRecord(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
