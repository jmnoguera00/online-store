package com.onlinestore.application.service;

import com.onlinestore.application.port.in.PriceQueryInputParams;
import com.onlinestore.application.port.in.GetApplicablePriceUseCase;
import com.onlinestore.application.port.out.LoadPricePort;
import com.onlinestore.domain.exception.PriceNotFoundException;
import com.onlinestore.domain.model.Price;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Caso de uso "obtener tarifa aplicable". Depende unicamente de puertos
 */
@Service
public class PriceService implements GetApplicablePriceUseCase {

    private final LoadPricePort loadPricePort;

    public PriceService(LoadPricePort loadPricePort) {
        this.loadPricePort = loadPricePort;
    }

    @Override
    public Price getApplicablePrice(PriceQueryInputParams query) {
        List<Price> candidates = loadPricePort.loadCandidatePrices(
                query.brandId(), query.productId(), query.applicationDate());

        //de entre todas las tarifas cuyo rango de fechas cubre la fecha solicitada, se aplica la de mayor prioridad.
        return candidates.stream()
                .max(Comparator.comparingInt(Price::priority))
                .orElseThrow(() -> new PriceNotFoundException(
                        query.brandId(), query.productId(), query.applicationDate()));
    }
}
