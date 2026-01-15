package com.tft.batch.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tft.batch.model.entity.MetaDeck;
import com.tft.batch.model.entity.Participant;
import com.tft.batch.model.entity.Trait;
import com.tft.batch.model.entity.Unit;
import com.tft.batch.repository.MetaDeckRepository;
import com.tft.batch.repository.ParticipantRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetaAnalysisService {

    private final ParticipantRepository participantRepository;
    private final MetaDeckRepository metaDeckRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void analyzeMeta() {
        log.info("Starting Sophisticated Meta Analysis...");
        
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        List<Participant> participants = participantRepository.findAllWithTraits(threeDaysAgo);
        
        if (participants.isEmpty()) {
            log.info("No match data found.");
            return;
        }

        participantRepository.fetchUnits(participants);
        log.info("Total participants to analyze: {}", participants.size());

        // 1. 덱 그룹화 (유닛 조합 기반)
        Map<String, List<Participant>> deckGroups = new HashMap<>();
        for (Participant p : participants) {
            String deckKey = generateDeckKey(p);
            if (deckKey != null) {
                deckGroups.computeIfAbsent(deckKey, k -> new ArrayList<>()).add(p);
            }
        }

        // 2. 초기화
        metaDeckRepository.deleteAll();

        // 3. 덱 통계 계산
        List<MetaDeck> newDecks = new ArrayList<>();
        double totalGames = participants.size() / 8.0;

        for (Map.Entry<String, List<Participant>> entry : deckGroups.entrySet()) {
            List<Participant> group = entry.getValue();
            if (group.size() < 5) continue; // 표본 최소 5개

            String mainTrait = entry.getKey();
            
            // 통계 계산
            double avgPlacement = group.stream().mapToInt(Participant::getPaPlacement).average().orElse(8.0);
            long wins = group.stream().filter(p -> p.getPaPlacement() == 1).count();
            long top4 = group.stream().filter(p -> p.getPaPlacement() <= 4).count();

            // 상세 정보 추출
            List<UnitInfo> coreUnits = calculateCoreUnits(group);
            List<String> coreTraits = calculateCoreTraits(group);
            List<String> keyAugments = calculateKeyAugments(group);

            try {
                MetaDeck deck = MetaDeck.builder()
                        .name(generateDeckName(mainTrait, coreUnits))
                        .coreUnits(objectMapper.writeValueAsString(coreUnits))
                        .traits(objectMapper.writeValueAsString(coreTraits))
                        .keyAugments(objectMapper.writeValueAsString(keyAugments))
                        .avgPlacement(avgPlacement)
                        .winRate((double) wins / group.size() * 100.0)
                        .top4Rate((double) top4 / group.size() * 100.0)
                        .pickRate((double) group.size() / totalGames * 100.0)
                        .updatedAt(LocalDateTime.now())
                        .build();

                // 티어 결정 로직 개선
                double score = (8.0 - avgPlacement) * 2 + (wins / (double)group.size() * 20);
                if (score > 10) deck.setTier("S");
                else if (score > 8) deck.setTier("A");
                else if (score > 6) deck.setTier("B");
                else deck.setTier("C");

                newDecks.add(deck);
            } catch (Exception e) {
                log.error("Error creating MetaDeck", e);
            }
        }

        // 4. 정렬 및 저장
        newDecks.sort((d1, d2) -> Double.compare(d1.getAvgPlacement(), d2.getAvgPlacement()));
        if (newDecks.size() > 30) newDecks = newDecks.subList(0, 30);
        
        metaDeckRepository.saveAll(newDecks);
        log.info("Analysis complete. Saved {} decks.", newDecks.size());
    }

    private String generateDeckKey(Participant p) {
        // 가장 단계가 높은 시너지를 키로 사용 (예: "7 Pentakill")
        return p.getTraits().stream()
                .filter(t -> t.getTrStyle() >= 1)
                .max(Comparator.comparingInt(Trait::getTrStyle)
                        .thenComparingInt(Trait::getTrNumUnits))
                .map(t -> t.getTrNumUnits() + " " + t.getTrName())
                .orElse(null);
    }

    private List<UnitInfo> calculateCoreUnits(List<Participant> group) {
        Map<String, Integer> unitCounts = new HashMap<>();
        Map<String, Map<String, Integer>> itemStats = new HashMap<>();
        Map<String, Integer> unitCosts = new HashMap<>();

        for (Participant p : group) {
            for (Unit u : p.getUnits()) {
                unitCounts.merge(u.getUnId(), 1, Integer::sum);
                unitCosts.put(u.getUnId(), u.getUnCost());
                
                if (u.getItem() != null) {
                    processItem(u.getItem().getItFirst(), u.getUnId(), itemStats);
                    processItem(u.getItem().getItSecond(), u.getUnId(), itemStats);
                    processItem(u.getItem().getItThird(), u.getUnId(), itemStats);
                }
            }
        }

        return unitCounts.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(8)
                .map(e -> {
                    String unitId = e.getKey();
                    List<String> bestItems = itemStats.getOrDefault(unitId, Collections.emptyMap())
                            .entrySet().stream()
                            .sorted((i1, i2) -> i2.getValue().compareTo(i1.getValue()))
                            .limit(3)
                            .map(Map.Entry::getKey)
                            .collect(Collectors.toList());
                    
                    return UnitInfo.builder()
                            .name(unitId)
                            .cost(unitCosts.get(unitId))
                            .items(bestItems)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private void processItem(String itemName, String unitId, Map<String, Map<String, Integer>> itemStats) {
        if (itemName == null || itemName.isEmpty() || itemName.contains("Augment")) return;
        itemStats.computeIfAbsent(unitId, k -> new HashMap<>()).merge(itemName, 1, Integer::sum);
    }

    private List<String> calculateCoreTraits(List<Participant> group) {
        Map<String, Integer> traitCounts = new HashMap<>();
        for (Participant p : group) {
            for (Trait t : p.getTraits()) {
                if (t.getTrStyle() >= 1) {
                    traitCounts.merge(t.getTrName(), 1, Integer::sum);
                }
            }
        }
        return traitCounts.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private List<String> calculateKeyAugments(List<Participant> group) {
        Map<String, Integer> augmentCounts = new HashMap<>();
        for (Participant p : group) {
            if (p.getPaAugments() != null) {
                String[] augs = p.getPaAugments().split(",");
                for (String a : augs) {
                    augmentCounts.merge(a, 1, Integer::sum);
                }
            }
        }
        return augmentCounts.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(4)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private String generateDeckName(String mainTrait, List<UnitInfo> coreUnits) {
        // 메인 시너지 + 고코스트 핵심 유닛 이름
        String carry = coreUnits.stream()
                .filter(u -> u.getCost() >= 4)
                .findFirst()
                .map(UnitInfo::getName)
                .orElse("");
        return mainTrait + (carry.isEmpty() ? "" : " " + carry);
    }

    @Data
    @Builder
    public static class UnitInfo {
        private String name;
        private int cost;
        private List<String> items;
    }
}