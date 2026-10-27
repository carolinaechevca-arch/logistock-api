package com.logistock.inventory.configuration.beans;

import com.logistock.inventory.infra.adapters.driving.http.controller.HealthController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.time.Clock;

@Configuration
public class HealthConfiguration {

    @Bean
    HealthController healthController(Environment environment, Clock clock) {
        return new HealthController(environment, clock);
    }
}