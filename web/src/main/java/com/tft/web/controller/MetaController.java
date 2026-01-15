package com.tft.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tft.web.domain.MetaDeck;
import com.tft.web.repository.MetaDeckRepository;
import com.tft.web.service.TftStaticDataService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class MetaController {

    private final MetaDeckRepository metaDeckRepository;
    private final TftStaticDataService staticDataService;
    private final ObjectMapper objectMapper;

    @GetMapping("/meta")
    public String meta(Model model) {
        List<MetaDeck> decks = metaDeckRepository.findAllByOrderByAvgPlacementAsc();

        List<MetaDeckDto> dtos = decks.stream().map(this::convertToDto).collect(Collectors.toList());

        model.addAttribute("decks", dtos);
        return "tft/meta";
    }

    private MetaDeckDto convertToDto(MetaDeck deck) {
        MetaDeckDto dto = new MetaDeckDto();
        dto.setId(deck.getId());
        dto.setAvgPlacement(deck.getAvgPlacement());
        dto.setWinRate(deck.getWinRate());
        dto.setTop4Rate(deck.getTop4Rate());
        dto.setPickRate(deck.getPickRate());
        dto.setTier(deck.getTier());

        try {
            // 핵심 유닛 (아이템 포함) 먼저 파싱
            List<UnitInfoInput> unitInputs = objectMapper.readValue(deck.getCoreUnits(), new TypeReference<List<UnitInfoInput>>() {});
            dto.setCoreUnits(unitInputs.stream().map(ui -> {
                UnitDto ud = new UnitDto();
                ud.setName(staticDataService.getUnitKoName(ui.getName()));
                ud.setImgUrl(staticDataService.getUnitImgUrl(ui.getName()));
                ud.setCost(ui.getCost());
                ud.setItems(ui.getItems().stream().map(item -> 
                    new ItemDto(staticDataService.getItemKoName(item), staticDataService.getItemImgUrlByName(item))
                ).collect(Collectors.toList()));
                return ud;
            }).collect(Collectors.toList()));

            // 덱 이름 가공 (한글 시너지 + 핵심 캐리 유닛)
            String deckName = "";
            
            // 1. 시너지 이름 추출 및 한글화 (더 유연한 파싱)
            // deck.getName() 형식 예: "6 TFT16_Ambusher TFT16_Garen"
            String fullRawName = deck.getName();
            String[] parts = fullRawName.split(" ");
            
            String count = "";
            String traitId = "";
            
            if (parts.length >= 2) {
                // 첫 번째 파트가 숫자(시너지 개수)인 경우
                if (parts[0].matches("[0-9]+")) {
                    count = parts[0];
                    traitId = parts[1].replace("TFT16_", "");
                } else {
                    // 숫자가 붙어 있는 경우 (예: "6TFT16_Ambusher")
                    count = parts[0].replaceAll("[^0-9]", "");
                    traitId = parts[0].replaceAll("[0-9]", "").replace("TFT16_", "").replace("_", "");
                }
            } else {
                // 공백이 없는 경우
                count = fullRawName.replaceAll("[^0-9]", "");
                traitId = fullRawName.replaceAll("[0-9]", "").replace("TFT16_", "").replace("_", "");
            }
            
            // 한글 시너지명 찾기
            String koTrait = staticDataService.getTraitKoName("TFT16_" + traitId);
            if (koTrait.equals("TFT16_" + traitId)) {
                koTrait = staticDataService.getTraitKoName(traitId);
            }
            
            // 최종 덱 이름 조합 (예: 6 매복자)
            deckName = (count.isEmpty() ? "" : count + " ") + koTrait;
            dto.setMainTraitIcon(staticDataService.getTraitIconUrl("TFT16_" + traitId));

            // 2. 캐리 유닛(아이템이 가장 많은 유닛) 찾아서 이름에 추가
            UnitInfoInput carryUnit = unitInputs.stream()
                .max((u1, u2) -> {
                    int c1 = u1.getItems().size();
                    int c2 = u2.getItems().size();
                    if (c1 != c2) return Integer.compare(c1, c2);
                    return Integer.compare(u1.getCost(), u2.getCost()); // 아이템 수가 같으면 고코스트 우선
                })
                .orElse(null);
            
            if (carryUnit != null) {
                deckName += " " + staticDataService.getUnitKoName(carryUnit.getName());
            }
            dto.setName(deckName);

            // 주요 시너지
            List<String> traitNames = objectMapper.readValue(deck.getTraits(), new TypeReference<List<String>>() {});
            dto.setTraits(traitNames.stream().map(t -> 
                new TraitDto(staticDataService.getTraitKoName(t), staticDataService.getTraitIconUrl(t))
            ).collect(Collectors.toList()));

            // 핵심 증강체
            List<String> augmentNames = objectMapper.readValue(deck.getKeyAugments(), new TypeReference<List<String>>() {});
            dto.setKeyAugments(augmentNames.stream().map(a -> 
                new AugmentDto(staticDataService.getItemKoName(a), staticDataService.getItemImgUrlByName(a))
            ).collect(Collectors.toList()));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return dto;
    }

    @Data
    public static class MetaDeckDto {
        private Long id;
        private String name;
        private String mainTraitIcon;
        private double avgPlacement;
        private double winRate;
        private double top4Rate;
        private double pickRate;
        private String tier;
        private List<UnitDto> coreUnits;
        private List<TraitDto> traits;
        private List<AugmentDto> keyAugments;
    }

    @Data
    public static class UnitDto {
        private String name;
        private String imgUrl;
        private int cost;
        private List<ItemDto> items;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemDto {
        private String name;
        private String imgUrl;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TraitDto {
        private String name;
        private String iconUrl;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AugmentDto {
        private String name;
        private String imgUrl;
    }

    @Data
    public static class UnitInfoInput {
        private String name;
        private int cost;
        private List<String> items;
    }
}