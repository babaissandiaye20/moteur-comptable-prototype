package com.dmplus.moteurcomptable.ecriture.service;

import com.dmplus.moteurcomptable.shared.entity.Sens;

import java.math.BigDecimal;

/** Une ligne d'écriture produite par le moteur, avec son compte déjà résolu par le mapping. */
public record LigneProposee(int ordre, String rubrique, String compte, Sens sens, BigDecimal montant) {
}
