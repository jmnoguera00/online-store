package com.onlinestore.infrastructure.adapter.in.web;

import com.onlinestore.application.port.in.PriceQueryInputParams;
import com.onlinestore.domain.model.Price;
import com.onlinestore.application.port.in.GetApplicablePriceUseCase;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * Inbound adapter: translates HTTP requests into calls to the domain use case,
 * and its result into an HTTP response. It contains no business rules.
 */
@RestController
@RequestMapping("/api/v1/prices")
public class PriceController {

    private final GetApplicablePriceUseCase getApplicablePriceUseCase;

    public PriceController(GetApplicablePriceUseCase getApplicablePriceUseCase) {
        this.getApplicablePriceUseCase = getApplicablePriceUseCase;
    }

    @GetMapping
    public ResponseEntity<PriceResponse> getApplicablePrice(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime applicationDate,
            @RequestParam Long productId,
            @RequestParam Long brandId) {

        Price price = getApplicablePriceUseCase.getApplicablePrice(
                new PriceQueryInputParams(applicationDate, productId, brandId));

        return ResponseEntity.ok(PriceResponse.from(price));
    }
}
