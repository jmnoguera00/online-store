package com.onlineStore.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

interface PriceRepository extends JpaRepository<Price, Long> {

    List<Price> findByBrandIdAndProductIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long brandId,
            Long productId,
            LocalDateTime applicationDateForStartCheck,
            LocalDateTime applicationDateForEndCheck
    );
}
