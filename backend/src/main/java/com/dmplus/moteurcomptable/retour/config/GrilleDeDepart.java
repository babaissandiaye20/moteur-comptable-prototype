package com.dmplus.moteurcomptable.retour.config;

import com.dmplus.moteurcomptable.retour.entity.Retour;
import com.dmplus.moteurcomptable.retour.repository.RetourRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * La grille de retours de départ : ce que le testeur doit vérifier.
 * Chargée une seule fois, si la table est vide. Ce ne sont pas des données comptables.
 */
@Component
public class GrilleDeDepart implements ApplicationRunner {

    private static final List<String[]> POINTS = List.of(
            new String[]{"Opérations", "Paramétrer › Opérations", "Créer une opération (ex. VENTE) : elle apparaît dans la liste"},
            new String[]{"Opérations", "Paramétrer › Opérations", "Créer une opération qui existe déjà : refusé avec un message clair"},
            new String[]{"Opérations", "Paramétrer › Opérations", "Supprimer une opération utilisée : le message dit ce qui en dépend et propose de le supprimer aussi"},
            new String[]{"Journaux", "Paramétrer › Journaux", "Créer un journal avec son code dans le logiciel comptable"},
            new String[]{"Journaux", "Paramétrer › Journaux", "Supprimer un journal utilisé par un événement : refusé ou supprimé en cascade selon le choix"},
            new String[]{"Événements", "Paramétrer › Événements", "Créer un événement rattaché à une opération et à un journal"},
            new String[]{"Événements", "Paramétrer › Événements", "Un événement ne peut avoir qu'une seule opération"},
            new String[]{"Rubriques", "Paramétrer › Rubriques", "Créer une rubrique avec sa nature (actif, charge, passif, produit)"},
            new String[]{"Rubriques", "Paramétrer › Rubriques", "Supprimer une rubrique utilisée par le mapping ou une recette : le message explique pourquoi"},
            new String[]{"Comptes", "Paramétrer › Comptes", "Créer un compte du plan de comptes"},
            new String[]{"Comptes", "Paramétrer › Comptes", "Supprimer un compte libre : il disparaît ; le recréer ensuite fonctionne"},
            new String[]{"Mapping", "Paramétrer › Mapping", "Associer rubrique + classe + catégorie à un compte"},
            new String[]{"Mapping", "Paramétrer › Mapping", "Une classe ou une catégorie laissée vide vaut « toutes »"},
            new String[]{"Mapping", "Générer une écriture", "Obligation d'État → compte 2745 ; obligation de société → compte 2746"},
            new String[]{"Recettes", "Paramétrer › Recettes", "Créer et publier une recette avec au moins un débit et un crédit"},
            new String[]{"Recettes", "Paramétrer › Recettes", "Publier une recette sans crédit (ou sans débit) : refusé"},
            new String[]{"Recettes", "Paramétrer › Recettes", "Créer une nouvelle recette pour le même événement : version 2"},
            new String[]{"Recettes", "Valider les écritures", "Une écriture créée avec la version 1 garde la version 1 après la publication de la version 2"},
            new String[]{"Recettes", "Paramétrer › Recettes", "Supprimer un brouillon de recette ; retirer une recette publiée"},
            new String[]{"Génération", "Générer une écriture", "L'aperçu de la pièce se remplit pendant la saisie et signale un déséquilibre"},
            new String[]{"Génération", "Générer une écriture", "Achat d'obligation (nominal 2 000 000, frais 20 000, total 2 020 000) : 2745 D 2 000 000 · 6311 D 20 000 · 5211 C 2 020 000"},
            new String[]{"Génération", "Générer une écriture", "Sans frais : la ligne de frais n'apparaît pas"},
            new String[]{"Génération", "Générer une écriture", "Total payé faux : refusé, débit ≠ crédit, rien n'est enregistré"},
            new String[]{"Génération", "Générer une écriture", "Renvoyer le même identifiant d'événement : aucune nouvelle écriture"},
            new String[]{"Génération", "Générer une écriture", "Même identifiant avec d'autres montants : refusé, rien n'est écrasé"},
            new String[]{"Génération", "Générer une écriture", "Classe d'actifs sans mapping (ex. ACTION) : refusé, avec la ligne de mapping à ajouter"},
            new String[]{"Validation", "Valider les écritures", "Le créateur d'une écriture ne peut pas la valider (4 yeux)"},
            new String[]{"Validation", "Valider les écritures", "Un autre utilisateur valide : statut Validée et numéro attribué"},
            new String[]{"Validation", "Valider les écritures", "Numéros sans trou par journal et par année : PLA-2026-0001, PLA-2026-0002, REV-2026-0001"},
            new String[]{"Validation", "Valider les écritures", "Une écriture validée ne peut plus être validée, modifiée ni annulée"},
            new String[]{"Annulation", "Valider les écritures", "Annuler une écriture à valider : statut Annulée, elle reste visible"},
            new String[]{"Consultation", "Valider les écritures", "Chaque écriture montre ses lignes, ses comptes et ses totaux débit et crédit"},
            new String[]{"Ergonomie", "Toute l'application", "Les écrans sont compréhensibles sans explication du développeur"},
            new String[]{"Ergonomie", "Toute l'application", "Les messages d'erreur disent quoi corriger"},
            new String[]{"Ergonomie", "Toute l'application", "Le vocabulaire (opération, événement, rubrique, mapping, recette) correspond au métier"});

    private final RetourRepository retours;

    public GrilleDeDepart(RetourRepository retours) {
        this.retours = retours;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (retours.count() > 0) {
            return;
        }
        List<Retour> grille = new ArrayList<>();
        for (int i = 0; i < POINTS.size(); i++) {
            String[] p = POINTS.get(i);
            grille.add(new Retour(i + 1, p[0], p[1], p[2], false));
        }
        retours.saveAll(grille);
    }
}
