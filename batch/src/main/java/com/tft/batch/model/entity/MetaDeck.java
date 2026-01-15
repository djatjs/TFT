package com.tft.batch.model.entity;

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
    private String coreUnits; // [{ name: "Ahri", items: ["Item1", "Item2"] }, ...]
    
    @Column(columnDefinition = "TEXT")
    private String traits; // ["Pentakill", "Executioner"]
    
    @Column(columnDefinition = "TEXT")
    private String keyAugments; // ["Augment1", "Augment2"]

    private double avgPlacement;
    private double winRate;
    private double top4Rate;
    private double pickRate;
    
    private String tier;

    private LocalDateTime updatedAt;
}