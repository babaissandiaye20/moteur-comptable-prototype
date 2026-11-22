package com.dmplus.moteurcomptable.ecriture.dto;

import com.dmplus.moteurcomptable.ecriture.entity.Ecriture;
import com.dmplus.moteurcomptable.ecriture.entity.EcritureLigne;
import com.dmplus.moteurcomptable.shared.entity.Sens;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record EcritureDto(Long id, String numero, String journal, int exercice, String evenement,
                          Long recetteId, int recetteVersion, LocalDate dateValeur, LocalDate dateReglement,
                          String libelle, String statut, String creePar, LocalDateTime creeLe,
                          String valideePar, String annuleePar, BigDecimal totalDebit, BigDecimal totalCredit,
                          boolean equilibree, List<LigneDto> lignes) {

    public static EcritureDto de(Ecriture e) {
        BigDecimal debit = total(e.getLignes(), Sens.D);
        BigDecimal credit = total(e.getLignes(), Sens.C);
        return new EcritureDto(e.getId(), e.getNumero(), e.getJournalCode(), e.getExercice(), e.getEvenementCode(),
                e.getRecetteId(), e.getRecetteVersion(), e.getDateValeur(), e.getDateReglement(), e.getLibelle(),
                e.getStatut().name(), e.getCreatedBy(), e.getCreatedAt(), e.getValidatedBy(), e.getAnnuleePar(),
                debit, credit, debit.compareTo(credit) == 0,
                e.getLignes().stream().map(LigneDto::de).toList());
    }

    private static BigDecimal total(List<EcritureLigne> lignes, Sens sens) {
        return lignes.stream().filter(l -> l.getSens() == sens)
                .map(EcritureLigne::getMontant).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
