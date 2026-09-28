package com.onlinestore.application.port.in;

import com.onlinestore.domain.model.Price;

/**
 * Input port:
 * Any inbound adapter invokes this use case without knowing
 * how it is implemented or where the data comes from.
 */
public interface GetApplicablePriceUseCase {

    Price getApplicablePrice(PriceQueryInputParams query);
}
