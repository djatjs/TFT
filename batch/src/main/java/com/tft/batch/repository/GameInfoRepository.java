package com.tft.batch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.tft.batch.model.entity.GameInfo;
import java.util.Optional;

public interface GameInfoRepository extends JpaRepository<GameInfo, Integer> {
    Optional<GameInfo> findByGaId(String gaId);
    boolean existsByGaId(String gaId);
}
