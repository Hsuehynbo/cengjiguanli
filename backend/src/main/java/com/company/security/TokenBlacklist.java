package com.company.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenBlacklist {

    private static final long JWT_EXPIRATION_MS = 86400000L; // 24 hours

    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    public void blacklist(String token) {
        blacklist.put(token, System.currentTimeMillis());
    }

    public boolean isBlacklisted(String token) {
        return blacklist.containsKey(token);
    }

    public void cleanExpired(long maxAgeMs) {
        long now = System.currentTimeMillis();
        blacklist.entrySet().removeIf(e -> now - e.getValue() > maxAgeMs);
    }

    @Scheduled(fixedRate = 3600000) // every hour
    public void scheduledCleanup() {
        cleanExpired(JWT_EXPIRATION_MS);
    }
}
