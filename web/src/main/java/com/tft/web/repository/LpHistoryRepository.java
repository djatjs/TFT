package com.tft.web.repository;

import com.tft.web.domain.LpHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LpHistoryRepository extends JpaRepository<LpHistory, Long> {
    List<LpHistory> findTop15ByPuuidOrderByCreatedAtDesc(String puuid);
    
    // 가장 최근 기록 하나 가져오기
    LpHistory findTopByPuuidOrderByCreatedAtDesc(String puuid);

    @Query(value = """
        SELECT *
        FROM (
            SELECT *, ROW_NUMBER() OVER (PARTITION BY puuid ORDER BY created_at DESC) as rn
            FROM lp_history
            WHERE tier IN ('CHALLENGER', 'GRANDMASTER')
        ) t
        WHERE t.rn = 1
        ORDER BY 
            CASE tier
                WHEN 'CHALLENGER' THEN 1
                WHEN 'GRANDMASTER' THEN 2
                ELSE 3
            END,
            lp DESC
        LIMIT 300
        """, nativeQuery = true)
    List<LpHistory> findTopRankers();
}
