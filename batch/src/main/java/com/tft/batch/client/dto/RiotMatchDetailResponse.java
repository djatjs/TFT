package com.tft.batch.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RiotMatchDetailResponse {

    private Metadata metadata;
    private Info info;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Metadata {
        private String match_id;
        private List<String> participants;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Info {
        private Long game_datetime;
        private String game_version;
        
        @JsonProperty("queueId")
        private Integer queue_id;
        
        private List<Participant> participants;
    }

    @Getter
    public static class Participant {
        private String puuid;
        private Integer placement;
        private Integer level;
        private Integer gold_left;
        private String riotIdGameName;
        private String riotIdTagline;
        private List<Unit> units;
        private List<Trait> traits;
        private List<String> augments;
        private Companion companion;
    }

    @Getter
    public static class Unit {
        private String character_id;
        private Integer tier;
        private Integer rarity;
        private List<String> itemNames;
    }

    @Getter
    public static class Trait {
        private String name;
        private Integer num_units;
        private Integer style;
        private Integer tier_current;
    }

    @Getter
    public static class Companion {
        private Integer item_ID;
    }
}