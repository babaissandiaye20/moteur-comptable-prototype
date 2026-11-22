package com.dmplus.moteurcomptable.shared.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Point de contrôle léger. Appelé toutes les 10 minutes par un service externe (cron)
 * pour empêcher l'hébergeur gratuit de mettre l'application en veille.
 */
@RestController
public class SanteController {

    @GetMapping("/api/sante")
    public Map<String, String> sante() {
        return Map.of("statut", "ok");
    }
}
