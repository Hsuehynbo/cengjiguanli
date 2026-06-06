package com.company.dto;

import com.company.entity.User;

import java.util.HashMap;
import java.util.Map;

public class UserDto {

    private UserDto() {
    }

    public static Map<String, Object> fromEntity(User user) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", user.getId());
        dto.put("jobNo", user.getJobNo());
        dto.put("name", user.getName());
        dto.put("position", user.getPosition());
        dto.put("superiorJobNo", user.getSuperiorJobNo());
        dto.put("isKeyPersonnel", user.getIsKeyPersonnel());
        dto.put("riskLevel", user.getRiskLevel());
        dto.put("role", user.getRole());
        dto.put("avatar", user.getAvatar());
        dto.put("createTime", user.getCreateTime());
        if (user.getDepartment() != null) {
            Map<String, Object> deptDto = new HashMap<>();
            deptDto.put("id", user.getDepartment().getId());
            deptDto.put("deptName", user.getDepartment().getDeptName());
            dto.put("department", deptDto);
        }
        return dto;
    }
}
