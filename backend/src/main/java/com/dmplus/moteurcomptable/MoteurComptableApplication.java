package com.dmplus.moteurcomptable;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Point d'entrée : démarre Spring Boot, qui trouve les controllers, services et repositories. */
@SpringBootApplication
public class MoteurComptableApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoteurComptableApplication.class, args);
    }
}
