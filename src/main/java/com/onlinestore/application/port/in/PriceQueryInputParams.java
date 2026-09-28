package com.onlinestore.application.port.in;

import java.time.LocalDateTime;

/**
 * Input of the "get applicable price" use case. It is part of the use case
 * contract, not an HTTP detail: the web adapter builds it from the request params.
 */
public record PriceQueryInputParams(
        LocalDateTime applicationDate,
        Long productId,
        Long brandId
) {
}
