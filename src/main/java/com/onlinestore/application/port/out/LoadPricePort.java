package com.onlinestore.application.port.out;

import com.onlinestore.domain.model.Price;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Puerto de salida de la aplicación (output port)
 * Devuelve TODAS las tarifas candidatas cuyo rango de fechas cubre la fecha de aplicacion
 */
public interface LoadPricePort {

    List<Price> loadCandidatePrices(Long brandId, Long productId, LocalDateTime applicationDate);
}
