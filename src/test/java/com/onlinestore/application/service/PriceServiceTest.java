package com.onlinestore.application.service;

import com.onlinestore.domain.exception.PriceNotFoundException;
import com.onlinestore.domain.model.Price;
import com.onlinestore.application.port.in.PriceQueryInputParams;
import com.onlinestore.application.port.out.LoadPricePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Test unitario del caso de uso PriceService:
 * se mockea el puerto de salida y se comprueba exclusivamente el algoritmo de negocio
 */
@ExtendWith(MockitoExtension.class)
class PriceServiceTest {

    private static final Long BRAND_ID = 1L;
    private static final Long PRODUCT_ID = 35455L;
    private static final LocalDateTime APPLICATION_DATE = LocalDateTime.of(2020, 6, 14, 16, 0, 0);

    @Mock
    private LoadPricePort loadPricePort;

    private PriceService service;

    @BeforeEach
    void setUp() {
        service = new PriceService(loadPricePort);
    }

    @Test
    void unSoloCandidato_devuelveEseCandidato() {
        Price unica = priceWithPriority(0, "35.50");
        when(loadPricePort.loadCandidatePrices(BRAND_ID, PRODUCT_ID, APPLICATION_DATE))
                .thenReturn(List.of(unica));

        Price resultado = service.getApplicablePrice(
                new PriceQueryInputParams(APPLICATION_DATE, PRODUCT_ID, BRAND_ID));

        assertThat(resultado).isEqualTo(unica);
    }

    @Test
    void variosCandidatosSolapados_eligeElDeMayorPrioridad() {
        Price prioridadBaja = priceWithPriority(0, "35.50");
        Price prioridadAlta = priceWithPriority(1, "25.45");

        when(loadPricePort.loadCandidatePrices(BRAND_ID, PRODUCT_ID, APPLICATION_DATE))
                .thenReturn(List.of(prioridadBaja, prioridadAlta));

        Price resultado = service.getApplicablePrice(
                new PriceQueryInputParams(APPLICATION_DATE, PRODUCT_ID, BRAND_ID));

        assertThat(resultado).isEqualTo(prioridadAlta);
        assertThat(resultado.priority()).isEqualTo(1);
    }

    @Test
    void tresCandidatosConPrioridadesMezcladas_eligeElMaximo() {
        Price p0 = priceWithPriority(0, "35.50");
        Price p2 = priceWithPriority(2, "38.95");
        Price p1 = priceWithPriority(1, "30.50");

        when(loadPricePort.loadCandidatePrices(BRAND_ID, PRODUCT_ID, APPLICATION_DATE))
                .thenReturn(List.of(p0, p2, p1));

        Price resultado = service.getApplicablePrice(
                new PriceQueryInputParams(APPLICATION_DATE, PRODUCT_ID, BRAND_ID));

        assertThat(resultado).isEqualTo(p2);
    }

    @Test
    void sinCandidatos_lanzaPriceNotFoundException() {
        when(loadPricePort.loadCandidatePrices(BRAND_ID, PRODUCT_ID, APPLICATION_DATE))
                .thenReturn(List.of());

        assertThatThrownBy(() -> service.getApplicablePrice(
                new PriceQueryInputParams(APPLICATION_DATE, PRODUCT_ID, BRAND_ID)))
                .isInstanceOf(PriceNotFoundException.class)
                .hasMessageContaining(String.valueOf(PRODUCT_ID))
                .hasMessageContaining(String.valueOf(BRAND_ID));
    }

    private Price priceWithPriority(int priority, String price) {
        return new Price(
                BRAND_ID,
                LocalDateTime.of(2020, 6, 14, 0, 0, 0),
                LocalDateTime.of(2020, 12, 31, 23, 59, 59),
                (long) (priority + 1),
                PRODUCT_ID,
                priority,
                new BigDecimal(price),
                "EUR"
        );
    }
}
