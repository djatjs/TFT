package com.tft.batch.service;

import com.tft.batch.client.RiotLeagueClient;
import com.tft.batch.client.dto.TftLeagueItemDto;
import com.tft.batch.client.dto.TftLeagueListDto;
import com.tft.batch.model.entity.MatchFetchQueue;
import com.tft.batch.repository.MatchFetchQueueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HighTierCollectorService {

    private final RiotLeagueClient riotLeagueClient;
    private final MatchFetchQueueRepository queueRepository;

    @Transactional
    public void collectHighTierPlayers() {
        log.info("Starting High Tier player collection...");

        // 1. Challenger
        try {
            collectLeague(riotLeagueClient.getChallengerLeague(), 100);
        } catch (Exception e) {
            log.error("Failed to collect Challenger league: {}", e.getMessage());
        }

        // 2. Grandmaster
        try {
            collectLeague(riotLeagueClient.getGrandmasterLeague(), 80);
        } catch (Exception e) {
            log.error("Failed to collect Grandmaster league: {}", e.getMessage());
        }

        // 3. Master
        try {
            // collectLeague(riotLeagueClient.getMasterLeague(), 50);
        } catch (Exception e) {
            log.error("Failed to collect Master league: {}", e.getMessage());
        }

        log.info("High Tier player collection finished.");
    }

    private void collectLeague(TftLeagueListDto league, int priority) {
        if (league == null || league.getEntries() == null) {
            log.warn("League data is null or empty.");
            return;
        }

        if (!league.getEntries().isEmpty()) {
            log.info("Sample first entry puuid: {}", league.getEntries().get(0).getPuuid());
        }

        int count = 0;
        for (TftLeagueItemDto entry : league.getEntries()) {
            if (entry.getPuuid() != null && !queueRepository.existsByMfqId(entry.getPuuid())) {
                queueRepository.save(MatchFetchQueue.builder()
                        .mfqId(entry.getPuuid())
                        .mfqType("SUMMONER")
                        .mfqStatus("READY")
                        .mfqPriority(priority)
                        .mfqUpdatedAt(LocalDateTime.now())
                        .build());
                count++;
            }
        }
        log.info("Added {} new players to queue from {}", count, league.getTier());
    }
}
