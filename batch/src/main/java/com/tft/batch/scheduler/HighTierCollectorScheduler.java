package com.tft.batch.scheduler;

import com.tft.batch.service.HighTierCollectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class HighTierCollectorScheduler {

    private final HighTierCollectorService highTierCollectorService;

    // 매일 새벽 3시에 실행 (0 0 3 * * *)
    @Scheduled(cron = "0 0 3 * * *")
    public void collectHighTierPlayers() {
        log.info("Scheduled task: Collecting high tier players");
        highTierCollectorService.collectHighTierPlayers();
    }
}
