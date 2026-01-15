package com.tft.batch.service;

import com.tft.batch.client.RiotLeagueClient;
import com.tft.batch.client.dto.TftLeagueEntryDto;
import com.tft.batch.model.entity.LpHistory;
import com.tft.batch.repository.LpHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LpUpdateService {

    private final LpHistoryRepository lpHistoryRepository;
    private final RiotLeagueClient riotLeagueClient;

    @Transactional
    public void updateActiveSummonersLp() {
        // 1. Get PUUIDs active in the last 24 hours
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        List<String> activePuuids = lpHistoryRepository.findDistinctPuuidByCreatedAtAfter(oneDayAgo);

        log.info("Found {} active summoners to track LP.", activePuuids.size());

        for (String puuid : activePuuids) {
            try {
                processPuuid(puuid);
                // Rate limit safety: 50ms delay
                Thread.sleep(50);
            } catch (Exception e) {
                log.error("Error updating LP for puuid: {}", puuid, e);
            }
        }
    }

    private void processPuuid(String puuid) {
        TftLeagueEntryDto league = riotLeagueClient.getTftLeagueByPuuid(puuid);

        if (league == null) return;

        LpHistory lastRecord = lpHistoryRepository.findTopByPuuidOrderByCreatedAtDesc(puuid);

        boolean needUpdate = false;
        if (lastRecord == null) {
            needUpdate = true;
        } else {
            // Check if LP or Tier changed
            if (lastRecord.getLp() != league.getLeaguePoints() || !lastRecord.getTier().equals(league.getTier())) {
                needUpdate = true;
            }
        }

        if (needUpdate) {
            LpHistory newHistory = LpHistory.builder()
                    .puuid(puuid)
                    .tier(league.getTier())
                    .rank_str(league.getRank())
                    .lp(league.getLeaguePoints())
                    .createdAt(LocalDateTime.now())
                    .build();
            
            lpHistoryRepository.save(newHistory);
            log.info("Updated LP for {}: {} {} {}LP", puuid, league.getTier(), league.getRank(), league.getLeaguePoints());
        }
    }
}
