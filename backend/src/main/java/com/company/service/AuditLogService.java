package com.company.service;

import com.company.entity.User;
import com.company.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class AuditLogService {

    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    public void log(String action, String targetType, String targetId, String detail) {
        User operator = SecurityUtils.getCurrentUser();
        String operatorJobNo = operator != null ? operator.getJobNo() : "SYSTEM";
        String operatorName = operator != null ? operator.getName() : "系统";
        String ipAddress = getClientIp();

        auditLogger.info("[{}] [{}] {}({}) - {} - IP: {}", action, targetType, operatorName, operatorJobNo, detail, ipAddress);
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) return "unknown";
            HttpServletRequest request = attributes.getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("X-Real-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            return ip;
        } catch (Exception e) {
            return "unknown";
        }
    }
}
