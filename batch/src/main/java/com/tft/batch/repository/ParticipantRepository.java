package com.tft.batch.repository;

import com.tft.batch.model.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ParticipantRepository extends JpaRepository<Participant, Integer> {
    
    @Query("SELECT DISTINCT p FROM Participant p LEFT JOIN FETCH p.traits WHERE p.gameInfo.gaDatetime > :startTime")
    List<Participant> findAllWithTraits(@Param("startTime") LocalDateTime startTime);

    @Query("SELECT DISTINCT p FROM Participant p LEFT JOIN FETCH p.units WHERE p IN :participants")
    List<Participant> fetchUnits(@Param("participants") List<Participant> participants);
}
