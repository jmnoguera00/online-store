package com.onlinestore.application.port.out;

import com.onlinestore.domain.model.Price;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Output port:
 * It returns ALL the candidate prices whose date range covers the application date (ranges may overlap).
 */
public interface LoadPricePort {

    List<Price> loadCandidatePrices(Long brandId, Long productId, LocalDateTime applicationDate);
}
