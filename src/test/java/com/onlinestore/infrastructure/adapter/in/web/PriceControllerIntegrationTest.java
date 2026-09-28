package com.onlinestore.infrastructure.adapter.in.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end test across the whole hexagonal stack for the five scenarios
 * of the technical brief plus a negative case.
 * Every scenario verifies the complete output contract, not just the price.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PriceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String URL = "/api/v1/prices";
    private static final String PRODUCT_ID = "35455";
    private static final String BRAND_ID = "1";


    @ParameterizedTest(name = "[{index}] request at {0} -> price list {1}, price {4}")
    @CsvSource(textBlock = """
            # applicationDate,priceList,startDate,endDate,price
            2020-06-14T10:00:00,1,2020-06-14T00:00:00,2020-12-31T23:59:59,35.50
            2020-06-14T16:00:00,2,2020-06-14T15:00:00,2020-06-14T18:30:00,25.45
            2020-06-14T21:00:00,1,2020-06-14T00:00:00,2020-12-31T23:59:59,35.50
            2020-06-15T10:00:00,3,2020-06-15T00:00:00,2020-06-15T11:00:00,30.50
            2020-06-16T21:00:00,4,2020-06-15T16:00:00,2020-12-31T23:59:59,38.95
            """)
    void returnsTheApplicablePrice(String applicationDate, int expectedPriceList,
                                   String expectedStartDate, String expectedEndDate,
                                   double expectedPrice) throws Exception {
        mockMvc.perform(get(URL)
                        .param("applicationDate", applicationDate)
                        .param("productId", PRODUCT_ID)
                        .param("brandId", BRAND_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId", is(35455)))
                .andExpect(jsonPath("$.brandId", is(1)))
                .andExpect(jsonPath("$.priceList", is(expectedPriceList)))
                .andExpect(jsonPath("$.startDate", is(expectedStartDate)))
                .andExpect(jsonPath("$.endDate", is(expectedEndDate)))
                .andExpect(jsonPath("$.price", is(expectedPrice)))
                .andExpect(jsonPath("$.curr", is("EUR")));
    }

    @Test
    void dateWithoutAnyApplicablePrice_returns404() throws Exception {
        mockMvc.perform(get(URL)
                        .param("applicationDate", "2021-01-01T00:00:00")
                        .param("productId", PRODUCT_ID)
                        .param("brandId", BRAND_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    @Test
    void invalidParameter_returns400WithTheSameErrorShape() throws Exception {
        mockMvc.perform(get(URL)
                        .param("applicationDate", "2020-06-14T10:00:00")
                        .param("productId", "abc")
                        .param("brandId", BRAND_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")));
    }
}
