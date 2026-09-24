package com.onlineStore.application.port.in;

import java.time.LocalDateTime;

/**
 * Datos de entrada para la consulta de precios aplicables.
 */
public record PriceQueryInputParams(
        LocalDateTime applicationDate,
        Long productId,
        Long brandId
) {
}
