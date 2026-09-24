package com.onlineStore.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Objeto de dominio puro: no conoce ningun detalle de infraestructura.
public record PriceModel(
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
