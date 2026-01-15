package com.tft.batch.scheduler;

import com.tft.batch.service.MetaAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MetaAnalysisScheduler {

    private final MetaAnalysisService metaAnalysisService;

    // 프로그램 시작 시 1회 즉시 실행
    @org.springframework.context.event.EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void init() {
        log.info("Triggering initial Meta Analysis...");
        metaAnalysisService.analyzeMeta();
    }

    // 1시간마다 실행 (매 정각)
    @Scheduled(cron = "0 0 * * * *")
    public void run() {
        metaAnalysisService.analyzeMeta();
    }
}
