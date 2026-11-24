package com.logistock.inventory.infra.adapters.driving.http.controller;

import com.logistock.inventory.infra.adapters.driving.http.constants.ApiPaths;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping(ApiPaths.HEALTH)
@Tag(name = "Health", description = "Comprobacion de disponibilidad")
public class HealthController {

    // Version visible de la aplicacion. Se cambia en el "cambio controlado" de la demo.
    static final String APP_VERSION = "v2";
    private static final String LOCAL = "local";

    private final Environment environment;
    private final Clock clock;
    private final ObjectProvider<BuildProperties> buildProperties;

    public HealthController(Environment environment, Clock clock, ObjectProvider<BuildProperties> buildProperties) {
        this.environment = environment;
        this.clock = clock;
        this.buildProperties = buildProperties;
    }

    @GetMapping
    @Operation(summary = "Indica si la API esta en ejecucion")
    public ResponseEntity<Map<String, Object>> health() {
        String[] activeProfiles = environment.getActiveProfiles();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("application", environment.getProperty("spring.application.name", "inventory"));
        body.put("environment", activeProfiles.length == 0 ? "default" : String.join(",", activeProfiles));
        body.put("version", APP_VERSION);
        // Trazabilidad: ejecucion de CI y commit que generaron el .jar desplegado.
        BuildProperties build = buildProperties.getIfAvailable();
        body.put("ciBuild", build == null ? LOCAL : valueOrLocal(build.get("ciBuild")));
        body.put("commit", build == null ? LOCAL : valueOrLocal(build.get("commit")));
        body.put("timestamp", Instant.now(clock).toString());
        return ResponseEntity.ok(body);
    }

    private static String valueOrLocal(String value) {
        return value == null || value.isBlank() ? LOCAL : value;
    }
}
