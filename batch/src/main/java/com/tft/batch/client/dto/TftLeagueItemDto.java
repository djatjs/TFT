package com.tft.batch.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TftLeagueItemDto {
    @JsonProperty("puuid")
    private String puuid;
    
    @JsonProperty("summonerId")
    private String summonerId;

    @JsonProperty("leaguePoints")
    private int leaguePoints;
    
    @JsonProperty("rank")
    private String rank;
    
    @JsonProperty("wins")
    private int wins;
    
    @JsonProperty("losses")
    private int losses;
}
