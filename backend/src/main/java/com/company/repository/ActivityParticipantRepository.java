package com.company.repository;

import com.company.entity.ActivityParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ActivityParticipantRepository extends JpaRepository<ActivityParticipant, Long> {

    List<ActivityParticipant> findByRecordId(Long recordId);

    void deleteByRecordId(Long recordId);

    long countByRecordId(Long recordId);

    @Query("SELECT COUNT(ap) > 0 FROM ActivityParticipant ap JOIN ActivityRecord ar ON ap.recordId = ar.id WHERE ap.userJobNo = :userJobNo AND ar.taskId = :taskId")
    boolean existsByUserJobNoAndTaskId(@Param("userJobNo") String userJobNo, @Param("taskId") Long taskId);
}
