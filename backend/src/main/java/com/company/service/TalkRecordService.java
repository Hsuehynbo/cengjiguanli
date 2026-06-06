package com.company.service;

import com.company.dto.TalkRecordRequest;
import com.company.entity.TalkRecord;
import com.company.entity.User;
import com.company.repository.TalkRecordRepository;
import com.company.repository.UserRepository;
import com.company.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TalkRecordService {

    private final TalkRecordRepository talkRecordRepository;
    private final UserRepository userRepository;
    private final HierarchyService hierarchyService;
    private final FileUploadService fileUploadService;
    private final AuditLogService auditLogService;

    public TalkRecordService(TalkRecordRepository talkRecordRepository,
                             UserRepository userRepository,
                             HierarchyService hierarchyService,
                             FileUploadService fileUploadService,
                             AuditLogService auditLogService) {
        this.talkRecordRepository = talkRecordRepository;
        this.userRepository = userRepository;
        this.hierarchyService = hierarchyService;
        this.fileUploadService = fileUploadService;
        this.auditLogService = auditLogService;
    }

    public List<Map<String, Object>> getTalkRecords(String targetJobNo, String talkType, String startDate, String endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        Map<String, String> userNames = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getJobNo, User::getName, (a, b) -> a));

        LocalDateTime start = (startDate != null && !startDate.isEmpty())
                ? LocalDate.parse(startDate).atStartOfDay() : null;
        LocalDateTime end = (endDate != null && !endDate.isEmpty())
                ? LocalDate.parse(endDate).plusDays(1).atStartOfDay() : null;

        boolean hasTarget = targetJobNo != null && !targetJobNo.isEmpty();
        boolean hasType = talkType != null && !talkType.isEmpty();
        boolean hasTimeRange = start != null && end != null;

        List<TalkRecord> records;
        if (hasTarget && hasType && hasTimeRange) {
            records = talkRecordRepository.findByTargetJobNoAndTalkTypeAndTalkTimeBetweenOrderByTalkTimeDesc(targetJobNo, talkType, start, end);
        } else if (hasTarget && hasTimeRange) {
            records = talkRecordRepository.findByTargetJobNoAndTalkTimeBetweenOrderByTalkTimeDesc(targetJobNo, start, end);
        } else if (hasType && hasTimeRange) {
            records = talkRecordRepository.findByTalkTypeAndTalkTimeBetweenOrderByTalkTimeDesc(talkType, start, end);
        } else if (hasTimeRange) {
            records = talkRecordRepository.findByTalkTimeBetweenOrderByTalkTimeDesc(start, end);
        } else {
            records = talkRecordRepository.findAll().stream()
                    .filter(r -> !hasTarget || r.getTargetJobNo().equals(targetJobNo))
                    .filter(r -> !hasType || talkType.equals(r.getTalkType()))
                    .sorted((a, b) -> {
                        if (a.getTalkTime() == null && b.getTalkTime() == null) return 0;
                        if (a.getTalkTime() == null) return 1;
                        if (b.getTalkTime() == null) return -1;
                        return b.getTalkTime().compareTo(a.getTalkTime());
                    })
                    .collect(Collectors.toList());
        }

        // 按权限过滤：只能看到自己下属的记录（系统管理员/局领导看全部）
        Set<String> visibleJobNos = getVisibleSubordinateJobNos();
        if (visibleJobNos != null) {
            records = records.stream()
                    .filter(r -> visibleJobNos.contains(r.getTargetJobNo()))
                    .collect(Collectors.toList());
        }

        return records.stream().map(record -> {
            Map<String, Object> response = new HashMap<>();
            response.put("id", record.getId());
            response.put("talkerJobNo", record.getTalkerJobNo());
            response.put("talkerName", userNames.getOrDefault(record.getTalkerJobNo(), record.getTalkerJobNo()));
            response.put("targetJobNo", record.getTargetJobNo());
            response.put("targetName", userNames.getOrDefault(record.getTargetJobNo(), record.getTargetJobNo()));
            response.put("talkTime", record.getTalkTime() != null ? record.getTalkTime().format(formatter) : "");
            response.put("talkType", record.getTalkType());
            response.put("location", record.getLocation());
            response.put("content", record.getContent());
            response.put("photo", record.getPhoto());
            return response;
        }).collect(Collectors.toList());
    }

    public Map<String, Object> getTrend(int days) {
        List<TalkRecord> allTalks = talkRecordRepository.findAll();

        // 按权限过滤：只能看到自己下属的记录
        Set<String> visibleJobNos = getVisibleSubordinateJobNos();
        if (visibleJobNos != null) {
            allTalks = allTalks.stream()
                    .filter(r -> visibleJobNos.contains(r.getTargetJobNo()))
                    .collect(Collectors.toList());
        }

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        List<String> dates = new ArrayList<>();
        List<Integer> talkCounts = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = days - 1; i >= 0; i--) {
            LocalDateTime dayStart = todayStart.minusDays(i);
            LocalDateTime dayEnd = dayStart.plusDays(1);

            String label = dayStart.format(formatter);
            dates.add(label);

            long count = allTalks.stream()
                    .filter(t -> t.getTalkTime() != null
                            && !t.getTalkTime().isBefore(dayStart)
                            && t.getTalkTime().isBefore(dayEnd))
                    .count();
            talkCounts.add((int) count);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("dates", dates);
        result.put("talks", talkCounts);
        return result;
    }

    public Map<String, Object> getTalkRecordDetail(Integer id) {
        TalkRecord record = talkRecordRepository.findById(id).orElse(null);
        if (record == null) return null;

        // 访问控制：只能查看自己下属的记录详情
        Set<String> visibleJobNos = getVisibleSubordinateJobNos();
        if (visibleJobNos != null && !visibleJobNos.contains(record.getTargetJobNo())) {
            throw new SecurityException("无权查看此谈话记录");
        }

        User talker = userRepository.findByJobNo(record.getTalkerJobNo());
        String talkerName = talker != null ? talker.getName() : record.getTalkerJobNo();
        User target = userRepository.findByJobNo(record.getTargetJobNo());
        String targetName = target != null ? target.getName() : record.getTargetJobNo();

        Map<String, Object> response = new HashMap<>();
        response.put("id", record.getId());
        response.put("talkerJobNo", record.getTalkerJobNo());
        response.put("talkerName", talkerName);
        response.put("targetJobNo", record.getTargetJobNo());
        response.put("targetName", targetName);
        response.put("talkTime", record.getTalkTime());
        response.put("talkType", record.getTalkType());
        response.put("location", record.getLocation());
        response.put("content", record.getContent());
        response.put("photo", record.getPhoto());
        return response;
    }

    public TalkRecord addTalkRecord(TalkRecordRequest request) {
        hierarchyService.validateHierarchy(request.getTalkerJobNo(), request.getTargetJobNo());
        checkDuplicate(request.getTargetJobNo(), request.getTalkType(), request.getTalkTime());

        if (request.getTalkerJobNo() == null || request.getTalkerJobNo().isBlank()) {
            throw new IllegalArgumentException("谈话人工号不能为空");
        }
        TalkRecord talkRecord = new TalkRecord(
                request.getTalkerJobNo(),
                request.getTargetJobNo(),
                request.getTalkTime(),
                request.getLocation(),
                request.getContent(),
                null,
                request.getTalkType()
        );
        talkRecordRepository.save(talkRecord);

        auditLogService.log("ADD_TALK", "TALK_RECORD", talkRecord.getId().toString(),
                "新增谈话记录：对象" + request.getTargetJobNo() + "，类型" + request.getTalkType());

        return talkRecord;
    }

    public TalkRecord addTalkRecordWithPhoto(String talkerJobNo, String targetJobNo,
                                              LocalDateTime talkTime, String location, String content,
                                              String talkType, MultipartFile photo) throws IOException {
        hierarchyService.validateHierarchy(talkerJobNo, targetJobNo);
        checkDuplicate(targetJobNo, talkType, talkTime);

        String photoPath = null;
        if (photo != null && !photo.isEmpty()) {
            photoPath = fileUploadService.uploadFile(photo);
        }

        TalkRecord talkRecord = new TalkRecord(talkerJobNo, targetJobNo, talkTime, location, content, photoPath, talkType);
        talkRecordRepository.save(talkRecord);

        auditLogService.log("ADD_TALK", "TALK_RECORD", talkRecord.getId().toString(),
                "新增谈话记录（含照片）：对象" + targetJobNo + "，类型" + talkType);

        return talkRecord;
    }

    private void checkDuplicate(String targetJobNo, String talkType, LocalDateTime talkTime) {
        if (targetJobNo == null || talkType == null || talkTime == null) return;
        LocalDate date = talkTime.toLocalDate();
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        List<TalkRecord> existing = talkRecordRepository.findByTargetAndTypeAndDateRange(targetJobNo, talkType, dayStart, dayEnd);
        if (!existing.isEmpty()) {
            throw new IllegalArgumentException("该人员当天已有相同类型的谈话记录，不能重复录入");
        }
    }

    public TalkRecord updateTalkRecord(Integer id, TalkRecordRequest request) {
        TalkRecord record = talkRecordRepository.findById(id).orElse(null);
        if (record == null) return null;

        User operator = SecurityUtils.getCurrentUser();
        if (operator == null || !canModifyRecord(operator, record)) {
            throw new SecurityException("无权限修改此谈话记录");
        }

        record.setTalkTime(request.getTalkTime());
        record.setLocation(request.getLocation());
        record.setContent(request.getContent());
        record.setTalkType(request.getTalkType());
        talkRecordRepository.save(record);

        auditLogService.log("UPDATE_TALK", "TALK_RECORD", id.toString(),
                "修改谈话记录：对象" + record.getTargetJobNo());

        return record;
    }

    public boolean deleteTalkRecord(Integer id) {
        TalkRecord record = talkRecordRepository.findById(id).orElse(null);
        if (record == null) return false;

        User operator = SecurityUtils.getCurrentUser();
        if (operator == null || !canModifyRecord(operator, record)) {
            throw new SecurityException("无权限删除此谈话记录");
        }

        talkRecordRepository.delete(record);

        auditLogService.log("DELETE_TALK", "TALK_RECORD", id.toString(),
                "删除谈话记录：对象" + record.getTargetJobNo() + "，类型" + record.getTalkType());

        return true;
    }

    private boolean canModifyRecord(User operator, TalkRecord record) {
        if ("ADMIN_GLOBAL".equals(operator.getRole()) || "admin".equals(operator.getJobNo())) return true;
        return operator.getJobNo().equals(record.getTalkerJobNo());
    }

    /**
     * 获取当前用户可见的下属工号集合（含自己）。
     * 系统管理员/局领导返回 null 表示不限制。
     */
    public Set<String> getVisibleSubordinateJobNos() {
        User user = SecurityUtils.getCurrentUser();
        if (user == null) return Set.of();
        if (SecurityUtils.canViewGlobal()) return null;
        Set<String> result = new HashSet<>();
        result.add(user.getJobNo());
        collectSubordinates(user.getJobNo(), userRepository.findAll(), result);
        return result;
    }

    private void collectSubordinates(String jobNo, List<User> allUsers, Set<String> result) {
        for (User u : allUsers) {
            if (jobNo.equals(u.getSuperiorJobNo())) {
                result.add(u.getJobNo());
                collectSubordinates(u.getJobNo(), allUsers, result);
            }
        }
    }
}
