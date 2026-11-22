package com.dmplus.moteurcomptable.ecriture.service;

import com.dmplus.moteurcomptable.shared.entity.Sens;
import com.dmplus.moteurcomptable.shared.exception.MoteurException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Le cœur du moteur, testé sans base ni Spring. */
class GenerateurEcritureTest {

    private static final List<LigneRecette> ACHAT = List.of(
            new LigneRecette(1, "R01", Sens.D, "nominal", false),
            new LigneRecette(2, "R08", Sens.D, "frais", true),
            new LigneRecette(3, "R04", Sens.C, "total_paye", false));
    private static final ResolveurCompte MAPPING = rub -> switch (rub) {
        case "R01" -> "2745";
        case "R08" -> "6311";
        default -> "5211";
    };

    @Test
    void achat_produit_une_ecriture_equilibree() {
        var lignes = GenerateurEcriture.generer(ACHAT,
                Map.of("nominal", bd("2000000"), "frais", bd("20000"), "total_paye", bd("2020000")), MAPPING);
        assertThat(lignes).extracting(LigneProposee::compte).containsExactly("2745", "6311", "5211");
        assertThat(GenerateurEcriture.total(lignes, Sens.D)).isEqualByComparingTo("2020000");
        assertThat(GenerateurEcriture.total(lignes, Sens.C)).isEqualByComparingTo("2020000");
    }

    @Test
    void ligne_facultative_absente_ou_nulle_est_sautee() {
        var lignes = GenerateurEcriture.generer(ACHAT, Map.of("nominal", bd("100"), "total_paye", bd("100")), MAPPING);
        assertThat(lignes).extracting(LigneProposee::rubrique).containsExactly("R01", "R04");
    }

    @Test
    void desequilibre_refuse() {
        assertThatThrownBy(() -> GenerateurEcriture.generer(ACHAT,
                Map.of("nominal", bd("2000000"), "frais", bd("20000"), "total_paye", bd("2000000")), MAPPING))
                .isInstanceOf(MoteurException.class)
                .hasFieldOrPropertyWithValue("code", "ECRITURE_DESEQUILIBREE");
    }

    @Test
    void montant_obligatoire_manquant_refuse() {
        assertThatThrownBy(() -> GenerateurEcriture.generer(ACHAT, Map.of("nominal", bd("100")), MAPPING))
                .isInstanceOf(MoteurException.class)
                .hasFieldOrPropertyWithValue("code", "MONTANT_MANQUANT");
    }

    @Test
    void montant_negatif_refuse() {
        assertThatThrownBy(() -> GenerateurEcriture.generer(ACHAT,
                Map.of("nominal", bd("-5"), "total_paye", bd("-5")), MAPPING))
                .isInstanceOf(MoteurException.class)
                .hasFieldOrPropertyWithValue("code", "MONTANT_NEGATIF");
    }

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }
}
