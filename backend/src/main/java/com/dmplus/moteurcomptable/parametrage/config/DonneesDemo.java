package com.dmplus.moteurcomptable.parametrage.config;

import com.dmplus.moteurcomptable.parametrage.entity.Nature;
import com.dmplus.moteurcomptable.shared.entity.Sens;
import com.dmplus.moteurcomptable.parametrage.repository.OperationRepository;
import com.dmplus.moteurcomptable.parametrage.service.ParametrageService;
import com.dmplus.moteurcomptable.parametrage.service.ParametrageService.LigneSaisie;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Remplit l'exemple de démonstration si la base est vide. Passe par ParametrageService,
 * donc par les mêmes contrôles qu'un vrai paramétrage. Comptes donnés à titre d'exemple.
 */
@Component
@ConditionalOnProperty(name = "moteur.donnees-demo", havingValue = "true")
public class DonneesDemo implements ApplicationRunner {

    private static final String AUTEUR = "parametrage.demo";

    private final ParametrageService p;
    private final OperationRepository operations;

    public DonneesDemo(ParametrageService p, OperationRepository operations) {
        this.p = p;
        this.operations = operations;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (operations.count() > 0) {
            return; // déjà paramétré : on ne touche à rien
        }
        p.creerOperation("ACHAT", "Achat");
        p.creerOperation("ENCAISSEMENT", "Encaissement");
        p.creerOperation("REMBOURSEMENT", "Remboursement");

        p.creerJournal("PLA", "Placements", "PLC");
        p.creerJournal("REV", "Revenus des placements", "OD");

        p.creerRubrique("R01", "Valeur d'inscription du titre", Nature.ACTIF);
        p.creerRubrique("R04", "Compte de règlement (banque)", Nature.ACTIF);
        p.creerRubrique("R05", "Revenus des placements", Nature.PRODUIT);
        p.creerRubrique("R06", "Décote constatée", Nature.PRODUIT);
        p.creerRubrique("R08", "Frais d'acquisition", Nature.CHARGE);
        p.creerRubrique("R16", "Coupon couru acheté", Nature.ACTIF);

        p.creerCompte("2745", "Titres souverains (exemple)");
        p.creerCompte("2746", "Titres de sociétés (exemple)");
        p.creerCompte("2764", "Coupons courus à recevoir (exemple)");
        p.creerCompte("5211", "Banque A (exemple)");
        p.creerCompte("6311", "Frais d'acquisition de titres (exemple)");
        p.creerCompte("7722", "Revenus des obligations (exemple)");

        p.creerMapping("R01", "OBLIGATION", "ETAT", "2745");
        p.creerMapping("R01", "OBLIGATION", "SOCIETE", "2746");
        p.creerMapping("R04", "*", "*", "5211");
        p.creerMapping("R05", "*", "*", "7722");
        p.creerMapping("R06", "*", "*", "7722");
        p.creerMapping("R08", "*", "*", "6311");
        p.creerMapping("R16", "*", "*", "2764");

        p.creerEvenement("ACQ_OBLIGATION", "Achat d'une obligation", "ACHAT", "PLA");
        p.creerEvenement("COUPON_OBLIGATION", "Coupon d'obligation encaissé", "ENCAISSEMENT", "REV");
        p.creerEvenement("REMB_OBLIGATION", "Remboursement d'une obligation à l'échéance", "REMBOURSEMENT", "PLA");

        // Achat : le titre entre au nominal, les frais en charge, la décote en produit, la banque paie le total.
        p.creerRecette("ACQ_OBLIGATION", null, List.of(
                new LigneSaisie("R01", Sens.D, "nominal", false),
                new LigneSaisie("R16", Sens.D, "coupon_couru", true),
                new LigneSaisie("R08", Sens.D, "frais", true),
                new LigneSaisie("R06", Sens.C, "decote", true),
                new LigneSaisie("R04", Sens.C, "total_paye", false)), AUTEUR, true);

        // Coupon : l'argent arrive en banque, il vient d'un revenu.
        p.creerRecette("COUPON_OBLIGATION", null, List.of(
                new LigneSaisie("R04", Sens.D, "coupon", false),
                new LigneSaisie("R05", Sens.C, "coupon", false)), AUTEUR, true);

        // Remboursement : l'argent arrive en banque, il vient du titre qui sort.
        p.creerRecette("REMB_OBLIGATION", null, List.of(
                new LigneSaisie("R04", Sens.D, "nominal", false),
                new LigneSaisie("R01", Sens.C, "nominal", false)), AUTEUR, true);
    }
}
