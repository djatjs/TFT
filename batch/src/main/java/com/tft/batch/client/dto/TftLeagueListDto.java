package com.tft.batch.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TftLeagueListDto {
    private String tier;
    private String leagueId;
    private String queue;
    private String name;
    private List<TftLeagueItemDto> entries;
}
