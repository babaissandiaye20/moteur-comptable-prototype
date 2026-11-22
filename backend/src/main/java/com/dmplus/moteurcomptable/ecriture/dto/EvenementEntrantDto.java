package com.dmplus.moteurcomptable.ecriture.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Ce que l'application source envoie : un fait métier avec ses montants nommés, jamais une écriture.
 * Exemple : { "source":"SIGAP", "eventId":"ACQ-0001", "evenement":"ACQ_OBLIGATION",
 *             "dateValeur":"2026-11-23", "classeActif":"OBLIGATION", "categorie":"ETAT",
 *             "montants": { "nominal": 2000000, "frais": 20000, "total_paye": 2020000 } }
 */
public record EvenementEntrantDto(
        String source,
        String eventId,
        String evenement,
        LocalDate dateValeur,
        LocalDate dateReglement,
        String classeActif,
        String categorie,
        String referenceTitre,
        String devise,
        String libelle,
        String utilisateur,
        Map<String, BigDecimal> montants) {
}
