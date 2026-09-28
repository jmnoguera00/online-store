package com.onlinestore.infrastructure.adapter.out.persistence;

import com.onlinestore.domain.model.Price;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the outbound persistence adapter: the date-range query returns all
 * the overlapping candidates and the entity-to-domain mapping is correct.
 * Choosing the winning price is NOT this layer's job
 */
@DataJpaTest
@Import(PriceAdapter.class)
class PriceAdapterTest {

    @Autowired
    private PriceAdapter adapter;

    @Autowired
    private PriceRepository repository;

    @Test
    void returnsAllOverlappingCandidatesForTheDate() {
        // At 16:00 on day 14, price list 1 (whole period, priority 0) and
        // price list 2 (15:00-18:30, priority 1) overlap.
        List<Price> candidates = adapter.loadCandidatePrices(
                1L, 35455L, LocalDateTime.of(2020, 6, 14, 16, 0, 0));

        assertThat(candidates).extracting(Price::priceList).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void dateOutsideAnyRange_returnsEmptyList() {
        List<Price> candidates = adapter.loadCandidatePrices(
                1L, 35455L, LocalDateTime.of(2021, 1, 1, 0, 0, 0));

        assertThat(candidates).isEmpty();
    }

    @Test
    void unknownProduct_returnsEmptyList() {
        List<Price> candidates = adapter.loadCandidatePrices(
                1L, 99999L, LocalDateTime.of(2020, 6, 14, 16, 0, 0));

        assertThat(candidates).isEmpty();
    }

    @Test
    void differentBrand_returnsEmptyList() {
        List<Price> candidates = adapter.loadCandidatePrices(
                2L, 35455L, LocalDateTime.of(2020, 6, 14, 16, 0, 0));

        assertThat(candidates).isEmpty();
    }

    @Test
    void rangeBoundariesAreInclusive() {
        // Price list 2 covers exactly 2020-06-14 15:00:00 to 2020-06-14 18:30:00
        assertThat(adapter.loadCandidatePrices(1L, 35455L, LocalDateTime.of(2020, 6, 14, 15, 0, 0)))
                .extracting(Price::priceList).contains(2L);
        assertThat(adapter.loadCandidatePrices(1L, 35455L, LocalDateTime.of(2020, 6, 14, 18, 30, 0)))
                .extracting(Price::priceList).contains(2L);
    }

    @Test
    void mapsEveryEntityFieldToTheDomainPrice() {
        // Every field gets a distinct value so that any crossed mapping is detected.
        PriceEntity entity = new PriceEntity();
        entity.setBrandId(7L);
        entity.setStartDate(LocalDateTime.of(2030, 1, 10, 8, 0, 0));
        entity.setEndDate(LocalDateTime.of(2030, 2, 20, 18, 30, 0));
        entity.setPriceList(4L);
        entity.setProductId(900L);
        entity.setPriority(5);
        entity.setPrice(new BigDecimal("12.34"));
        entity.setCurr("USD");
        repository.save(entity);

        List<Price> result = adapter.loadCandidatePrices(7L, 900L, LocalDateTime.of(2030, 1, 15, 12, 0, 0));

        Price expected = new Price(
                7L,
                LocalDateTime.of(2030, 1, 10, 8, 0, 0),
                LocalDateTime.of(2030, 2, 20, 18, 30, 0),
                4L,
                900L,
                5,
                new BigDecimal("12.34"),
                "USD");
        assertThat(result)
                .singleElement()
                .usingRecursiveComparison()
                .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .isEqualTo(expected);
    }
}
