package com.company.service;

import com.company.entity.*;
import com.company.repository.*;
import com.company.security.SecurityUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ActivityTaskService {

    private final ActivityTaskRepository activityTaskRepository;
    private final TaskTargetRepository taskTargetRepository;
    private final ActivityRecordRepository activityRecordRepository;
    private final ActivityParticipantRepository activityParticipantRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public ActivityTaskService(ActivityTaskRepository activityTaskRepository,
                               TaskTargetRepository taskTargetRepository,
                               ActivityRecordRepository activityRecordRepository,
                               ActivityParticipantRepository activityParticipantRepository,
                               DepartmentRepository departmentRepository,
                               UserRepository userRepository,
                               NotificationService notificationService,
                               AuditLogService auditLogService) {
        this.activityTaskRepository = activityTaskRepository;
        this.taskTargetRepository = taskTargetRepository;
        this.activityRecordRepository = activityRecordRepository;
        this.activityParticipantRepository = activityParticipantRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public Map<String, Object> createTask(Map<String, Object> request) {
        User operator = SecurityUtils.getCurrentUser();
        if (operator == null) throw new IllegalArgumentException("操作人不存在");
        boolean isGlobalAdmin = "ADMIN_GLOBAL".equals(operator.getRole()) || "admin".equals(operator.getJobNo());
        boolean hasPublishPerm = operator.hasPermission("ACTIVITY_PUBLISH");
        if (!isGlobalAdmin && !hasPublishPerm) {
            throw new SecurityException("只有系统管理员或拥有活动发布权限的用户可以创建活动任务");
        }

        String title = (String) request.get("title");
        String description = (String) request.get("description");
        String taskType = (String) request.get("taskType");
        String deadlineStr = (String) request.get("deadline");

        if (title == null || title.isBlank()) throw new IllegalArgumentException("活动名称不能为空");
        if (taskType == null || taskType.isBlank()) throw new IllegalArgumentException("活动类型不能为空");
        if (deadlineStr == null || deadlineStr.isBlank()) throw new IllegalArgumentException("截止时间不能为空");

        LocalDateTime deadline;
        try {
            deadline = LocalDateTime.parse(deadlineStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            throw new IllegalArgumentException("截止时间格式错误，应为 yyyy-MM-dd HH:mm:ss");
        }
        if (deadline.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("截止时间必须晚于当前时间");
        }

        ActivityTask task = new ActivityTask();
        task.setTitle(title);
        task.setDescription(description);
        task.setTaskType(taskType);
        task.setDeadline(deadline);
        task.setCreatedBy(operator.getJobNo());
        task.setCreatedByName(operator.getName());
        task.setStatus("ACTIVE");
        task = activityTaskRepository.save(task);

        List<Integer> deptIds = new ArrayList<>();
        if (isGlobalAdmin) {
            Boolean allDepts = (Boolean) request.get("allDepts");
            if (Boolean.TRUE.equals(allDepts)) {
                departmentRepository.findAll().forEach(d -> deptIds.add(d.getId()));
            } else {
                Object deptIdList = request.get("deptIds");
                if (deptIdList instanceof List<?> list) {
                    for (Object item : list) {
                        if (item instanceof Integer id) deptIds.add(id);
                        else deptIds.add(Integer.parseInt(item.toString()));
                    }
                }
            }
        } else {
            // 单位管理员只能给本单位下任务
            if (operator.getDepartment() == null) throw new IllegalArgumentException("您不属于任何部门");
            deptIds.add(operator.getDepartment().getId());
        }

        if (deptIds.isEmpty()) throw new IllegalArgumentException("下发单位不能为空");

        for (Integer deptId : deptIds) {
            Department dept = departmentRepository.findById(deptId).orElse(null);
            if (dept != null) {
                TaskTarget target = new TaskTarget(task.getId(), deptId, dept.getDeptName());
                taskTargetRepository.save(target);

                List<User> deptAdmins = userRepository.findByDepartmentIdAndRole(deptId, "ADMIN_UNIT");
                for (User admin : deptAdmins) {
                    notificationService.createNotification(
                            admin.getJobNo(),
                            "新活动任务",
                            "上级下发了活动任务「" + title + "」，请在" + deadlineStr + "前完成填报。",
                            "ACTIVITY_TASK",
                            task.getId().toString()
                    );
                }
            }
        }

        auditLogService.log("CREATE_ACTIVITY_TASK", "ACTIVITY_TASK", task.getId().toString(),
                "创建活动任务：" + title + "，下发" + deptIds.size() + "个单位");

        Map<String, Object> result = new HashMap<>();
        result.put("id", task.getId());
        result.put("title", task.getTitle());
        result.put("targetCount", deptIds.size());
        return result;
    }

    public List<Map<String, Object>> getTasks() {
        User operator = SecurityUtils.getCurrentUser();
        if (operator == null) return Collections.emptyList();

        List<ActivityTask> tasks;
        if (SecurityUtils.canViewGlobal() || SecurityUtils.hasPermission("ACTIVITY_PUBLISH")) {
            // 全局管理员、局领导、活动发布管理员：看所有任务
            tasks = activityTaskRepository.findAllByOrderByCreateTimeDesc();
        } else {
            // 收集本单位及下级单位的部门ID
            Set<Integer> visibleDeptIds = new HashSet<>();
            if (operator.getDepartment() != null) {
                visibleDeptIds.add(operator.getDepartment().getId());
            }
            // 查找直接下级的部门
            List<User> subordinates = userRepository.findBySuperiorJobNo(operator.getJobNo());
            for (User sub : subordinates) {
                if (sub.getDepartment() != null) {
                    visibleDeptIds.add(sub.getDepartment().getId());
                }
            }

            if (visibleDeptIds.isEmpty()) return Collections.emptyList();

            List<TaskTarget> targets = taskTargetRepository.findByDeptIdIn(new ArrayList<>(visibleDeptIds));
            Set<Long> taskIds = targets.stream().map(TaskTarget::getTaskId).collect(Collectors.toSet());
            tasks = activityTaskRepository.findAllByOrderByCreateTimeDesc().stream()
                    .filter(t -> taskIds.contains(t.getId()))
                    .collect(Collectors.toList());
        }

        return tasks.stream().map(task -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", task.getId());
            map.put("title", task.getTitle());
            map.put("description", task.getDescription());
            map.put("taskType", task.getTaskType());
            map.put("deadline", task.getDeadline());
            map.put("status", task.getStatus());
            map.put("createdByName", task.getCreatedByName());
            map.put("createTime", task.getCreateTime());

            long totalTargets = taskTargetRepository.countByTaskId(task.getId());
            long submittedCount = taskTargetRepository.countByTaskIdAndStatus(task.getId(), "SUBMITTED");
            boolean overdue = "ACTIVE".equals(task.getStatus())
                    && task.getDeadline().isBefore(LocalDateTime.now())
                    && submittedCount < totalTargets;

            map.put("totalTargets", totalTargets);
            map.put("submittedCount", submittedCount);
            map.put("overdue", overdue);

            if (operator.getDepartment() != null) {
                TaskTarget myTarget = taskTargetRepository.findByTaskIdAndDeptId(task.getId(), operator.getDepartment().getId());
                if (myTarget != null) {
                    map.put("myStatus", myTarget.getStatus());
                    // 检查当前用户是否被选为参与人
                    if ("SUBMITTED".equals(myTarget.getStatus())) {
                        boolean participated = activityParticipantRepository.existsByUserJobNoAndTaskId(operator.getJobNo(), task.getId());
                        map.put("participated", participated);
                    }
                }
            }

            return map;
        }).collect(Collectors.toList());
    }

    public Map<String, Object> getTaskDetail(Long taskId) {
        ActivityTask task = activityTaskRepository.findById(taskId).orElse(null);
        if (task == null) return null;

        Map<String, Object> result = new HashMap<>();
        result.put("id", task.getId());
        result.put("title", task.getTitle());
        result.put("description", task.getDescription());
        result.put("taskType", task.getTaskType());
        result.put("deadline", task.getDeadline());
        result.put("status", task.getStatus());
        result.put("createdByName", task.getCreatedByName());
        result.put("createTime", task.getCreateTime());

        List<TaskTarget> targets = taskTargetRepository.findByTaskId(taskId);
        List<Map<String, Object>> targetDetails = new ArrayList<>();
        int totalParticipants = 0;

        for (TaskTarget target : targets) {
            Map<String, Object> tMap = new HashMap<>();
            tMap.put("deptId", target.getDeptId());
            tMap.put("deptName", target.getDeptName());
            tMap.put("status", target.getStatus());
            tMap.put("submitTime", target.getSubmitTime());

            ActivityRecord record = activityRecordRepository.findByTaskIdAndDeptId(taskId, target.getDeptId());
            if (record != null) {
                long participantCount = activityParticipantRepository.countByRecordId(record.getId());
                tMap.put("recordId", record.getId());
                tMap.put("participantCount", participantCount);
                totalParticipants += participantCount;
            }

            targetDetails.add(tMap);
        }

        long submittedCount = targets.stream().filter(t -> "SUBMITTED".equals(t.getStatus())).count();
        result.put("totalTargets", targets.size());
        result.put("submittedCount", submittedCount);
        result.put("pendingCount", targets.size() - submittedCount);
        result.put("totalParticipants", totalParticipants);
        result.put("targets", targetDetails);

        return result;
    }

    @Transactional
    public Map<String, Object> closeTask(Long taskId) {
        User operator = SecurityUtils.getCurrentUser();
        if (operator == null) throw new IllegalArgumentException("操作人不存在");
        if (!"ADMIN_GLOBAL".equals(operator.getRole()) && !"admin".equals(operator.getJobNo())
                && !SecurityUtils.hasPermission("ACTIVITY_PUBLISH")) {
            throw new SecurityException("无权结束活动任务");
        }

        ActivityTask task = activityTaskRepository.findById(taskId).orElse(null);
        if (task == null) throw new IllegalArgumentException("任务不存在");

        task.setStatus("CLOSED");
        activityTaskRepository.save(task);

        auditLogService.log("CLOSE_ACTIVITY_TASK", "ACTIVITY_TASK", taskId.toString(),
                "结束活动任务：" + task.getTitle());

        return Map.of("message", "任务已结束", "id", taskId);
    }

    @Transactional
    public Map<String, Object> submitRecord(Map<String, Object> request) {
        User operator = SecurityUtils.getCurrentUser();
        if (operator == null) throw new IllegalArgumentException("操作人不存在");
        if (operator.getDepartment() == null) throw new IllegalArgumentException("您不属于任何部门");

        Object taskIdObj = request.get("taskId");
        if (taskIdObj == null) throw new IllegalArgumentException("任务ID不能为空");
        Long taskId = Long.parseLong(taskIdObj.toString());
        Integer deptId = operator.getDepartment().getId();

        ActivityTask task = activityTaskRepository.findById(taskId).orElse(null);
        if (task == null) throw new IllegalArgumentException("任务不存在");
        if ("CLOSED".equals(task.getStatus())) throw new IllegalArgumentException("任务已结束，无法提交");
        if (task.getDeadline().isBefore(LocalDateTime.now())) throw new IllegalArgumentException("已超过截止时间，无法提交");

        TaskTarget target = taskTargetRepository.findByTaskIdAndDeptId(taskId, deptId);
        if (target == null) throw new IllegalArgumentException("该任务未下发给您的单位");

        String content = (String) request.get("content");
        if (content == null || content.isBlank()) throw new IllegalArgumentException("活动内容不能为空");

        String photos = (String) request.get("photos");
        String remark = (String) request.get("remark");

        Object participantsObj = request.get("participants");
        if (!(participantsObj instanceof List<?> list) || list.isEmpty()) {
            throw new IllegalArgumentException("请至少选择一名参与人员");
        }
        List<String> participantJobNos = list.stream()
                .map(Object::toString)
                .collect(Collectors.toList());

        ActivityRecord existingRecord = activityRecordRepository.findByTaskIdAndDeptId(taskId, deptId);
        ActivityRecord record;
        boolean isUpdate = existingRecord != null;

        if (isUpdate) {
            record = existingRecord;
            record.setContent(content);
            record.setPhotos(photos);
            record.setRemark(remark);
            record.setSubmittedBy(operator.getJobNo());
            record.setSubmittedByName(operator.getName());
            record.setSubmitTime(LocalDateTime.now());
            activityParticipantRepository.deleteByRecordId(record.getId());
        } else {
            record = new ActivityRecord();
            record.setTaskId(taskId);
            record.setDeptId(deptId);
            record.setContent(content);
            record.setPhotos(photos);
            record.setRemark(remark);
            record.setSubmittedBy(operator.getJobNo());
            record.setSubmittedByName(operator.getName());
        }
        record = activityRecordRepository.save(record);

        for (String jobNo : participantJobNos) {
            User user = userRepository.findByJobNo(jobNo);
            if (user != null) {
                ActivityParticipant participant = new ActivityParticipant(record.getId(), jobNo, user.getName());
                activityParticipantRepository.save(participant);
            }
        }

        // 通知被选中的参与人
        if (!isUpdate) {
            for (String jobNo : participantJobNos) {
                notificationService.createNotification(
                        jobNo,
                        "活动参与通知",
                        "您已被选为活动「" + task.getTitle() + "」的参与人。",
                        "ACTIVITY_PARTICIPANT",
                        task.getId().toString()
                );
            }
        }

        target.setStatus("SUBMITTED");
        target.setSubmitTime(LocalDateTime.now());
        taskTargetRepository.save(target);

        String action = isUpdate ? "UPDATE_ACTIVITY_RECORD" : "SUBMIT_ACTIVITY_RECORD";
        String actionLabel = isUpdate ? "修改" : "提交";
        auditLogService.log(action, "ACTIVITY_RECORD", record.getId().toString(),
                actionLabel + "活动记录：" + task.getTitle() + "，参与人" + participantJobNos.size() + "人");

        Map<String, Object> result = new HashMap<>();
        result.put("message", isUpdate ? "修改成功" : "提交成功");
        result.put("recordId", record.getId());
        result.put("participantCount", participantJobNos.size());
        return result;
    }

    public Map<String, Object> getRecordForEdit(Long taskId) {
        User operator = SecurityUtils.getCurrentUser();
        if (operator == null || operator.getDepartment() == null) return null;

        Integer deptId = operator.getDepartment().getId();
        ActivityRecord record = activityRecordRepository.findByTaskIdAndDeptId(taskId, deptId);
        if (record == null) return null;

        Map<String, Object> result = new HashMap<>();
        result.put("id", record.getId());
        result.put("taskId", record.getTaskId());
        result.put("content", record.getContent());
        result.put("photos", record.getPhotos());
        result.put("remark", record.getRemark());
        result.put("submittedByName", record.getSubmittedByName());
        result.put("submitTime", record.getSubmitTime());

        List<ActivityParticipant> participants = activityParticipantRepository.findByRecordId(record.getId());
        result.put("participants", participants.stream().map(p -> {
            Map<String, Object> pMap = new HashMap<>();
            pMap.put("userJobNo", p.getUserJobNo());
            pMap.put("userName", p.getUserName());
            return pMap;
        }).toList());

        return result;
    }

    public Map<String, Object> getRecordDetail(Long recordId) {
        ActivityRecord record = activityRecordRepository.findById(recordId).orElse(null);
        if (record == null) return null;

        ActivityTask task = activityTaskRepository.findById(record.getTaskId()).orElse(null);

        Map<String, Object> result = new HashMap<>();
        result.put("id", record.getId());
        result.put("taskId", record.getTaskId());
        result.put("taskTitle", task != null ? task.getTitle() : "");
        result.put("deptId", record.getDeptId());
        Department dept = departmentRepository.findById(record.getDeptId()).orElse(null);
        result.put("deptName", dept != null ? dept.getDeptName() : "");
        result.put("content", record.getContent());
        result.put("photos", record.getPhotos());
        result.put("remark", record.getRemark());
        result.put("submittedBy", record.getSubmittedBy());
        result.put("submittedByName", record.getSubmittedByName());
        result.put("submitTime", record.getSubmitTime());

        List<ActivityParticipant> participants = activityParticipantRepository.findByRecordId(record.getId());
        result.put("participants", participants.stream().map(p -> {
            Map<String, Object> pMap = new HashMap<>();
            pMap.put("userJobNo", p.getUserJobNo());
            pMap.put("userName", p.getUserName());
            return pMap;
        }).toList());

        return result;
    }

    public byte[] exportParticipants(Long taskId) throws IOException {
        ActivityTask task = activityTaskRepository.findById(taskId).orElse(null);
        if (task == null) throw new IllegalArgumentException("任务不存在");

        List<TaskTarget> targets = taskTargetRepository.findByTaskId(taskId);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("参与人员");
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            String[] headers = {"单位", "参与人工号", "参与人姓名", "提交时间"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            int rowIndex = 1;

            for (TaskTarget target : targets) {
                ActivityRecord record = activityRecordRepository.findByTaskIdAndDeptId(taskId, target.getDeptId());
                if (record == null) continue;

                List<ActivityParticipant> participants = activityParticipantRepository.findByRecordId(record.getId());
                String submitTimeStr = record.getSubmitTime() != null ? record.getSubmitTime().format(dtf) : "";

                if (participants.isEmpty()) {
                    Row row = sheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue(target.getDeptName());
                    row.getCell(0).setCellStyle(dataStyle);
                    row.createCell(1).setCellValue("-");
                    row.getCell(1).setCellStyle(dataStyle);
                    row.createCell(2).setCellValue("（无参与人）");
                    row.getCell(2).setCellStyle(dataStyle);
                    row.createCell(3).setCellValue(submitTimeStr);
                    row.getCell(3).setCellStyle(dataStyle);
                } else {
                    for (ActivityParticipant p : participants) {
                        Row row = sheet.createRow(rowIndex++);
                        row.createCell(0).setCellValue(target.getDeptName());
                        row.getCell(0).setCellStyle(dataStyle);
                        row.createCell(1).setCellValue(p.getUserJobNo());
                        row.getCell(1).setCellStyle(dataStyle);
                        row.createCell(2).setCellValue(p.getUserName());
                        row.getCell(2).setCellStyle(dataStyle);
                        row.createCell(3).setCellValue(submitTimeStr);
                        row.getCell(3).setCellStyle(dataStyle);
                    }
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    @Transactional
    public Map<String, Object> deleteTask(Long taskId) {
        User operator = SecurityUtils.getCurrentUser();
        if (operator == null) throw new IllegalArgumentException("操作人不存在");
        if (!"ADMIN_GLOBAL".equals(operator.getRole()) && !"admin".equals(operator.getJobNo())
                && !SecurityUtils.hasPermission("ACTIVITY_PUBLISH")) {
            throw new SecurityException("无权删除活动任务");
        }

        ActivityTask task = activityTaskRepository.findById(taskId).orElse(null);
        if (task == null) throw new IllegalArgumentException("任务不存在");

        List<TaskTarget> targets = taskTargetRepository.findByTaskId(taskId);
        for (TaskTarget target : targets) {
            ActivityRecord record = activityRecordRepository.findByTaskIdAndDeptId(taskId, target.getDeptId());
            if (record != null) {
                activityParticipantRepository.deleteByRecordId(record.getId());
                activityRecordRepository.delete(record);
            }
            taskTargetRepository.delete(target);
        }

        activityTaskRepository.delete(task);

        auditLogService.log("DELETE_ACTIVITY_TASK", "ACTIVITY_TASK", taskId.toString(),
                "删除活动任务：" + task.getTitle());

        return Map.of("message", "任务已删除");
    }

    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void autoCloseOverdueTasks() {
        LocalDateTime now = LocalDateTime.now();
        List<ActivityTask> activeTasks = activityTaskRepository.findByStatusOrderByCreateTimeDesc("ACTIVE");
        for (ActivityTask task : activeTasks) {
            if (task.getDeadline().isBefore(now)) {
                long total = taskTargetRepository.countByTaskId(task.getId());
                long submitted = taskTargetRepository.countByTaskIdAndStatus(task.getId(), "SUBMITTED");
                if (total > 0 && submitted >= total) {
                    task.setStatus("CLOSED");
                    activityTaskRepository.save(task);
                    auditLogService.log("AUTO_CLOSE_ACTIVITY_TASK", "ACTIVITY_TASK", task.getId().toString(),
                            "自动结束活动任务：" + task.getTitle() + "（全部已提交）");
                }
            }
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(true);
        return style;
    }
}
