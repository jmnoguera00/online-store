package com.onlinestore.infrastructure.config;

import com.onlinestore.application.port.in.GetApplicablePriceUseCase;
import com.onlinestore.application.port.out.LoadPricePort;
import com.onlinestore.application.service.PriceService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the application use cases into the Spring context.
 * Declaring beans here instead of annotating the use case
 * classes keeps the application layer independent from framework.
 */
@Configuration(proxyBeanMethods = false)
public class UseCaseConfiguration {

    @Bean
    public GetApplicablePriceUseCase getApplicablePriceUseCase(LoadPricePort loadPricePort) {
        return new PriceService(loadPricePort);
    }
}
