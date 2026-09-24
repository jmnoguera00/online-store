package com.onlineStore.domain.exception;

import java.time.LocalDateTime;

public class PriceNotFoundException extends RuntimeException {

    public PriceNotFoundException(Long brandId, Long productId, LocalDateTime applicationDate) {
        super("No existe tarifa aplicable para brandId=%d, productId=%d en la fecha %s"
                .formatted(brandId, productId, applicationDate));
    }
}
