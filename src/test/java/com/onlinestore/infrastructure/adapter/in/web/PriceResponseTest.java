package com.onlinestore.infrastructure.adapter.in.web;

import com.onlinestore.domain.model.Price;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PriceResponseTest {

    @Test
    void from_mapsEveryDomainFieldToTheResponse() {
        // Every field gets a distinct value so that any crossed mapping is detected.
        Price price = new Price(
                7L,
                LocalDateTime.of(2030, 1, 10, 8, 0, 0),
                LocalDateTime.of(2030, 2, 20, 18, 30, 0),
                4L,
                900L,
                5,
                new BigDecimal("12.34"),
                "USD");

        PriceResponse response = PriceResponse.from(price);

        assertThat(response.productId()).isEqualTo(900L);
        assertThat(response.brandId()).isEqualTo(7L);
        assertThat(response.priceList()).isEqualTo(4L);
        assertThat(response.startDate()).isEqualTo(LocalDateTime.of(2030, 1, 10, 8, 0, 0));
        assertThat(response.endDate()).isEqualTo(LocalDateTime.of(2030, 2, 20, 18, 30, 0));
        assertThat(response.price()).isEqualByComparingTo("12.34");
        assertThat(response.curr()).isEqualTo("USD");
    }
}
