package com.company.repository;

import com.company.entity.HierarchyHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HierarchyHistoryRepository extends JpaRepository<HierarchyHistory, Long> {
    List<HierarchyHistory> findByTargetJobNoOrderByStartDateDesc(String targetJobNo);
    Optional<HierarchyHistory> findFirstByTargetJobNoAndEndDateIsNullOrderByStartDateDesc(String targetJobNo);
}
