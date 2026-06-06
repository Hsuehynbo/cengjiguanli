package com.company.service;

import com.company.entity.*;
import com.company.repository.*;
import com.company.security.SecurityUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final TalkRecordRepository talkRecordRepository;
    private final HomeVisitRepository homeVisitRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository,
                               TalkRecordRepository talkRecordRepository,
                               HomeVisitRepository homeVisitRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.talkRecordRepository = talkRecordRepository;
        this.homeVisitRepository = homeVisitRepository;
    }

    public List<Notification> getMyNotifications() {
        User user = SecurityUtils.getCurrentUser();
        if (user == null) return Collections.emptyList();
        return notificationRepository.findByUserJobNoOrderByCreateTimeDesc(user.getJobNo());
    }

    public List<Notification> getUnreadNotifications() {
        User user = SecurityUtils.getCurrentUser();
        if (user == null) return Collections.emptyList();
        return notificationRepository.findByUserJobNoAndIsReadFalseOrderByCreateTimeDesc(user.getJobNo());
    }

    public long getUnreadCount() {
        User user = SecurityUtils.getCurrentUser();
        if (user == null) return 0;
        return notificationRepository.countByUserJobNoAndIsReadFalse(user.getJobNo());
    }

    public Notification markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id).orElse(null);
        if (notification == null) return null;
        User user = SecurityUtils.getCurrentUser();
        if (user == null || !user.getJobNo().equals(notification.getUserJobNo())) {
            throw new SecurityException("无权操作此通知");
        }
        notification.setIsRead(true);
        return notificationRepository.save(notification);
    }

    public void markAllAsRead() {
        User user = SecurityUtils.getCurrentUser();
        if (user == null) return;
        List<Notification> unread = notificationRepository.findByUserJobNoAndIsReadFalseOrderByCreateTimeDesc(user.getJobNo());
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
    }

    public Notification createNotification(String userJobNo, String title, String content, String type, String relatedId) {
        Notification notification = new Notification(userJobNo, title, content, type, relatedId);
        return notificationRepository.save(notification);
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void generateOverdueReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<User> allUsers = userRepository.findAll();
        Map<String, String> userNames = allUsers.stream()
                .collect(Collectors.toMap(User::getJobNo, User::getName, (a, b) -> a));

        List<TalkRecord> allTalks = talkRecordRepository.findAll();
        List<HomeVisit> allVisits = homeVisitRepository.findAll();

        Map<String, LocalDateTime> lastTalkMap = new HashMap<>();
        for (TalkRecord t : allTalks) {
            if (t.getTalkTime() != null) {
                lastTalkMap.merge(t.getTargetJobNo(), t.getTalkTime(),
                        (a, b) -> a.isAfter(b) ? a : b);
            }
        }

        Map<String, LocalDateTime> lastVisitMap = new HashMap<>();
        for (HomeVisit v : allVisits) {
            if (v.getVisitTime() != null) {
                lastVisitMap.merge(v.getTargetJobNo(), v.getVisitTime(),
                        (a, b) -> a.isAfter(b) ? a : b);
            }
        }

        for (User user : allUsers) {
            if (!"USER".equals(user.getRole())) continue;

            String jobNo = user.getJobNo();
            String name = user.getName();

            LocalDateTime lastTalk = lastTalkMap.get(jobNo);
            if (lastTalk == null || ChronoUnit.DAYS.between(lastTalk, now) > 30) {
                String title = "谈话提醒";
                String content = name + "（" + jobNo + "）已超过30天未进行谈话，请及时安排。";
                if (lastTalk != null) {
                    long days = ChronoUnit.DAYS.between(lastTalk, now);
                    content = name + "（" + jobNo + "）距上次谈话已" + days + "天，请及时安排。";
                }
                createNotificationForSuperiors(jobNo, title, content, "TALK_OVERDUE");
            }

            LocalDateTime lastVisit = lastVisitMap.get(jobNo);
            if (lastVisit == null || ChronoUnit.DAYS.between(lastVisit, now) > 90) {
                String title = "家访提醒";
                String content = name + "（" + jobNo + "）已超过90天未进行家访，请及时安排。";
                if (lastVisit != null) {
                    long days = ChronoUnit.DAYS.between(lastVisit, now);
                    content = name + "（" + jobNo + "）距上次家访已" + days + "天，请及时安排。";
                }
                createNotificationForSuperiors(jobNo, title, content, "VISIT_OVERDUE");
            }
        }
    }

    private void createNotificationForSuperiors(String targetJobNo, String title, String content, String type) {
        User target = userRepository.findByJobNo(targetJobNo);
        if (target == null || target.getSuperiorJobNo() == null) return;

        String superiorJobNo = target.getSuperiorJobNo();
        boolean alreadyExists = notificationRepository.existsByUserJobNoAndTypeAndRelatedIdAndIsReadFalse(superiorJobNo, type, targetJobNo);
        if (!alreadyExists) {
            createNotification(superiorJobNo, title, content, type, targetJobNo);
        }

        User superior = userRepository.findByJobNo(superiorJobNo);
        if (superior != null && superior.getSuperiorJobNo() != null) {
            String grandSuperiorJobNo = superior.getSuperiorJobNo();
            boolean alreadyExistsForGrand = notificationRepository.existsByUserJobNoAndTypeAndRelatedIdAndIsReadFalse(grandSuperiorJobNo, type, targetJobNo);
            if (!alreadyExistsForGrand) {
                createNotification(grandSuperiorJobNo, title, content, type, targetJobNo);
            }
        }
    }
}
