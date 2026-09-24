package com.onlineStore.application.port.in;

import com.onlineStore.domain.model.PriceModel;

/**
 * Puerto de entrada de la aplicación (input port)
 */
public interface GetApplicablePriceUseCase {

    PriceModel getApplicablePrice(PriceQueryInputParams query);
}
