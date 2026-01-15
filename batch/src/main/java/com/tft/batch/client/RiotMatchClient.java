package com.tft.batch.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.tft.batch.client.dto.RiotMatchDetailResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RiotMatchClient {

    private final RestTemplate restTemplate;

    @Value("${riot.api.key}")
    private String apiKey;

    public RiotMatchDetailResponse fetchMatchDetail(String matchId) {
        String url = "https://asia.api.riotgames.com/tft/match/v1/matches/"
                + matchId
                + "?api_key=" + apiKey;

        return restTemplate.getForObject(url, RiotMatchDetailResponse.class);
    }

    public java.util.List<String> fetchMatchIds(String puuid, int start, int count, Long startTime) {
        String url = "https://asia.api.riotgames.com/tft/match/v1/matches/by-puuid/"
                + puuid
                + "/ids?start=" + start + "&count=" + count 
                + (startTime != null ? "&startTime=" + startTime : "")
                + "&api_key=" + apiKey;

        return restTemplate.getForObject(url, java.util.List.class);
    }
}