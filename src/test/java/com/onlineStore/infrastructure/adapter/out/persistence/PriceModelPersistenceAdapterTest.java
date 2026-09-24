package com.onlineStore.infrastructure.adapter.out.persistence;

import com.onlineStore.domain.model.PriceModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica que el adaptador de persistencia traduce correctamente entre la
 * entidad JPA y el modelo de dominio, y que la query de rango de fechas
 * devuelve todos los candidatos solapados.
 */
@DataJpaTest
@Import(PriceAdapter.class)
class PriceModelPersistenceAdapterTest {

    @Autowired
    private PriceAdapter pricePersistenceAdapter;

    @Test
    void devuelveTodosLosCandidatosSolapadosEnLaFecha() {
        // A las 16:00 del dia 14 solapan la tarifa 1 (0-24h, prioridad 0) y la tarifa 2 (15:00-18:30, prioridad 1)
        List<PriceModel> candidatos = pricePersistenceAdapter.loadCandidatePrices(
                1L, 35455L, LocalDateTime.of(2020, 6, 14, 16, 0, 0));

        assertThat(candidatos).hasSize(2);
        assertThat(candidatos).extracting(PriceModel::priceList).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void fueraDeCualquierRango_devuelveListaVacia() {
        List<PriceModel> candidatos = pricePersistenceAdapter.loadCandidatePrices(
                1L, 35455L, LocalDateTime.of(2021, 1, 1, 0, 0, 0));

        assertThat(candidatos).isEmpty();
    }

    @Test
    void productoOcadenaDistintos_noDevuelveCandidatos() {
        List<PriceModel> candidatos = pricePersistenceAdapter.loadCandidatePrices(
                1L, 99999L, LocalDateTime.of(2020, 6, 14, 16, 0, 0));

        assertThat(candidatos).isEmpty();
    }
}
