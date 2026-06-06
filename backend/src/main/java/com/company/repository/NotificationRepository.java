package com.company.repository;

import com.company.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserJobNoOrderByCreateTimeDesc(String userJobNo);

    List<Notification> findByUserJobNoAndIsReadFalseOrderByCreateTimeDesc(String userJobNo);

    long countByUserJobNoAndIsReadFalse(String userJobNo);

    boolean existsByUserJobNoAndTypeAndRelatedIdAndIsReadFalse(String userJobNo, String type, String relatedId);
}
