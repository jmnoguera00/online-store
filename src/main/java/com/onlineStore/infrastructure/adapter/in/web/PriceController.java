package com.onlineStore.infrastructure.adapter.in.web;

import com.onlineStore.domain.model.PriceModel;
import com.onlineStore.application.port.in.PriceQueryInputParams;
import com.onlineStore.application.port.in.GetApplicablePriceUseCase;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * Adaptador de entrada (input adapter) en este caso del tipo REST
 */
@RestController
@RequestMapping("/api/v1/prices")
@Validated
public class PriceController {

    private final GetApplicablePriceUseCase getApplicablePriceUseCase;

    public PriceController(GetApplicablePriceUseCase getApplicablePriceUseCase) {
        this.getApplicablePriceUseCase = getApplicablePriceUseCase;
    }

    /**
     * GET /api/v1/prices?applicationDate=2020-06-14T16:00:00&productId=35455&brandId=1
     */
    @GetMapping
    public ResponseEntity<PriceResponse> getApplicablePrice(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @NotNull LocalDateTime applicationDate,
            @RequestParam @NotNull Long productId,
            @RequestParam @NotNull Long brandId) {

        PriceModel price = getApplicablePriceUseCase.getApplicablePrice(
                new PriceQueryInputParams(applicationDate, productId, brandId));

        return ResponseEntity.ok(PriceResponse.from(price));
    }
}
