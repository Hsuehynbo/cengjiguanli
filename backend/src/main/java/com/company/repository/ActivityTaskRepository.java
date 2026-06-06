package com.company.repository;

import com.company.entity.ActivityTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityTaskRepository extends JpaRepository<ActivityTask, Long> {

    List<ActivityTask> findAllByOrderByCreateTimeDesc();

    List<ActivityTask> findByStatusOrderByCreateTimeDesc(String status);

    long countByStatus(String status);
}
