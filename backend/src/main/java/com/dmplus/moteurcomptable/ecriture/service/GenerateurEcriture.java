package com.dmplus.moteurcomptable.ecriture.service;

import com.dmplus.moteurcomptable.shared.entity.Sens;

import com.dmplus.moteurcomptable.shared.exception.MoteurException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Le cœur du moteur, en Java pur : recette + montants reçus + mapping = lignes d'écriture.
 * Aucune base de données, aucun Spring : on peut le tester seul.
 * Le moteur ne calcule aucun montant métier ; il range les montants reçus dans les bonnes cases.
 */
public final class GenerateurEcriture {

    private GenerateurEcriture() {
    }

    public static List<LigneProposee> generer(List<LigneRecette> recette,
                                              Map<String, BigDecimal> montants,
                                              ResolveurCompte resolveur) {
        if (recette.isEmpty()) {
            throw new MoteurException("RECETTE_VIDE", "La règle d'événement ne contient aucune ligne");
        }
        List<LigneProposee> lignes = new ArrayList<>();
        recette.stream()
               .sorted(Comparator.comparingInt(LigneRecette::ordre))
               .forEach(l -> {
                   BigDecimal montant = montants.get(l.montantSource());
                   if (montant == null && l.ignorerSiZero()) {
                       return; // ligne facultative et montant absent : pas de ligne
                   }
                   if (montant == null) {
                       throw new MoteurException("MONTANT_MANQUANT",
                               "L'événement ne contient pas le montant « " + l.montantSource() + " »");
                   }
                   if (montant.signum() < 0) {
                       throw new MoteurException("MONTANT_NEGATIF",
                               "Le montant « " + l.montantSource() + " » est négatif");
                   }
                   if (montant.signum() == 0 && l.ignorerSiZero()) {
                       return; // ligne facultative : pas de frais, pas de ligne de frais
                   }
                   String compte = resolveur.compteDe(l.rubrique());
                   lignes.add(new LigneProposee(l.ordre(), l.rubrique(), compte, l.sens(), montant));
               });
        verifierEquilibre(lignes);
        return List.copyOf(lignes);
    }

    /** Règle n° 1 : total des débits = total des crédits, et au moins une ligne de chaque côté. */
    static void verifierEquilibre(List<LigneProposee> lignes) {
        BigDecimal debit = total(lignes, Sens.D);
        BigDecimal credit = total(lignes, Sens.C);
        if (debit.signum() == 0 || credit.signum() == 0) {
            throw new MoteurException("ECRITURE_UNILATERALE",
                    "Une écriture a besoin d'au moins une ligne au débit et une au crédit");
        }
        if (debit.compareTo(credit) != 0) {
            throw new MoteurException("ECRITURE_DESEQUILIBREE",
                    "Débit " + debit.toPlainString() + " ≠ crédit " + credit.toPlainString());
        }
    }

    public static BigDecimal total(List<LigneProposee> lignes, Sens sens) {
        return lignes.stream()
                     .filter(l -> l.sens() == sens)
                     .map(LigneProposee::montant)
                     .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
