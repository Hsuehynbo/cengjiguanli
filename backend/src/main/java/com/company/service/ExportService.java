package com.company.service;

import com.company.entity.*;
import com.company.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExportService {

    private final TalkRecordRepository talkRecordRepository;
    private final HomeVisitRepository homeVisitRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    public ExportService(TalkRecordRepository talkRecordRepository,
                         HomeVisitRepository homeVisitRepository,
                         UserRepository userRepository,
                         DepartmentRepository departmentRepository) {
        this.talkRecordRepository = talkRecordRepository;
        this.homeVisitRepository = homeVisitRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    public byte[] exportTalkRecords(String targetJobNo, String talkType, String startDate, String endDate, java.util.Set<String> visibleJobNos) throws IOException {
        Map<String, String> userNames = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getJobNo, User::getName, (a, b) -> a));

        LocalDateTime start = (startDate != null && !startDate.isEmpty())
                ? LocalDate.parse(startDate).atStartOfDay() : null;
        LocalDateTime end = (endDate != null && !endDate.isEmpty())
                ? LocalDate.parse(endDate).plusDays(1).atStartOfDay() : null;

        List<TalkRecord> records = talkRecordRepository.findAll().stream()
                .filter(r -> visibleJobNos == null || visibleJobNos.contains(r.getTargetJobNo()))
                .filter(r -> targetJobNo == null || targetJobNo.isEmpty() || r.getTargetJobNo().equals(targetJobNo))
                .filter(r -> talkType == null || talkType.isEmpty() || talkType.equals(r.getTalkType()))
                .filter(r -> start == null || (r.getTalkTime() != null && !r.getTalkTime().isBefore(start)))
                .filter(r -> end == null || (r.getTalkTime() != null && r.getTalkTime().isBefore(end)))
                .sorted((a, b) -> {
                    if (a.getTalkTime() == null && b.getTalkTime() == null) return 0;
                    if (a.getTalkTime() == null) return 1;
                    if (b.getTalkTime() == null) return -1;
                    return b.getTalkTime().compareTo(a.getTalkTime());
                })
                .collect(Collectors.toList());

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("谈话记录");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            String[] headers = {"序号", "谈话时间", "谈话人", "被谈话人", "谈话类型", "谈话地点", "谈话内容"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (int i = 0; i < records.size(); i++) {
                TalkRecord r = records.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(i + 1);
                row.getCell(0).setCellStyle(dataStyle);
                row.createCell(1).setCellValue(r.getTalkTime() != null ? r.getTalkTime().format(dtf) : "");
                row.getCell(1).setCellStyle(dataStyle);
                row.createCell(2).setCellValue(userNames.getOrDefault(r.getTalkerJobNo(), r.getTalkerJobNo()));
                row.getCell(2).setCellStyle(dataStyle);
                row.createCell(3).setCellValue(userNames.getOrDefault(r.getTargetJobNo(), r.getTargetJobNo()));
                row.getCell(3).setCellStyle(dataStyle);
                row.createCell(4).setCellValue(r.getTalkType() != null ? r.getTalkType() : "");
                row.getCell(4).setCellStyle(dataStyle);
                row.createCell(5).setCellValue(r.getLocation() != null ? r.getLocation() : "");
                row.getCell(5).setCellStyle(dataStyle);
                row.createCell(6).setCellValue(r.getContent() != null ? r.getContent() : "");
                row.getCell(6).setCellStyle(dataStyle);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportHomeVisits(String targetJobNo, String visitType, String startDate, String endDate, java.util.Set<String> visibleJobNos) throws IOException {
        Map<String, String> userNames = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getJobNo, User::getName, (a, b) -> a));

        LocalDateTime start = (startDate != null && !startDate.isEmpty())
                ? LocalDate.parse(startDate).atStartOfDay() : null;
        LocalDateTime end = (endDate != null && !endDate.isEmpty())
                ? LocalDate.parse(endDate).plusDays(1).atStartOfDay() : null;

        List<HomeVisit> visits = homeVisitRepository.findAll().stream()
                .filter(v -> visibleJobNos == null || visibleJobNos.contains(v.getTargetJobNo()))
                .filter(v -> targetJobNo == null || targetJobNo.isEmpty() || v.getTargetJobNo().equals(targetJobNo))
                .filter(v -> visitType == null || visitType.isEmpty() || visitType.equals(v.getVisitType()))
                .filter(v -> start == null || (v.getVisitTime() != null && !v.getVisitTime().isBefore(start)))
                .filter(v -> end == null || (v.getVisitTime() != null && v.getVisitTime().isBefore(end)))
                .sorted((a, b) -> {
                    if (a.getVisitTime() == null && b.getVisitTime() == null) return 0;
                    if (a.getVisitTime() == null) return 1;
                    if (b.getVisitTime() == null) return -1;
                    return b.getVisitTime().compareTo(a.getVisitTime());
                })
                .collect(Collectors.toList());

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("家访记录");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            String[] headers = {"序号", "家访时间", "家访人", "被家访人", "家访类型", "家访地点", "家访内容"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (int i = 0; i < visits.size(); i++) {
                HomeVisit v = visits.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(i + 1);
                row.getCell(0).setCellStyle(dataStyle);
                row.createCell(1).setCellValue(v.getVisitTime() != null ? v.getVisitTime().format(dtf) : "");
                row.getCell(1).setCellStyle(dataStyle);
                row.createCell(2).setCellValue(userNames.getOrDefault(v.getOperatorJobNo(), v.getOperatorJobNo()));
                row.getCell(2).setCellStyle(dataStyle);
                row.createCell(3).setCellValue(userNames.getOrDefault(v.getTargetJobNo(), v.getTargetJobNo()));
                row.getCell(3).setCellStyle(dataStyle);
                row.createCell(4).setCellValue(v.getVisitType() != null ? v.getVisitType() : "");
                row.getCell(4).setCellStyle(dataStyle);
                row.createCell(5).setCellValue(v.getLocation() != null ? v.getLocation() : "");
                row.getCell(5).setCellStyle(dataStyle);
                row.createCell(6).setCellValue(v.getContent() != null ? v.getContent() : "");
                row.getCell(6).setCellStyle(dataStyle);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportDepartmentStats(Integer deptId) throws IOException {
        List<User> users;
        if (deptId != null) {
            users = userRepository.findAll().stream()
                    .filter(u -> u.getDepartment() != null && u.getDepartment().getId().equals(deptId))
                    .collect(Collectors.toList());
        } else {
            users = userRepository.findAll();
        }

        Map<String, String> userNames = users.stream()
                .collect(Collectors.toMap(User::getJobNo, User::getName, (a, b) -> a));

        List<TalkRecord> allTalks = talkRecordRepository.findAll();
        List<HomeVisit> allVisits = homeVisitRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            // Sheet 1: 人员统计
            Sheet sheet1 = workbook.createSheet("人员统计");
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            String[] headers1 = {"序号", "工号", "姓名", "部门", "职位", "风险等级", "是否重点关注", "谈话次数", "家访次数"};
            Row headerRow1 = sheet1.createRow(0);
            for (int i = 0; i < headers1.length; i++) {
                Cell cell = headerRow1.createCell(i);
                cell.setCellValue(headers1[i]);
                cell.setCellStyle(headerStyle);
            }

            for (int i = 0; i < users.size(); i++) {
                User u = users.get(i);
                long talkCount = allTalks.stream()
                        .filter(t -> t.getTargetJobNo().equals(u.getJobNo())).count();
                long visitCount = allVisits.stream()
                        .filter(v -> v.getTargetJobNo().equals(u.getJobNo())).count();

                Row row = sheet1.createRow(i + 1);
                row.createCell(0).setCellValue(i + 1);
                row.getCell(0).setCellStyle(dataStyle);
                row.createCell(1).setCellValue(u.getJobNo());
                row.getCell(1).setCellStyle(dataStyle);
                row.createCell(2).setCellValue(u.getName());
                row.getCell(2).setCellStyle(dataStyle);
                row.createCell(3).setCellValue(u.getDepartment() != null ? u.getDepartment().getDeptName() : "");
                row.getCell(3).setCellStyle(dataStyle);
                row.createCell(4).setCellValue(u.getPosition() != null ? u.getPosition() : "");
                row.getCell(4).setCellStyle(dataStyle);
                row.createCell(5).setCellValue(u.getRiskLevel() != null ? u.getRiskLevel() : "");
                row.getCell(5).setCellStyle(dataStyle);
                row.createCell(6).setCellValue(Boolean.TRUE.equals(u.getIsKeyPersonnel()) ? "是" : "否");
                row.getCell(6).setCellStyle(dataStyle);
                row.createCell(7).setCellValue((int) talkCount);
                row.getCell(7).setCellStyle(dataStyle);
                row.createCell(8).setCellValue((int) visitCount);
                row.getCell(8).setCellStyle(dataStyle);
            }

            for (int i = 0; i < headers1.length; i++) {
                sheet1.autoSizeColumn(i);
            }

            // Sheet 2: 谈话汇总
            Sheet sheet2 = workbook.createSheet("谈话汇总");
            Map<String, Long> talkByType = allTalks.stream()
                    .filter(t -> users.stream().anyMatch(u -> u.getJobNo().equals(t.getTargetJobNo())))
                    .collect(Collectors.groupingBy(TalkRecord::getTalkType, Collectors.counting()));

            String[] headers2 = {"谈话类型", "次数"};
            Row headerRow2 = sheet2.createRow(0);
            for (int i = 0; i < headers2.length; i++) {
                Cell cell = headerRow2.createCell(i);
                cell.setCellValue(headers2[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Map.Entry<String, Long> entry : talkByType.entrySet()) {
                Row row = sheet2.createRow(rowIdx++);
                row.createCell(0).setCellValue(entry.getKey());
                row.getCell(0).setCellStyle(dataStyle);
                row.createCell(1).setCellValue(entry.getValue());
                row.getCell(1).setCellStyle(dataStyle);
            }

            sheet2.autoSizeColumn(0);
            sheet2.autoSizeColumn(1);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
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
