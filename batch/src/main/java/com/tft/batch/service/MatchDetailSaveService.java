package com.tft.batch.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;

import org.springframework.stereotype.Service;

import com.tft.batch.client.dto.RiotMatchDetailResponse;
import com.tft.batch.model.entity.GameInfo;
import com.tft.batch.model.entity.Item;
import com.tft.batch.model.entity.Participant;
import com.tft.batch.model.entity.Trait;
import com.tft.batch.model.entity.Unit;
import com.tft.batch.repository.GameInfoRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchDetailSaveService {

    private final GameInfoRepository gameInfoRepository;

    @Transactional
    public void save(RiotMatchDetailResponse response) {
        if (gameInfoRepository.existsByGaId(response.getMetadata().getMatch_id())) {
            return;
        }

        RiotMatchDetailResponse.Info info = response.getInfo();

        GameInfo gameInfo = new GameInfo();
        gameInfo.setGaId(response.getMetadata().getMatch_id());
        
        LocalDateTime gameDateTime = Instant
                .ofEpochMilli(info.getGame_datetime())
                .atZone(ZoneId.of("Asia/Seoul"))
                .toLocalDateTime();
        gameInfo.setGaDatetime(gameDateTime);
        gameInfo.setGaVersion(info.getGame_version());
        gameInfo.setQueueId(info.getQueue_id());

        for (RiotMatchDetailResponse.Participant pDto : info.getParticipants()) {
            Participant p = new Participant();
            p.setPaPuuid(pDto.getPuuid());
            p.setPaPlacement(pDto.getPlacement());
            p.setPaLevel(pDto.getLevel());
            p.setPaGold(pDto.getGold_left());
            p.setPaName(pDto.getRiotIdGameName() != null ? pDto.getRiotIdGameName() : "");
            p.setPaTag(pDto.getRiotIdTagline() != null ? pDto.getRiotIdTagline() : "");
            
            if (pDto.getAugments() != null) {
                p.setPaAugments(String.join(",", pDto.getAugments()));
            }

            if (pDto.getCompanion() != null) {
                p.setPaCompanionId(pDto.getCompanion().getItem_ID());
            }

            p.setGameInfo(gameInfo);
            gameInfo.getParticipants().add(p);

            // Traits
            if (pDto.getTraits() != null) {
                for (RiotMatchDetailResponse.Trait tDto : pDto.getTraits()) {
                    if (tDto.getTier_current() > 0) {
                        Trait t = new Trait();
                        t.setTrName(tDto.getName());
                        t.setTrNumUnits(tDto.getNum_units());
                        t.setTrStyle(tDto.getStyle());
                        t.setParticipant(p);
                        p.getTraits().add(t);
                    }
                }
            }

            // Units
            if (pDto.getUnits() != null) {
                for (RiotMatchDetailResponse.Unit uDto : pDto.getUnits()) {
                    Unit u = new Unit();
                    u.setUnId(uDto.getCharacter_id());
                    u.setUnName(uDto.getCharacter_id()); // KoName은 웹에서 매핑
                    u.setUnTier(uDto.getTier());
                    u.setUnCost(uDto.getRarity()); // Riot API에서는 rarity가 cost 개념
                    u.setParticipant(p);
                    p.getUnits().add(u);

                    if (uDto.getItemNames() != null && !uDto.getItemNames().isEmpty()) {
                        Item item = new Item();
                        java.util.List<String> items = uDto.getItemNames();
                        item.setItFirst(items.size() > 0 ? items.get(0) : null);
                        item.setItSecond(items.size() > 1 ? items.get(1) : null);
                        item.setItThird(items.size() > 2 ? items.get(2) : null);
                        item.setUnit(u);
                        u.setItem(item);
                    }
                }
            }
        }

        gameInfoRepository.save(gameInfo);
    }
}
