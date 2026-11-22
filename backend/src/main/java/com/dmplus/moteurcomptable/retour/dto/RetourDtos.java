package com.dmplus.moteurcomptable.retour.dto;

import com.dmplus.moteurcomptable.retour.entity.StatutRetour;

public final class RetourDtos {
    private RetourDtos() {
    }

    /** Ajouter un point à la grille. */
    public record NouveauRetourDto(String fonctionnalite, String ouTester, String critere) {}

    /** Donner son avis sur un point. */
    public record AvisDto(StatutRetour statut, String observation) {}
}
