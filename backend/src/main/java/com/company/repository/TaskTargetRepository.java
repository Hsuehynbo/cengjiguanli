package com.company.repository;

import com.company.entity.TaskTarget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskTargetRepository extends JpaRepository<TaskTarget, Long> {

    List<TaskTarget> findByTaskId(Long taskId);

    List<TaskTarget> findByDeptId(Integer deptId);

    List<TaskTarget> findByDeptIdIn(List<Integer> deptIds);

    List<TaskTarget> findByDeptIdAndStatus(Integer deptId, String status);

    TaskTarget findByTaskIdAndDeptId(Long taskId, Integer deptId);

    long countByTaskId(Long taskId);

    long countByTaskIdAndStatus(Long taskId, String status);
}
