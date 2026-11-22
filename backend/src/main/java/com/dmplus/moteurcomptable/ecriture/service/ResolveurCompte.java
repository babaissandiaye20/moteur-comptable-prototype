package com.dmplus.moteurcomptable.ecriture.service;

/** Port : trouve le numéro de compte d'une rubrique (le « répertoire », c'est-à-dire le mapping). */
@FunctionalInterface
public interface ResolveurCompte {
    String compteDe(String rubrique);
}
