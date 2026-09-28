package com.onlinestore.application.service;

import com.onlinestore.application.port.in.PriceQueryInputParams;
import com.onlinestore.application.port.in.GetApplicablePriceUseCase;
import com.onlinestore.application.port.out.LoadPricePort;
import com.onlinestore.domain.exception.PriceNotFoundException;
import com.onlinestore.domain.model.Price;

import java.util.Comparator;
import java.util.List;

/**
 * "Get applicable price" use case. It only depends on ports (interfaces) and on the domain
 * The bean is declared in infrastructure layer (see {@code UseCaseConfiguration}),
 * which keeps this class free of any framework annotation and
 * trivially unit-testable with a mocked output port.
 */
public class PriceService implements GetApplicablePriceUseCase {

    private final LoadPricePort loadPricePort;

    public PriceService(LoadPricePort loadPricePort) {
        this.loadPricePort = loadPricePort;
    }

    @Override
    public Price getApplicablePrice(PriceQueryInputParams query) {
        List<Price> candidates = loadPricePort.loadCandidatePrices(
                query.brandId(), query.productId(), query.applicationDate());

        // Resolution algorithm: among all the prices whose date range covers the
        // requested date, the one with the highest priority wins.
        return candidates.stream()
                .max(Comparator.comparingInt(Price::priority))
                .orElseThrow(() -> new PriceNotFoundException(
                        query.brandId(), query.productId(), query.applicationDate()));
    }
}
