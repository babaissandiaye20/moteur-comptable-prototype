package com.dmplus.moteurcomptable.parametrage.entity;

import com.dmplus.moteurcomptable.shared.entity.Sens;

/** Les quatre natures d'une rubrique, et le sens dans lequel chacune augmente. */
public enum Nature {
    ACTIF(Sens.D),    // ce que la compagnie possède
    CHARGE(Sens.D),   // ce qu'elle dépense
    PASSIF(Sens.C),   // ce qu'elle doit, d'où vient l'argent
    PRODUIT(Sens.C);  // ce qu'elle gagne

    private final Sens sensAugmentation;

    Nature(Sens sensAugmentation) {
        this.sensAugmentation = sensAugmentation;
    }

    public Sens sensAugmentation() {
        return sensAugmentation;
    }
}
