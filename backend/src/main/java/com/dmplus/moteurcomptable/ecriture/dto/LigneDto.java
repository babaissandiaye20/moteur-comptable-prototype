package com.dmplus.moteurcomptable.ecriture.dto;

import com.dmplus.moteurcomptable.ecriture.entity.EcritureLigne;

import java.math.BigDecimal;

public record LigneDto(int ordre, String rubrique, String compte, String sens,
                       BigDecimal debit, BigDecimal credit, String devise, String referenceTitre) {

    public static LigneDto de(EcritureLigne l) {
        boolean debit = l.getSens().name().equals("D");
        return new LigneDto(l.getOrdre(), l.getRubriqueCode(), l.getCompteCode(), l.getSens().name(),
                debit ? l.getMontant() : null, debit ? null : l.getMontant(), l.getDevise(), l.getReferenceTitre());
    }
}
