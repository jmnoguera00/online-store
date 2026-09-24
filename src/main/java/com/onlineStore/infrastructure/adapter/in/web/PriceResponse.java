package com.onlineStore.infrastructure.adapter.in.web;

import com.onlineStore.domain.model.PriceModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representacion HTTP de la respuesta.
 */
public record PriceResponse(
        Long productId,
        Long brandId,
        Long priceList,
        LocalDateTime startDate,
        LocalDateTime endDate,
        BigDecimal price,
        String curr
) {

    public static PriceResponse from(PriceModel price) {
        return new PriceResponse(
                price.productId(),
                price.brandId(),
                price.priceList(),
                price.startDate(),
                price.endDate(),
                price.price(),
                price.curr()
        );
    }
}
