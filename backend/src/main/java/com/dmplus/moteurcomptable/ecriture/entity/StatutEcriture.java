package com.dmplus.moteurcomptable.ecriture.entity;

/**
 * Le cycle de vie d'une écriture. En enum (et non en table) parce que le code en dépend :
 * chaque transition autorisée est écrite ici.
 */
public enum StatutEcriture {
    PROPOSEE, VALIDEE, ANNULEE;

    public boolean peutPasserA(StatutEcriture cible) {
        return switch (this) {
            case PROPOSEE -> cible == VALIDEE || cible == ANNULEE;
            case VALIDEE, ANNULEE -> false; // une écriture validée ne bouge plus : on contre-passe
        };
    }
}
