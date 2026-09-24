package com.onlineStore.application.service;

import com.onlineStore.application.port.in.PriceQueryInputParams;
import com.onlineStore.application.port.in.GetApplicablePriceUseCase;
import com.onlineStore.application.port.out.LoadPricePort;
import com.onlineStore.domain.exception.PriceNotFoundException;
import com.onlineStore.domain.model.PriceModel;
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
    public PriceModel getApplicablePrice(PriceQueryInputParams query) {
        List<PriceModel> candidates = loadPricePort.loadCandidatePrices(
                query.brandId(), query.productId(), query.applicationDate());

        //de entre todas las tarifas cuyo rango de fechas cubre la fecha solicitada, se aplica la de mayor prioridad.
        return candidates.stream()
                .max(Comparator.comparingInt(PriceModel::priority))
                .orElseThrow(() -> new PriceNotFoundException(
                        query.brandId(), query.productId(), query.applicationDate()));
    }
}
