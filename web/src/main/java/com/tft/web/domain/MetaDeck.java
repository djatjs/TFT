package com.tft.web.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "meta_deck")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetaDeck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; 
    
    @Column(columnDefinition = "TEXT")
    private String coreUnits; 
    
    @Column(columnDefinition = "TEXT")
    private String traits; 
    
    @Column(columnDefinition = "TEXT")
    private String keyAugments; 

    private double avgPlacement;
    private double winRate;
    private double top4Rate;
    private double pickRate;
    
    private String tier;

    private LocalDateTime updatedAt;
}