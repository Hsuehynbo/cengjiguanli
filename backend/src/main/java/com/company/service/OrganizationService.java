package com.company.service;

import com.company.entity.*;
import com.company.repository.*;
import com.company.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrganizationService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final TalkRecordRepository talkRecordRepository;
    private final HomeVisitRepository homeVisitRepository;
    private final ActivityTaskRepository activityTaskRepository;

    public OrganizationService(DepartmentRepository departmentRepository,
                               UserRepository userRepository,
                               TalkRecordRepository talkRecordRepository,
                               HomeVisitRepository homeVisitRepository,
                               ActivityTaskRepository activityTaskRepository) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.talkRecordRepository = talkRecordRepository;
        this.homeVisitRepository = homeVisitRepository;
        this.activityTaskRepository = activityTaskRepository;
    }

    public Map<String, Object> getOrganizationTree(Integer deptId) {
        User loginUser = SecurityUtils.getCurrentUser();
        List<User> allUsers = userRepository.findAll();

        if (deptId != null) {
            Department dept = departmentRepository.findById(deptId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "部门不存在"));
            ensureDepartmentAccess(deptId);
            if (SecurityUtils.canViewGlobal() || SecurityUtils.isAdminUnit() || SecurityUtils.isDepartmentHead()) {
                return buildDepartmentTree(dept, allUsers);
            } else {
                return buildUserSubTree(loginUser, allUsers);
            }
        }

        if (SecurityUtils.canViewGlobal()) {
            return buildFullTree(allUsers);
        }

        if (loginUser.getDepartment() != null) {
            if (SecurityUtils.isAdminUnit() || SecurityUtils.isDepartmentHead()) {
                return buildDepartmentTree(loginUser.getDepartment(), allUsers);
            } else {
                return buildUserSubTree(loginUser, allUsers);
            }
        }

        return buildUserSubTree(loginUser, allUsers);
    }

    public List<Map<String, Object>> getDepartments() {
        User loginUser = SecurityUtils.getCurrentUser();
        List<Department> departments;
        if (SecurityUtils.canViewGlobal()) {
            departments = departmentRepository.findAll();
        } else if (loginUser.getDepartment() != null) {
            departments = List.of(loginUser.getDepartment());
        } else {
            departments = Collections.emptyList();
        }

        List<User> allUsers = userRepository.findAll();
        return departments.stream()
                .map(dept -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", dept.getId());
                    item.put("name", dept.getDeptName());
                    item.put("userCount", countDepartmentUsers(dept.getId(), allUsers));
                    return item;
                })
                .sorted(Comparator.comparing(item -> item.get("name").toString()))
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getChildren(String id) {
        List<Map<String, Object>> children = new ArrayList<>();
        if (id.startsWith("dept_")) {
            int deptId = Integer.parseInt(id.substring(5));
            ensureDepartmentAccess(deptId);
            List<User> users = userRepository.findAll();
            for (User user : users) {
                if (user.getDepartment() != null && user.getDepartment().getId().equals(deptId)) {
                    children.add(buildUserNode(user));
                }
            }
        } else if (id.startsWith("user_")) {
            String jobNo = id.substring(5);
            if (!SecurityUtils.canViewGlobal()) {
                User loginUser = SecurityUtils.getCurrentUser();
                if (loginUser == null || !loginUser.getJobNo().equals(jobNo)) {
                    Set<String> visible = new HashSet<>();
                    visible.add(loginUser.getJobNo());
                    collectSubordinates(loginUser.getJobNo(), userRepository.findAll(), visible);
                    if (!visible.contains(jobNo)) {
                        return children;
                    }
                }
            }
            List<User> subordinates = userRepository.findBySuperiorJobNo(jobNo);
            for (User subordinate : subordinates) {
                children.add(buildUserNode(subordinate));
            }
        }
        return children;
    }

    public List<Map<String, Object>> searchUsers(String keyword) {
        User loginUser = SecurityUtils.getCurrentUser();
        List<User> users = userRepository.findByNameOrJobNo(keyword);

        // 按权限过滤：系统管理员看全部，其他人只能搜到自己和下属
        if (SecurityUtils.canViewGlobal()) {
            return users.stream().map(this::buildUserNode).collect(Collectors.toList());
        }
        Set<String> visibleJobNos = new HashSet<>();
        visibleJobNos.add(loginUser.getJobNo());
        collectSubordinates(loginUser.getJobNo(), userRepository.findAll(), visibleJobNos);
        return users.stream()
                .filter(u -> visibleJobNos.contains(u.getJobNo()))
                .map(this::buildUserNode)
                .collect(Collectors.toList());
    }

    private void collectSubordinates(String jobNo, List<User> allUsers, Set<String> result) {
        for (User u : allUsers) {
            if (jobNo.equals(u.getSuperiorJobNo())) {
                result.add(u.getJobNo());
                collectSubordinates(u.getJobNo(), allUsers, result);
            }
        }
    }

    public Map<String, Object> getUserInfo(String jobNo) {
        User user = userRepository.findByJobNo(jobNo);
        Map<String, Object> userNode = new HashMap<>();
        if (user != null) {
            userNode.putAll(buildUserNode(user));
            userNode.put("role", user.getRole());
            if (user.getDepartment() != null) {
                Map<String, Object> deptInfo = new HashMap<>();
                deptInfo.put("id", user.getDepartment().getId());
                deptInfo.put("deptName", user.getDepartment().getDeptName());
                userNode.put("department", deptInfo);
            }
        }
        return userNode;
    }

    public Map<String, Object> getDepartmentStats(Integer deptId) {
        ensureDepartmentAccess(deptId);
        Department dept = departmentRepository.findById(deptId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "部门不存在"));

        Map<String, Object> result = new HashMap<>();
        List<User> users = getUsersByDepartment(deptId, userRepository.findAll());
        Set<String> completedUserJobNos = getCurrentMonthTalkTargets();
        Set<String> completedHomeVisitJobNos = getCurrentHalfYearVisitTargets();

        Map<String, Object> overview = buildDepartmentOverview(dept, users, completedUserJobNos, completedHomeVisitJobNos);
        List<Map<String, Object>> userList = users.stream().map(u -> {
            Map<String, Object> map = buildUserNode(u);
            map.put("completed", completedUserJobNos.contains(u.getJobNo()));
            map.put("homeVisitCompleted", completedHomeVisitJobNos.contains(u.getJobNo()));
            return map;
        }).sorted(Comparator.comparing(item -> {
            Object pos = item.get("position");
            return pos != null ? pos.toString() : "";
        }))
                .collect(Collectors.toList());

        result.put("overview", overview);
        result.put("userList", userList);
        return result;
    }

    public Map<String, Object> getDashboard() {
        User loginUser = SecurityUtils.getCurrentUser();
        List<User> allUsers = userRepository.findAll();
        List<Department> visibleDepartments;

        if (SecurityUtils.canViewGlobal()) {
            visibleDepartments = departmentRepository.findAll();
        } else if (loginUser.getDepartment() != null) {
            visibleDepartments = List.of(loginUser.getDepartment());
        } else {
            visibleDepartments = Collections.emptyList();
        }

        List<User> visibleUsers = allUsers.stream()
                .filter(u -> SecurityUtils.canViewGlobal()
                        || (u.getDepartment() != null && visibleDepartments.stream()
                                .anyMatch(d -> d.getId().equals(u.getDepartment().getId()))))
                .collect(Collectors.toList());

        Set<String> talkTargets = getCurrentMonthTalkTargets();
        Set<String> visitTargets = getCurrentHalfYearVisitTargets();
        List<Map<String, Object>> deptStats = visibleDepartments.stream()
                .map(dept -> buildDepartmentOverview(dept, getUsersByDepartment(dept.getId(), allUsers), talkTargets, visitTargets))
                .sorted(Comparator.comparingInt(item -> -((Number) item.get("total")).intValue()))
                .collect(Collectors.toList());

        Map<String, Object> summary = new HashMap<>();
        int totalUsers = deptStats.stream().mapToInt(item -> ((Number) item.get("total")).intValue()).sum();
        int talkCompleted = deptStats.stream().mapToInt(item -> ((Number) item.get("completed")).intValue()).sum();
        int homeVisitCompleted = deptStats.stream().mapToInt(item -> ((Number) item.get("homeVisitCompleted")).intValue()).sum();
        int keyCount = deptStats.stream().mapToInt(item -> ((Number) item.get("keyCount")).intValue()).sum();
        int riskCount = deptStats.stream().mapToInt(item -> ((Number) item.get("riskCount")).intValue()).sum();
        int attentionCount = deptStats.stream().mapToInt(item -> ((Number) item.get("attentionCount")).intValue()).sum();

        summary.put("deptCount", deptStats.size());
        summary.put("totalUsers", totalUsers);
        summary.put("talkCompleted", talkCompleted);
        summary.put("talkPending", Math.max(totalUsers - talkCompleted, 0));
        summary.put("talkRate", totalUsers == 0 ? 0 : roundPercent((double) talkCompleted / totalUsers * 100));
        int currentMonthHomeVisitCount = getCurrentMonthHomeVisitCount();
        summary.put("homeVisitCompleted", homeVisitCompleted);
        summary.put("homeVisitPending", Math.max(totalUsers - homeVisitCompleted, 0));
        summary.put("homeVisitRate", totalUsers == 0 ? 0 : roundPercent((double) homeVisitCompleted / totalUsers * 100));
        summary.put("homeVisitCount", currentMonthHomeVisitCount);
        summary.put("keyCount", keyCount);
        summary.put("riskCount", riskCount);
        summary.put("attentionCount", attentionCount);

        long activeTasks = activityTaskRepository.countByStatus("ACTIVE");
        long closedTasks = activityTaskRepository.countByStatus("CLOSED");
        summary.put("activeTasks", activeTasks);
        summary.put("closedTasks", closedTasks);
        summary.put("totalTasks", activeTasks + closedTasks);

        List<Map<String, Object>> userList = visibleUsers.stream().map(u -> {
            Map<String, Object> map = buildUserNode(u);
            if (u.getDepartment() != null) {
                map.put("deptName", u.getDepartment().getDeptName());
            }
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("summary", summary);
        result.put("departments", deptStats);
        result.put("userList", userList);
        return result;
    }

    private Map<String, Object> buildFullTree(List<User> allUsers) {
        Map<String, Object> root = new HashMap<>();
        root.put("id", "root");
        root.put("name", "云和县公安局");
        root.put("type", "company");
        root.put("children", new ArrayList<>());

        List<Department> departments = departmentRepository.findAll();
        List<Map<String, Object>> children = new ArrayList<>();
        for (Department dept : departments) {
            children.add(buildDepartmentTree(dept, allUsers));
        }

        root.put("children", children);
        root.put("deptCount", departments.size());
        root.put("userCount", allUsers.size());
        return root;
    }

    private Map<String, Object> buildDepartmentTree(Department dept, List<User> allUsers) {
        Map<String, Object> deptNode = new HashMap<>();
        deptNode.put("id", "dept_" + dept.getId());
        deptNode.put("deptId", dept.getId());
        deptNode.put("name", dept.getDeptName());
        deptNode.put("type", "department");

        List<User> deptUsers = allUsers.stream()
                .filter(u -> u.getDepartment() != null && u.getDepartment().getId().equals(dept.getId()))
                .collect(Collectors.toList());
        deptNode.put("userCount", deptUsers.size());

        Map<String, Map<String, Object>> userMap = new HashMap<>();
        List<Map<String, Object>> deptRootUsers = new ArrayList<>();

        for (User u : deptUsers) {
            Map<String, Object> userNode = buildUserNode(u);
            userNode.put("deptId", dept.getId());
            if (u.getDepartment() != null) {
                userNode.put("deptName", u.getDepartment().getDeptName());
            }
            userMap.put(u.getJobNo(), userNode);
        }

        for (User u : deptUsers) {
            Map<String, Object> userNode = userMap.get(u.getJobNo());
            String superiorJobNo = u.getSuperiorJobNo();
            if (superiorJobNo != null && userMap.containsKey(superiorJobNo)) {
                Map<String, Object> parentNode = userMap.get(superiorJobNo);
                ((List<Map<String, Object>>) parentNode.get("children")).add(userNode);
            } else {
                deptRootUsers.add(userNode);
            }
        }

        deptNode.put("children", deptRootUsers);
        return deptNode;
    }

    private Map<String, Object> buildUserSubTree(User rootUser, List<User> allUsers) {
        Map<String, Object> userNode = buildUserNode(rootUser);
        List<User> subordinates = allUsers.stream()
                .filter(u -> rootUser.getJobNo().equals(u.getSuperiorJobNo()))
                .collect(Collectors.toList());
        for (User sub : subordinates) {
            ((List<Map<String, Object>>) userNode.get("children")).add(buildUserSubTree(sub, allUsers));
        }
        return userNode;
    }

    private Map<String, Object> buildUserNode(User user) {
        Map<String, Object> userNode = new HashMap<>();
        userNode.put("id", "user_" + user.getJobNo());
        userNode.put("name", user.getName());
        userNode.put("jobNo", user.getJobNo());
        userNode.put("position", user.getPosition());
        userNode.put("type", "user");
        userNode.put("isKeyPersonnel", user.getIsKeyPersonnel());
        userNode.put("riskLevel", normalizeRiskLevel(user));
        userNode.put("superiorJobNo", user.getSuperiorJobNo());
        userNode.put("phone", user.getPhone());
        if (user.getDepartment() != null) {
            userNode.put("deptName", user.getDepartment().getDeptName());
        }
        userNode.put("children", new ArrayList<>());
        return userNode;
    }

    private Map<String, Object> buildDepartmentOverview(Department dept, List<User> users,
                                                        Set<String> talkTargets, Set<String> visitTargets) {
        int total = users.size();
        int completed = (int) users.stream().filter(u -> talkTargets.contains(u.getJobNo())).count();
        int pending = total - completed;
        int homeVisitCompleted = (int) users.stream().filter(u -> visitTargets.contains(u.getJobNo())).count();
        int homeVisitPending = total - homeVisitCompleted;

        Map<String, Long> riskCounts = users.stream()
                .collect(Collectors.groupingBy(this::normalizeRiskLevel, Collectors.counting()));

        Map<String, Object> overview = new HashMap<>();
        overview.put("id", dept.getId());
        overview.put("name", dept.getDeptName());
        overview.put("total", total);
        overview.put("completed", completed);
        overview.put("pending", pending);
        overview.put("rate", total == 0 ? 0 : roundPercent((double) completed / total * 100));
        overview.put("homeVisitCompleted", homeVisitCompleted);
        overview.put("homeVisitPending", homeVisitPending);
        overview.put("homeVisitRate", total == 0 ? 0 : roundPercent((double) homeVisitCompleted / total * 100));
        overview.put("keyCount", riskCounts.getOrDefault("KEY", 0L));
        overview.put("riskCount", riskCounts.getOrDefault("RISK", 0L));
        overview.put("attentionCount", riskCounts.getOrDefault("ATTENTION", 0L));
        overview.put("normalCount", riskCounts.getOrDefault("NORMAL", 0L));
        return overview;
    }

    private Set<String> getCurrentMonthTalkTargets() {
        LocalDateTime firstDayOfMonth = getCurrentMonthStart();
        return talkRecordRepository.findAll().stream()
                .filter(record -> record.getTalkTime() != null && !record.getTalkTime().isBefore(firstDayOfMonth))
                .map(TalkRecord::getTargetJobNo)
                .collect(Collectors.toSet());
    }

    private Set<String> getCurrentHalfYearVisitTargets() {
        LocalDateTime firstDayOfHalfYear = getCurrentHalfYearStart();
        return homeVisitRepository.findAll().stream()
                .filter(record -> record.getVisitTime() != null && !record.getVisitTime().isBefore(firstDayOfHalfYear))
                .map(HomeVisit::getTargetJobNo)
                .collect(Collectors.toSet());
    }

    private int getCurrentMonthHomeVisitCount() {
        LocalDateTime firstDayOfMonth = getCurrentMonthStart();
        return (int) homeVisitRepository.findAll().stream()
                .filter(record -> record.getVisitTime() != null && !record.getVisitTime().isBefore(firstDayOfMonth))
                .count();
    }

    private LocalDateTime getCurrentMonthStart() {
        LocalDate now = LocalDate.now();
        return now.withDayOfMonth(1).atStartOfDay();
    }

    private LocalDateTime getCurrentHalfYearStart() {
        LocalDate now = LocalDate.now();
        Month startMonth = now.getMonthValue() <= 6 ? Month.JANUARY : Month.JULY;
        return now.with(startMonth).withDayOfMonth(1).atStartOfDay();
    }

    private void ensureDepartmentAccess(Integer deptId) {
        if (SecurityUtils.canViewGlobal()) return;
        if (!SecurityUtils.isInDepartment(deptId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权查看该部门");
        }
    }

    private List<User> getUsersByDepartment(Integer deptId, List<User> allUsers) {
        return allUsers.stream()
                .filter(u -> u.getDepartment() != null && u.getDepartment().getId().equals(deptId))
                .collect(Collectors.toList());
    }

    private int countDepartmentUsers(Integer deptId, List<User> allUsers) {
        return (int) allUsers.stream()
                .filter(u -> u.getDepartment() != null && u.getDepartment().getId().equals(deptId))
                .count();
    }

    private String normalizeRiskLevel(User user) {
        String riskLevel = user.getRiskLevel();
        return (riskLevel == null || riskLevel.isBlank()) ? "NORMAL" : riskLevel.toUpperCase();
    }

    private double roundPercent(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
