package com.onlinestore.application.port.in;

import com.onlinestore.domain.model.Price;

/**
 * Puerto de entrada de la aplicación (input port)
 */
public interface GetApplicablePriceUseCase {

    Price getApplicablePrice(PriceQueryInputParams query);
}
