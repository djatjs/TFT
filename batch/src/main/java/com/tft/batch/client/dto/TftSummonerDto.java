package com.tft.batch.client.dto;

import lombok.Data;

@Data
public class TftSummonerDto {
    private String id;
    private String accountId;
    private String puuid;
    private String name;
    private int profileIconId;
    private long revisionDate;
    private long summonerLevel;
}
