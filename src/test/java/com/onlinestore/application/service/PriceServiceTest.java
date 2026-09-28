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
 * Unit test of {@link PriceService}. No Spring context and no DB
 * The output port is mocked and only the business algorithm is verified.
 */
@ExtendWith(MockitoExtension.class)
class PriceServiceTest {

    private static final Long BRAND_ID = 1L;
    private static final Long PRODUCT_ID = 35455L;
    private static final LocalDateTime APPLICATION_DATE = LocalDateTime.of(2020, 6, 14, 16, 0, 0);
    private static final PriceQueryInputParams QUERY =
            new PriceQueryInputParams(APPLICATION_DATE, PRODUCT_ID, BRAND_ID);

    @Mock
    private LoadPricePort loadPricePort;

    private PriceService service;

    @BeforeEach
    void setUp() {
        service = new PriceService(loadPricePort);
    }

    @Test
    void singleCandidate_returnsThatCandidate() {
        Price only = priceWithPriority(0, "35.50");
        when(loadPricePort.loadCandidatePrices(BRAND_ID, PRODUCT_ID, APPLICATION_DATE))
                .thenReturn(List.of(only));

        assertThat(service.getApplicablePrice(QUERY)).isEqualTo(only);
    }

    @Test
    void overlappingCandidates_returnsHighestPriority() {
        Price lowPriority = priceWithPriority(0, "35.50");
        Price highPriority = priceWithPriority(1, "25.45");
        when(loadPricePort.loadCandidatePrices(BRAND_ID, PRODUCT_ID, APPLICATION_DATE))
                .thenReturn(List.of(lowPriority, highPriority));

        Price result = service.getApplicablePrice(QUERY);

        assertThat(result).isEqualTo(highPriority);
        assertThat(result.priority()).isEqualTo(1);
    }

    @Test
    void listOrderDoesNotMatter_returnsHighestPriority() {
        // Same scenario as above with the candidates in reverse order: the
        // algorithm must not depend on the order returned by the persistence layer.
        Price highPriority = priceWithPriority(1, "25.45");
        Price lowPriority = priceWithPriority(0, "35.50");
        when(loadPricePort.loadCandidatePrices(BRAND_ID, PRODUCT_ID, APPLICATION_DATE))
                .thenReturn(List.of(highPriority, lowPriority));

        assertThat(service.getApplicablePrice(QUERY)).isEqualTo(highPriority);
    }

    @Test
    void threeCandidatesWithMixedPriorities_returnsTheMaximum() {
        Price p0 = priceWithPriority(0, "35.50");
        Price p2 = priceWithPriority(2, "38.95");
        Price p1 = priceWithPriority(1, "30.50");
        when(loadPricePort.loadCandidatePrices(BRAND_ID, PRODUCT_ID, APPLICATION_DATE))
                .thenReturn(List.of(p0, p2, p1));

        assertThat(service.getApplicablePrice(QUERY)).isEqualTo(p2);
    }

    @Test
    void noCandidates_throwsPriceNotFoundException() {
        when(loadPricePort.loadCandidatePrices(BRAND_ID, PRODUCT_ID, APPLICATION_DATE))
                .thenReturn(List.of());

        assertThatThrownBy(() -> service.getApplicablePrice(QUERY))
                .isInstanceOf(PriceNotFoundException.class)
                .hasMessageContaining("brandId=" + BRAND_ID)
                .hasMessageContaining("productId=" + PRODUCT_ID);
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
