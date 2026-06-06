package com.company.service;

import com.company.dto.UserDto;
import com.company.entity.*;
import com.company.repository.*;
import com.company.security.SecurityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PermissionRepository permissionRepository;
    private final HierarchyHistoryRepository hierarchyHistoryRepository;
    private final ViolationRecordRepository violationRecordRepository;
    private final AuditLogService auditLogService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       DepartmentRepository departmentRepository,
                       PermissionRepository permissionRepository,
                       HierarchyHistoryRepository hierarchyHistoryRepository,
                       ViolationRecordRepository violationRecordRepository,
                       AuditLogService auditLogService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.permissionRepository = permissionRepository;
        this.hierarchyHistoryRepository = hierarchyHistoryRepository;
        this.violationRecordRepository = violationRecordRepository;
        this.auditLogService = auditLogService;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public List<Map<String, Object>> getUsers(Integer deptId, String riskLevel) {
        User loginUser = SecurityUtils.getCurrentUser();
        List<User> allUsers = userRepository.findAll();

        if (!SecurityUtils.isAdminGlobal() && !SecurityUtils.hasPermission("PERSONNEL_MANAGE")
                && !SecurityUtils.hasPermission("HIERARCHY_MANAGE")) {
            if (loginUser != null && loginUser.getDepartment() != null) {
                deptId = loginUser.getDepartment().getId();
            }
        }

        if (deptId != null) {
            Integer finalDeptId = deptId;
            allUsers = allUsers.stream()
                    .filter(u -> u.getDepartment() != null && u.getDepartment().getId().equals(finalDeptId))
                    .toList();
        }

        if (riskLevel != null && !riskLevel.isEmpty()) {
            String targetLevel = riskLevel.toUpperCase();
            allUsers = allUsers.stream()
                    .filter(u -> targetLevel.equals(u.getRiskLevel()))
                    .toList();
        }

        return allUsers.stream().map(UserDto::fromEntity).toList();
    }

    public Map<String, Object> updateUser(String jobNo, Map<String, Object> updates) {
        User user = userRepository.findByJobNo(jobNo);
        if (user == null) return null;

        User operator = SecurityUtils.getCurrentUser();
        if (operator == null) throw new IllegalArgumentException("操作人不存在");
        if (!canManageUser(operator, user)) {
            throw new SecurityException("无权限修改该人员信息");
        }

        if (updates.containsKey("name")) user.setName((String) updates.get("name"));
        if (updates.containsKey("position")) user.setPosition((String) updates.get("position"));
        if (updates.containsKey("role")) {
            String targetRole = (String) updates.get("role");
            if (!canAssignRole(operator, targetRole)) {
                throw new SecurityException("无权限设置该角色");
            }
            user.setRole(targetRole);
            syncPermissionsForRole(user);
        }
        if (updates.containsKey("riskLevel")) user.setRiskLevel((String) updates.get("riskLevel"));
        if (updates.containsKey("isKeyPersonnel")) user.setIsKeyPersonnel((Boolean) updates.get("isKeyPersonnel"));

        userRepository.save(user);

        auditLogService.log("UPDATE_USER", "USER", jobNo,
                "更新用户信息：" + user.getName() + "（" + jobNo + "），字段：" + updates.keySet());

        return UserDto.fromEntity(user);
    }

    @Transactional
    public Map<String, Object> transferUser(String jobNo, Integer newDeptId) {
        User user = userRepository.findByJobNo(jobNo);
        Department newDept = departmentRepository.findById(newDeptId).orElse(null);
        User admin = SecurityUtils.getCurrentUser();

        if (user == null || newDept == null) throw new IllegalArgumentException("用户或部门不存在");
        if (admin == null) throw new IllegalArgumentException("操作人不存在");
        if (!canManageUser(admin, user)) {
            throw new SecurityException("无权限调动该人员");
        }
        if ("ADMIN_UNIT".equals(admin.getRole())) {
            if (admin.getDepartment() == null || !admin.getDepartment().getId().equals(newDeptId)) {
                throw new IllegalArgumentException("单位管理员只能调动到本部门");
            }
        }
        if (user.getDepartment() != null && user.getDepartment().getId().equals(newDeptId)) {
            throw new IllegalArgumentException("该人员已在目标部门，无需重复调动");
        }

        String oldDeptName = user.getDepartment() != null ? user.getDepartment().getDeptName() : "无";
        LocalDateTime now = LocalDateTime.now();

        closeCurrentHierarchyRecord(user, now);
        user.setDepartment(newDept);
        user.setSuperiorJobNo(null);
        if ("ADMIN_UNIT".equals(user.getRole())) {
            user.setRole("USER");
        }
        userRepository.save(user);
        syncPermissionsForRole(user);
        createOrRefreshActiveHierarchyRecord(user, now);

        auditLogService.log("TRANSFER_USER", "USER", jobNo,
                "调动用户：" + user.getName() + "（" + jobNo + "），从" + oldDeptName + "调至" + newDept.getDeptName());

        Map<String, Object> result = new HashMap<>();
        result.put("message", "调动成功");
        result.put("user", UserDto.fromEntity(user));
        return result;
    }

    @Transactional
    public Map<String, Object> assignSuperior(String jobNo, String superiorJobNo) {
        User user = userRepository.findByJobNo(jobNo);
        if (user == null) throw new IllegalArgumentException("用户不存在");
        User admin = SecurityUtils.getCurrentUser();
        if (admin == null) throw new IllegalArgumentException("操作人不存在");
        if (!canManageUser(admin, user)) {
            throw new SecurityException("无权限分配该人员上级");
        }

        if (superiorJobNo != null && !superiorJobNo.isBlank()) {
            User superior = userRepository.findByJobNo(superiorJobNo);
            if (superior == null) throw new IllegalArgumentException("上级人员不存在");
            if (user.getDepartment() == null || superior.getDepartment() == null
                    || !user.getDepartment().getId().equals(superior.getDepartment().getId())) {
                throw new IllegalArgumentException("只能分配同部门人员作为直接上级");
            }
            if (jobNo.equals(superiorJobNo)) {
                throw new IllegalArgumentException("不能将自己设为上级");
            }
            if (!canManageUser(admin, superior)) {
                throw new SecurityException("无权限选择该上级");
            }
        }

        String normalizedSuperiorJobNo = (superiorJobNo == null || superiorJobNo.isBlank()) ? null : superiorJobNo;
        boolean superiorChanged = !Objects.equals(user.getSuperiorJobNo(), normalizedSuperiorJobNo);
        if (!superiorChanged) return UserDto.fromEntity(user);

        boolean firstAssignForCurrentRecord = (user.getSuperiorJobNo() == null || user.getSuperiorJobNo().isBlank())
                && normalizedSuperiorJobNo != null;

        if (!firstAssignForCurrentRecord) {
            closeCurrentHierarchyRecord(user, LocalDateTime.now());
            user.setCreateTime(LocalDateTime.now());
        }

        user.setSuperiorJobNo(normalizedSuperiorJobNo);
        userRepository.save(user);
        createOrRefreshActiveHierarchyRecord(user, user.getCreateTime());

        String superiorInfo = normalizedSuperiorJobNo != null ? normalizedSuperiorJobNo : "无";
        auditLogService.log("ASSIGN_SUPERIOR", "USER", jobNo,
                "设置上级：" + user.getName() + "（" + jobNo + "）的上级设为" + superiorInfo);

        return UserDto.fromEntity(user);
    }

    public List<HierarchyHistory> getHierarchyHistory(String jobNo) {
        return hierarchyHistoryRepository.findByTargetJobNoOrderByStartDateDesc(jobNo);
    }

    public List<ViolationRecord> getViolationRecords(String jobNo) {
        return violationRecordRepository.findByTargetJobNoOrderByViolationTimeDesc(jobNo);
    }

    public ViolationRecord addViolationRecord(Map<String, Object> request) {
        ViolationRecord record = new ViolationRecord();
        record.setTargetJobNo((String) request.get("targetJobNo"));
        record.setReason((String) request.get("reason"));
        record.setPunishment((String) request.get("punishment"));

        User operator = SecurityUtils.getCurrentUser();
        if (operator == null) throw new IllegalArgumentException("操作人不存在");
        record.setCreatedBy(operator.getJobNo());

        if (request.containsKey("violationTime")) {
            try {
                String timeStr = (String) request.get("violationTime");
                record.setViolationTime(LocalDateTime.parse(timeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } catch (Exception e) {
                record.setViolationTime(LocalDateTime.now());
            }
        } else {
            record.setViolationTime(LocalDateTime.now());
        }

        violationRecordRepository.save(record);
        return record;
    }

    public Map<String, Object> createUser(Map<String, Object> request) {
        String jobNo = (String) request.get("jobNo");
        String name = (String) request.get("name");
        String position = (String) request.get("position");
        String password = (String) request.get("password");
        Object deptIdObj = request.get("deptId");
        if (deptIdObj == null) throw new IllegalArgumentException("部门ID不能为空");
        Integer deptId = deptIdObj instanceof Integer ? (Integer) deptIdObj
                : Integer.parseInt(deptIdObj.toString());

        if (jobNo == null || jobNo.isBlank()) throw new IllegalArgumentException("工号不能为空");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("姓名不能为空");
        if (position == null || position.isBlank()) throw new IllegalArgumentException("职位不能为空");
        if (password == null || password.length() < 6) throw new IllegalArgumentException("密码长度不能少于6位");

        if (userRepository.findByJobNo(jobNo) != null) {
            throw new IllegalArgumentException("工号已存在");
        }

        Department dept = departmentRepository.findById(deptId)
                .orElseThrow(() -> new IllegalArgumentException("部门不存在"));

        User operator = SecurityUtils.getCurrentUser();
        if (operator == null) throw new IllegalArgumentException("操作人不存在");
        if (!"ADMIN_GLOBAL".equals(operator.getRole()) && !"admin".equals(operator.getJobNo())) {
            if (!"ADMIN_UNIT".equals(operator.getRole()) || operator.getDepartment() == null
                    || !operator.getDepartment().getId().equals(deptId)) {
                throw new SecurityException("无权限在该部门创建用户");
            }
        }

        User user = new User();
        user.setJobNo(jobNo);
        user.setName(name);
        user.setPosition(position);
        user.setDepartment(dept);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("USER");
        user.setRiskLevel("NORMAL");
        user.setIsKeyPersonnel(false);
        user.setCreateTime(LocalDateTime.now());
        userRepository.save(user);

        auditLogService.log("CREATE_USER", "USER", jobNo,
                "创建用户：" + name + "（" + jobNo + "），部门：" + dept.getDeptName());

        return UserDto.fromEntity(user);
    }

    public void deleteUser(String jobNo) {
        User user = userRepository.findByJobNo(jobNo);
        if (user == null) throw new IllegalArgumentException("用户不存在");

        User operator = SecurityUtils.getCurrentUser();
        if (operator == null) throw new IllegalArgumentException("操作人不存在");
        if (!canManageUser(operator, user)) {
            throw new SecurityException("无权限删除该用户");
        }
        if (jobNo.equals(operator.getJobNo())) {
            throw new IllegalArgumentException("不能删除自己");
        }

        userRepository.delete(user);

        auditLogService.log("DELETE_USER", "USER", jobNo,
                "删除用户：" + user.getName() + "（" + jobNo + "）");
    }

    @Transactional
    public Map<String, Object> batchImportUsers(MultipartFile file) {
        User operator = SecurityUtils.getCurrentUser();
        if (operator == null) throw new IllegalArgumentException("操作人不存在");
        if (!SecurityUtils.isAdminGlobal() && !operator.hasPermission("PERSONNEL_MANAGE")
                && !operator.hasPermission("HIERARCHY_MANAGE")) {
            throw new SecurityException("无权限批量导入用户");
        }

        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int skipCount = 0;

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            // 预加载部门
            List<Department> allDepts = departmentRepository.findAll();
            Map<String, Department> deptMap = new HashMap<>();
            for (Department d : allDepts) {
                deptMap.put(d.getDeptName(), d);
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String jobNo = getCellStringValue(row, 0);
                String name = getCellStringValue(row, 1);
                String deptName = getCellStringValue(row, 2);
                String position = getCellStringValue(row, 3);
                String phone = getCellStringValue(row, 4);
                String superiorJobNo = getCellStringValue(row, 5);

                if (jobNo.isBlank() || name.isBlank()) {
                    errors.add("第" + (i + 1) + "行：警号或姓名为空，已跳过");
                    skipCount++;
                    continue;
                }

                if (userRepository.findByJobNo(jobNo) != null) {
                    errors.add("第" + (i + 1) + "行：警号 " + jobNo + " 已存在，已跳过");
                    skipCount++;
                    continue;
                }

                Department dept = null;
                if (!deptName.isBlank()) {
                    dept = deptMap.get(deptName);
                    if (dept == null) {
                        errors.add("第" + (i + 1) + "行：部门「" + deptName + "」不存在，已跳过");
                        skipCount++;
                        continue;
                    }
                }

                User user = new User();
                user.setJobNo(jobNo);
                user.setName(name);
                user.setPosition(position.isBlank() ? "组员" : position);
                user.setDepartment(dept);
                user.setSuperiorJobNo(superiorJobNo.isBlank() ? null : superiorJobNo);
                user.setPhone(phone.isBlank() ? null : phone);
                user.setPassword(passwordEncoder.encode(jobNo));
                user.setRole("USER");
                user.setRiskLevel("NORMAL");
                user.setIsKeyPersonnel(false);
                user.setCreateTime(LocalDateTime.now());
                userRepository.save(user);
                successCount++;
            }
        } catch (Exception e) {
            throw new RuntimeException("读取Excel文件失败：" + e.getMessage());
        }

        auditLogService.log("BATCH_IMPORT", "USER", operator.getJobNo(),
                "批量导入用户：成功" + successCount + "条，跳过" + skipCount + "条");

        Map<String, Object> result = new HashMap<>();
        result.put("success", successCount);
        result.put("skipped", skipCount);
        result.put("errors", errors);
        return result;
    }

    private String getCellStringValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return "";
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }

    private void closeCurrentHierarchyRecord(User user, LocalDateTime endDate) {
        HierarchyHistory activeHistory = hierarchyHistoryRepository
                .findFirstByTargetJobNoAndEndDateIsNullOrderByStartDateDesc(user.getJobNo())
                .orElse(null);

        if (activeHistory != null) {
            fillHierarchyHistory(activeHistory, user, activeHistory.getStartDate());
            activeHistory.setEndDate(endDate);
            hierarchyHistoryRepository.save(activeHistory);
            return;
        }
        saveHierarchySnapshot(user, endDate);
    }

    private void saveHierarchySnapshot(User user, LocalDateTime endDate) {
        HierarchyHistory history = new HierarchyHistory();
        fillHierarchyHistory(history, user, user.getCreateTime());
        history.setEndDate(endDate);
        hierarchyHistoryRepository.save(history);
    }

    private void createOrRefreshActiveHierarchyRecord(User user, LocalDateTime startDate) {
        HierarchyHistory activeHistory = hierarchyHistoryRepository
                .findFirstByTargetJobNoAndEndDateIsNullOrderByStartDateDesc(user.getJobNo())
                .orElse(new HierarchyHistory());

        fillHierarchyHistory(activeHistory, user, startDate);
        activeHistory.setEndDate(null);
        hierarchyHistoryRepository.save(activeHistory);
    }

    private void fillHierarchyHistory(HierarchyHistory history, User user, LocalDateTime startDate) {
        history.setTargetJobNo(user.getJobNo());
        history.setStartDate(startDate != null ? startDate : LocalDateTime.now());
        history.setUnitName(user.getDepartment() != null ? user.getDepartment().getDeptName() : null);
        history.setManagerJobNo(null);
        history.setManagerName(null);

        if (user.getSuperiorJobNo() != null && !user.getSuperiorJobNo().isBlank()) {
            User superior = userRepository.findByJobNo(user.getSuperiorJobNo());
            if (superior != null) {
                history.setManagerJobNo(superior.getJobNo());
                history.setManagerName(superior.getName());
            }
        }
    }

    private boolean canManageUser(User operator, User target) {
        if (operator == null || target == null) return false;
        if ("ADMIN_GLOBAL".equals(operator.getRole()) || "admin".equals(operator.getJobNo())) return true;
        if (operator.hasPermission("PERSONNEL_MANAGE")) return true;
        if (operator.hasPermission("HIERARCHY_MANAGE")) {
            if (operator.getDepartment() == null || target.getDepartment() == null) return false;
            return operator.getDepartment().getId().equals(target.getDepartment().getId());
        }
        return false;
    }

    private boolean canAssignRole(User operator, String targetRole) {
        if (operator == null) return false;
        if ("ADMIN_GLOBAL".equals(operator.getRole()) || "admin".equals(operator.getJobNo())) return true;
        return !"ADMIN_UNIT".equals(targetRole) && !"ADMIN_GLOBAL".equals(targetRole);
    }

    private void syncPermissionsForRole(User user) {
        if ("ADMIN_GLOBAL".equals(user.getRole())) {
            Set<Permission> allPerms = new HashSet<>(permissionRepository.findAll());
            user.setPermissions(allPerms);
            userRepository.save(user);
        } else if ("ADMIN_UNIT".equals(user.getRole())) {
            Permission hierarchy = permissionRepository.findByCode("HIERARCHY_MANAGE").orElse(null);
            Permission reports = permissionRepository.findByCode("STAT_REPORTS").orElse(null);
            Set<Permission> perms = new HashSet<>();
            if (hierarchy != null) perms.add(hierarchy);
            if (reports != null) perms.add(reports);
            user.setPermissions(perms);
            userRepository.save(user);
        } else {
            user.setPermissions(new HashSet<>());
            userRepository.save(user);
        }
    }
}
