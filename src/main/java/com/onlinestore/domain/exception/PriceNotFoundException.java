package com.onlinestore.domain.exception;

import java.time.LocalDateTime;

/**
 * Business rule not satisfied: there is no applicable price for the requested criteria.
 */
public class PriceNotFoundException extends RuntimeException {

    public PriceNotFoundException(Long brandId, Long productId, LocalDateTime applicationDate) {
        super("No applicable price found for brandId=%d, productId=%d at %s"
                .formatted(brandId, productId, applicationDate));
    }
}
