package com.tft.batch.repository;

import com.tft.batch.model.entity.MetaDeck;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetaDeckRepository extends JpaRepository<MetaDeck, Long> {
}
