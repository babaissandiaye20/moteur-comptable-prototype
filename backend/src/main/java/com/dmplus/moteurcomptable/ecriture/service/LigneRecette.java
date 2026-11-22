package com.dmplus.moteurcomptable.ecriture.service;

import com.dmplus.moteurcomptable.shared.entity.Sens;

/**
 * Une ligne de recette : « mettre le montant nommé {@code montantSource} de l'événement,
 * dans la rubrique {@code rubrique}, du côté {@code sens} ».
 */
public record LigneRecette(int ordre, String rubrique, Sens sens, String montantSource, boolean ignorerSiZero) {
}
