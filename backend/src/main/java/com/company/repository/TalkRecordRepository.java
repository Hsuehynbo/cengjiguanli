package com.company.repository;

import com.company.entity.TalkRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TalkRecordRepository extends JpaRepository<TalkRecord, Integer> {

    List<TalkRecord> findByTalkerJobNo(String talkerJobNo);

    List<TalkRecord> findByTargetJobNo(String targetJobNo);

    List<TalkRecord> findByTalkerJobNoAndTargetJobNo(String talkerJobNo, String targetJobNo);

    @Query("SELECT t FROM TalkRecord t WHERE t.targetJobNo = :targetJobNo AND t.talkType = :talkType AND t.talkTime >= :dayStart AND t.talkTime < :dayEnd")
    List<TalkRecord> findByTargetAndTypeAndDateRange(@Param("targetJobNo") String targetJobNo,
                                                      @Param("talkType") String talkType,
                                                      @Param("dayStart") LocalDateTime dayStart,
                                                      @Param("dayEnd") LocalDateTime dayEnd);

    List<TalkRecord> findByTalkTimeBetweenOrderByTalkTimeDesc(LocalDateTime start, LocalDateTime end);

    List<TalkRecord> findByTargetJobNoAndTalkTimeBetweenOrderByTalkTimeDesc(String targetJobNo, LocalDateTime start, LocalDateTime end);

    List<TalkRecord> findByTalkTypeAndTalkTimeBetweenOrderByTalkTimeDesc(String talkType, LocalDateTime start, LocalDateTime end);

    List<TalkRecord> findByTargetJobNoAndTalkTypeAndTalkTimeBetweenOrderByTalkTimeDesc(String targetJobNo, String talkType, LocalDateTime start, LocalDateTime end);
}