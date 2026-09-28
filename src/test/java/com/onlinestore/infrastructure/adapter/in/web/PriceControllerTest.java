package com.onlinestore.infrastructure.adapter.in.web;

import com.onlinestore.application.port.in.PriceQueryInputParams;
import com.onlinestore.application.port.in.GetApplicablePriceUseCase;
import com.onlinestore.domain.exception.PriceNotFoundException;
import com.onlinestore.domain.model.Price;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-slice test of the inbound adapter. The use case is mocked, so this test
 * only covers what belongs to the adapter: request binding, error handling,
 * the shape of the error body and the mapping to {@link PriceResponse}.
 */
@WebMvcTest(PriceController.class)
class PriceControllerTest {

    private static final String URL = "/api/v1/prices";
    private static final LocalDateTime DATE = LocalDateTime.of(2020, 6, 14, 16, 0, 0);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetApplicablePriceUseCase useCase;

    @Test
    void validRequest_delegatesToTheUseCaseAndMapsTheResponse() throws Exception {
        Price price = new Price(1L,
                LocalDateTime.of(2020, 6, 14, 15, 0, 0),
                LocalDateTime.of(2020, 6, 14, 18, 30, 0),
                2L, 35455L, 1, new BigDecimal("25.45"), "EUR");
        when(useCase.getApplicablePrice(new PriceQueryInputParams(DATE, 35455L, 1L))).thenReturn(price);

        performGet(validParams())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId", is(35455)))
                .andExpect(jsonPath("$.brandId", is(1)))
                .andExpect(jsonPath("$.priceList", is(2)))
                .andExpect(jsonPath("$.startDate", is("2020-06-14T15:00:00")))
                .andExpect(jsonPath("$.endDate", is("2020-06-14T18:30:00")))
                .andExpect(jsonPath("$.price", is(25.45)))
                .andExpect(jsonPath("$.curr", is("EUR")));
    }

    @Test
    void priceNotFound_returns404WithErrorBody() throws Exception {
        when(useCase.getApplicablePrice(any()))
                .thenThrow(new PriceNotFoundException(1L, 35455L, DATE));

        performGet(validParams())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("productId=35455")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @ParameterizedTest(name = "missing parameter: {0}")
    @ValueSource(strings = {"applicationDate", "productId", "brandId"})
    void missingParameter_returns400WithErrorBody(String missingParameter) throws Exception {
        Map<String, String> params = validParams();
        params.remove(missingParameter);

        performGet(params)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("'" + missingParameter + "' is missing")))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(useCase);
    }

    @ParameterizedTest(name = "invalid {0} = \"{1}\"")
    @CsvSource({
            "applicationDate,not-a-date",
            "applicationDate,2020-13-45T10:00:00",
            "applicationDate,14/06/2020",
            "productId,abc",
            "productId,12.5",
            "brandId,abc"
    })
    void invalidParameterType_returns400WithErrorBody(String parameter, String invalidValue) throws Exception {
        Map<String, String> params = validParams();
        params.put(parameter, invalidValue);

        performGet(params)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("'" + parameter + "'")))
                .andExpect(jsonPath("$.message", containsString("'" + invalidValue + "'")))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(useCase);
    }

    private static Map<String, String> validParams() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("applicationDate", "2020-06-14T16:00:00");
        params.put("productId", "35455");
        params.put("brandId", "1");
        return params;
    }

    private ResultActions performGet(Map<String, String> params) throws Exception {
        MockHttpServletRequestBuilder request = get(URL);
        params.forEach(request::param);
        return mockMvc.perform(request);
    }
}
