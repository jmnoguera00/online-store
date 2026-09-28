package com.onlinestore.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Price rate of a product of a brand, applicable during a date range.
 * Pure domain object: it has no knowledge of how it is persisted or retrieved.
 */
public record Price(
        Long brandId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Long priceList,
        Long productId,
        Integer priority,
        BigDecimal price,
        String curr
) {
}
