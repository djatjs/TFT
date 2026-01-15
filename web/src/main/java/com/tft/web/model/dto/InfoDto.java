package com.tft.web.model.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InfoDto {
    private long game_datetime;
    
    @JsonProperty("queueId")
    private int queue_id;
    
    private String game_mode;
    private List<ParticipantDto> participants;
}