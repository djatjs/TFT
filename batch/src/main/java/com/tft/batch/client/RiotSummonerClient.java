package com.tft.batch.client;

import com.tft.batch.client.dto.TftSummonerDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class RiotSummonerClient {

    private final RestTemplate restTemplate;

    @Value("${riot.api.key}")
    private String apiKey;

    public TftSummonerDto getSummonerById(String summonerId) {
        String url = "https://kr.api.riotgames.com/tft/summoner/v1/summoners/" + summonerId + "?api_key=" + apiKey;
        return restTemplate.getForObject(url, TftSummonerDto.class);
    }
}
