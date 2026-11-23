package com.logistock.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Punto de entrada de LogiStock API - verificado por la pipeline logistock-ci
// Prueba en vivo CI.
@SpringBootApplication(scanBasePackages = {
        "com.logistock.inventory.configuration.beans",
        "com.logistock.inventory.configuration.openapi"
})
public class InventoryApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryApplication.class, args);
	}

}
