package com.onlinestore.infrastructure.adapter.in.web;

import com.onlinestore.domain.model.Price;

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

    public static PriceResponse from(Price price) {
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
