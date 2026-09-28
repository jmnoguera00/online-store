package com.onlinestore.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA interface. It is package-private on purpose:
 * only {@link PriceAdapter} should use it.
 */
interface PriceRepository extends JpaRepository<PriceEntity, Long> {

    List<PriceEntity> findByBrandIdAndProductIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long brandId,
            Long productId,
            LocalDateTime applicationDateForStartCheck,
            LocalDateTime applicationDateForEndCheck
    );
}
