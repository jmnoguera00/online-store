package com.onlinestore.infrastructure.adapter.out.persistence;

import com.onlinestore.application.port.out.LoadPricePort;
import com.onlinestore.domain.model.Price;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Adaptador de salida: implementa el puerto de dominio LoadPricePort usando JPA,
 * traduciendo entre la entidad tecnica PriceEntity y el modelo de dominio Price.
 */
@Component
class PriceAdapter implements LoadPricePort {

    private final PriceRepository repository;

    public PriceAdapter(PriceRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Price> loadCandidatePrices(Long brandId, Long productId, LocalDateTime applicationDate) {
        return repository.findByBrandIdAndProductIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        brandId, productId, applicationDate, applicationDate)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private Price toDomain(PriceEntity entity) {
        return new Price(
                entity.getBrandId(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getPriceList(),
                entity.getProductId(),
                entity.getPriority(),
                entity.getPrice(),
                entity.getCurr()
        );
    }
}
