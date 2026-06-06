package com.company.service;

import com.company.dto.HomeVisitRequest;
import com.company.entity.HomeVisit;
import com.company.entity.User;
import com.company.repository.HomeVisitRepository;
import com.company.repository.UserRepository;
import com.company.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HomeVisitService {

    private final HomeVisitRepository homeVisitRepository;
    private final UserRepository userRepository;
    private final HierarchyService hierarchyService;
    private final FileUploadService fileUploadService;
    private final AuditLogService auditLogService;

    public HomeVisitService(HomeVisitRepository homeVisitRepository,
                            UserRepository userRepository,
                            HierarchyService hierarchyService,
                            FileUploadService fileUploadService,
                            AuditLogService auditLogService) {
        this.homeVisitRepository = homeVisitRepository;
        this.userRepository = userRepository;
        this.hierarchyService = hierarchyService;
        this.fileUploadService = fileUploadService;
        this.auditLogService = auditLogService;
    }

    public List<Map<String, Object>> getHomeVisits(String targetJobNo, String visitType, String startDate, String endDate) {
        Map<String, String> userNames = new HashMap<>();
        userRepository.findAll().forEach(u -> userNames.put(u.getJobNo(), u.getName()));

        LocalDateTime start = (startDate != null && !startDate.isEmpty())
                ? LocalDate.parse(startDate).atStartOfDay() : null;
        LocalDateTime end = (endDate != null && !endDate.isEmpty())
                ? LocalDate.parse(endDate).plusDays(1).atStartOfDay() : null;

        boolean hasTarget = targetJobNo != null && !targetJobNo.isEmpty();
        boolean hasType = visitType != null && !visitType.isEmpty();
        boolean hasTimeRange = start != null && end != null;

        List<HomeVisit> homeVisits;
        if (hasTarget && hasType && hasTimeRange) {
            homeVisits = homeVisitRepository.findByTargetJobNoAndVisitTypeAndVisitTimeBetweenOrderByVisitTimeDesc(targetJobNo, visitType, start, end);
        } else if (hasTarget && hasTimeRange) {
            homeVisits = homeVisitRepository.findByTargetJobNoAndVisitTimeBetweenOrderByVisitTimeDesc(targetJobNo, start, end);
        } else if (hasType && hasTimeRange) {
            homeVisits = homeVisitRepository.findByVisitTypeAndVisitTimeBetweenOrderByVisitTimeDesc(visitType, start, end);
        } else if (hasTimeRange) {
            homeVisits = homeVisitRepository.findByVisitTimeBetweenOrderByVisitTimeDesc(start, end);
        } else {
            homeVisits = homeVisitRepository.findAll().stream()
                    .filter(v -> !hasTarget || v.getTargetJobNo().equals(targetJobNo))
                    .filter(v -> !hasType || visitType.equals(v.getVisitType()))
                    .sorted((a, b) -> {
                        if (a.getVisitTime() == null && b.getVisitTime() == null) return 0;
                        if (a.getVisitTime() == null) return 1;
                        if (b.getVisitTime() == null) return -1;
                        return b.getVisitTime().compareTo(a.getVisitTime());
                    })
                    .collect(Collectors.toList());
        }

        // 按权限过滤：只能看到自己下属的记录（系统管理员/局领导看全部）
        Set<String> visibleJobNos = getVisibleSubordinateJobNos();
        if (visibleJobNos != null) {
            homeVisits = homeVisits.stream()
                    .filter(v -> visibleJobNos.contains(v.getTargetJobNo()))
                    .collect(Collectors.toList());
        }

        return homeVisits.stream().map(visit -> {
            Map<String, Object> response = new HashMap<>();
            response.put("id", visit.getId());
            response.put("operatorJobNo", visit.getOperatorJobNo());
            response.put("operatorName", userNames.getOrDefault(visit.getOperatorJobNo(), visit.getOperatorJobNo()));
            response.put("targetJobNo", visit.getTargetJobNo());
            response.put("targetName", userNames.getOrDefault(visit.getTargetJobNo(), visit.getTargetJobNo()));
            response.put("visitTime", visit.getVisitTime());
            response.put("visitType", visit.getVisitType());
            response.put("location", visit.getLocation());
            response.put("content", visit.getContent());
            response.put("photo", visit.getPhoto());
            return response;
        }).collect(Collectors.toList());
    }

    public HomeVisit addHomeVisit(String operatorJobNo, String targetJobNo,
                                  LocalDateTime visitTime, String visitType, String location,
                                  String content, MultipartFile photoFile) throws IOException {
        hierarchyService.validateHierarchy(operatorJobNo, targetJobNo);

        HomeVisit homeVisit = new HomeVisit();
        homeVisit.setOperatorJobNo(operatorJobNo);
        homeVisit.setTargetJobNo(targetJobNo);
        homeVisit.setVisitTime(visitTime);
        homeVisit.setVisitType(visitType);
        homeVisit.setLocation(location);
        homeVisit.setContent(content);

        if (photoFile != null && !photoFile.isEmpty()) {
            String photoPath = fileUploadService.uploadFile(photoFile);
            homeVisit.setPhoto(photoPath);
        }

        HomeVisit saved = homeVisitRepository.save(homeVisit);

        auditLogService.log("ADD_HOME_VISIT", "HOME_VISIT", saved.getId().toString(),
                "新增家访记录：对象" + targetJobNo + "，类型" + visitType);

        return saved;
    }

    public HomeVisit addHomeVisitByJson(HomeVisitRequest request) {
        hierarchyService.validateHierarchy(request.getOperatorJobNo(), request.getTargetJobNo());

        HomeVisit homeVisit = new HomeVisit();
        homeVisit.setOperatorJobNo(request.getOperatorJobNo());
        homeVisit.setTargetJobNo(request.getTargetJobNo());
        homeVisit.setVisitTime(request.getVisitTime());
        homeVisit.setVisitType(request.getVisitType());
        homeVisit.setLocation(request.getLocation());
        homeVisit.setContent(request.getContent());

        HomeVisit saved = homeVisitRepository.save(homeVisit);

        auditLogService.log("ADD_HOME_VISIT", "HOME_VISIT", saved.getId().toString(),
                "新增家访记录：对象" + request.getTargetJobNo() + "，类型" + request.getVisitType());

        return saved;
    }

    public HomeVisit updateHomeVisit(Long id, HomeVisitRequest request) {
        Optional<HomeVisit> optionalHomeVisit = homeVisitRepository.findById(id);
        if (optionalHomeVisit.isEmpty()) return null;

        HomeVisit homeVisit = optionalHomeVisit.get();

        User operator = SecurityUtils.getCurrentUser();
        if (operator == null || !canModifyRecord(operator, homeVisit)) {
            throw new SecurityException("无权限修改此家访记录");
        }

        homeVisit.setVisitTime(request.getVisitTime());
        homeVisit.setVisitType(request.getVisitType());
        homeVisit.setLocation(request.getLocation());
        homeVisit.setContent(request.getContent());
        HomeVisit saved = homeVisitRepository.save(homeVisit);

        auditLogService.log("UPDATE_HOME_VISIT", "HOME_VISIT", id.toString(),
                "修改家访记录：对象" + request.getTargetJobNo());

        return saved;
    }

    public boolean deleteHomeVisit(Long id) {
        Optional<HomeVisit> optionalHomeVisit = homeVisitRepository.findById(id);
        if (optionalHomeVisit.isEmpty()) return false;

        HomeVisit visit = optionalHomeVisit.get();

        User operator = SecurityUtils.getCurrentUser();
        if (operator == null || !canModifyRecord(operator, visit)) {
            throw new SecurityException("无权限删除此家访记录");
        }

        homeVisitRepository.delete(visit);

        auditLogService.log("DELETE_HOME_VISIT", "HOME_VISIT", id.toString(),
                "删除家访记录：对象" + visit.getTargetJobNo() + "，类型" + visit.getVisitType());

        return true;
    }

    private boolean canModifyRecord(User operator, HomeVisit visit) {
        if ("ADMIN_GLOBAL".equals(operator.getRole()) || "admin".equals(operator.getJobNo())) return true;
        return operator.getJobNo().equals(visit.getOperatorJobNo());
    }

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

    public Map<String, Object> getHomeVisitById(Long id) {
        HomeVisit visit = homeVisitRepository.findById(id).orElse(null);
        if (visit == null) return null;

        // 访问控制：只能查看自己下属的记录详情
        Set<String> visibleJobNos = getVisibleSubordinateJobNos();
        if (visibleJobNos != null && !visibleJobNos.contains(visit.getTargetJobNo())) {
            throw new SecurityException("无权查看此家访记录");
        }

        User operator = userRepository.findByJobNo(visit.getOperatorJobNo());
        String operatorName = operator != null ? operator.getName() : visit.getOperatorJobNo();
        User target = userRepository.findByJobNo(visit.getTargetJobNo());
        String targetName = target != null ? target.getName() : visit.getTargetJobNo();

        Map<String, Object> response = new HashMap<>();
        response.put("id", visit.getId());
        response.put("operatorJobNo", visit.getOperatorJobNo());
        response.put("operatorName", operatorName);
        response.put("targetJobNo", visit.getTargetJobNo());
        response.put("targetName", targetName);
        response.put("visitTime", visit.getVisitTime());
        response.put("visitType", visit.getVisitType());
        response.put("location", visit.getLocation());
        response.put("content", visit.getContent());
        response.put("photo", visit.getPhoto());
        return response;
    }
}
