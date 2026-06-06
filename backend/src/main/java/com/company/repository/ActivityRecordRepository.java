package com.company.repository;

import com.company.entity.ActivityRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRecordRepository extends JpaRepository<ActivityRecord, Long> {

    List<ActivityRecord> findByTaskId(Long taskId);

    ActivityRecord findByTaskIdAndDeptId(Long taskId, Integer deptId);

    List<ActivityRecord> findByDeptId(Integer deptId);
}
