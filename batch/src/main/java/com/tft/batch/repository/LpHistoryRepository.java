package com.tft.batch.repository;

import com.tft.batch.model.entity.LpHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LpHistoryRepository extends JpaRepository<LpHistory, Long> {
    
    LpHistory findTopByPuuidOrderByCreatedAtDesc(String puuid);

    @Query("SELECT DISTINCT l.puuid FROM LpHistory l WHERE l.createdAt > :date")
    List<String> findDistinctPuuidByCreatedAtAfter(@Param("date") LocalDateTime date);
}
