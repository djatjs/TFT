package com.tft.batch.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
@Table(name = "item")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IT_NUM")
    private Integer itNum;

    @Column(name = "IT_FIRST", length = 100)
    private String itFirst;

    @Column(name = "IT_SECOND", length = 100)
    private String itSecond;

    @Column(name = "IT_THIRD", length = 100)
    private String itThird;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IT_UN_NUM")
    private Unit unit;
}
