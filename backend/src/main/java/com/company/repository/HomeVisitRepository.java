package com.company.repository;

import com.company.entity.HomeVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HomeVisitRepository extends JpaRepository<HomeVisit, Long> {
    List<HomeVisit> findByTargetJobNo(String targetJobNo);

    List<HomeVisit> findByVisitTimeBetweenOrderByVisitTimeDesc(LocalDateTime start, LocalDateTime end);

    List<HomeVisit> findByTargetJobNoAndVisitTimeBetweenOrderByVisitTimeDesc(String targetJobNo, LocalDateTime start, LocalDateTime end);

    List<HomeVisit> findByVisitTypeAndVisitTimeBetweenOrderByVisitTimeDesc(String visitType, LocalDateTime start, LocalDateTime end);

    List<HomeVisit> findByTargetJobNoAndVisitTypeAndVisitTimeBetweenOrderByVisitTimeDesc(String targetJobNo, String visitType, LocalDateTime start, LocalDateTime end);
}