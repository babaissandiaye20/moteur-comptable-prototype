package com.dmplus.moteurcomptable.parametrage.service;

import com.dmplus.moteurcomptable.shared.exception.MoteurException;
import com.dmplus.moteurcomptable.parametrage.entity.Nature;
import com.dmplus.moteurcomptable.shared.entity.Sens;
import com.dmplus.moteurcomptable.parametrage.entity.Compte;
import com.dmplus.moteurcomptable.parametrage.repository.CompteRepository;
import com.dmplus.moteurcomptable.parametrage.entity.Evenement;
import com.dmplus.moteurcomptable.parametrage.repository.EvenementRepository;
import com.dmplus.moteurcomptable.parametrage.entity.Journal;
import com.dmplus.moteurcomptable.parametrage.repository.JournalRepository;
import com.dmplus.moteurcomptable.parametrage.entity.Mapping;
import com.dmplus.moteurcomptable.parametrage.repository.MappingRepository;
import com.dmplus.moteurcomptable.parametrage.entity.Operation;
import com.dmplus.moteurcomptable.parametrage.repository.OperationRepository;
import com.dmplus.moteurcomptable.parametrage.entity.Recette;
import com.dmplus.moteurcomptable.parametrage.entity.RecetteLigne;
import com.dmplus.moteurcomptable.parametrage.repository.RecetteRepository;
import com.dmplus.moteurcomptable.parametrage.entity.Rubrique;
import com.dmplus.moteurcomptable.parametrage.entity.StatutRecette;
import com.dmplus.moteurcomptable.parametrage.repository.RubriqueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Le paramétrage et ses contraintes. Le comptable remplit ; le code empêche les incohérences :
 * événement sans opération, journal inconnu, mapping vers un compte fermé, recette publiée modifiée…
 */
@Service
@Transactional
public class ParametrageService {

    private final OperationRepository operations;
    private final EvenementRepository evenements;
    private final JournalRepository journaux;
    private final RubriqueRepository rubriques;
    private final CompteRepository comptes;
    private final MappingRepository mappings;
    private final RecetteRepository recettes;

    public ParametrageService(OperationRepository operations, EvenementRepository evenements,
                              JournalRepository journaux, RubriqueRepository rubriques,
                              CompteRepository comptes, MappingRepository mappings,
                              RecetteRepository recettes) {
        this.operations = operations;
        this.evenements = evenements;
        this.journaux = journaux;
        this.rubriques = rubriques;
        this.comptes = comptes;
        this.mappings = mappings;
        this.recettes = recettes;
    }

    public Operation creerOperation(String code, String libelle) {
        String c = code(code);
        if (operations.existsById(c)) {
            throw dejaExistant("L'opération", c);
        }
        return operations.save(new Operation(c, libelle));
    }

    public Journal creerJournal(String code, String libelle, String codeExterne) {
        String c = code(code);
        if (journaux.existsById(c)) {
            throw dejaExistant("Le journal", c);
        }
        return journaux.save(new Journal(c, libelle, codeExterne));
    }

    /** Un événement appartient à UNE seule opération et va dans UN journal, tous deux existants. */
    public Evenement creerEvenement(String code, String libelle, String operationCode, String journalCode) {
        String c = code(code);
        if (evenements.existsById(c)) {
            throw dejaExistant("L'événement", c);
        }
        if (operationCode == null || !operations.existsById(operationCode)) {
            throw new MoteurException("OPERATION_INCONNUE", "L'opération « " + operationCode + " » n'existe pas");
        }
        Journal journal = journaux.findById(journalCode == null ? "" : journalCode)
                .orElseThrow(() -> new MoteurException("JOURNAL_INCONNU", "Le journal « " + journalCode + " » n'existe pas"));
        if (!journal.isActif()) {
            throw new MoteurException("JOURNAL_INACTIF", "Le journal " + journalCode + " est fermé");
        }
        return evenements.save(new Evenement(c, libelle, operationCode, journalCode));
    }

    public Rubrique creerRubrique(String code, String libelle, Nature nature) {
        String c = code(code);
        if (nature == null) {
            throw new MoteurException("NATURE_REQUISE", "Indiquez la nature : ACTIF, CHARGE, PASSIF ou PRODUIT");
        }
        if (rubriques.existsById(c)) {
            throw dejaExistant("La rubrique", c);
        }
        return rubriques.save(new Rubrique(c, libelle, nature));
    }

    public Compte creerCompte(String code, String libelle) {
        String c = code(code);
        if (comptes.existsById(c)) {
            throw dejaExistant("Le compte", c);
        }
        return comptes.save(new Compte(c, libelle, true));
    }

    /** Le mapping ne pointe que vers une rubrique existante et un compte ouvert ; une ligne par combinaison. */
    public Mapping creerMapping(String rubriqueCode, String classeActif, String categorie, String compteCode) {
        String classe = valeurOuTous(classeActif);
        String cat = valeurOuTous(categorie);
        if (rubriqueCode == null || !rubriques.existsById(rubriqueCode)) {
            throw new MoteurException("RUBRIQUE_INCONNUE", "La rubrique « " + rubriqueCode + " » n'existe pas");
        }
        Compte compte = comptes.findById(compteCode == null ? "" : compteCode)
                .orElseThrow(() -> new MoteurException("COMPTE_INCONNU", "Le compte « " + compteCode + " » n'existe pas"));
        if (!compte.isActif()) {
            throw new MoteurException("COMPTE_INACTIF", "Le compte " + compteCode + " est fermé");
        }
        if (mappings.existsByRubriqueCodeAndClasseActifAndCategorie(rubriqueCode, classe, cat)) {
            throw new MoteurException("DEJA_EXISTANT",
                    "Un mapping existe déjà pour " + rubriqueCode + " / " + classe + " / " + cat);
        }
        return mappings.save(new Mapping(rubriqueCode, classe, cat, compteCode));
    }

    /** Crée la version suivante d'une recette (en brouillon), et la publie si demandé. */
    public Recette creerRecette(String evenementCode, LocalDate dateEffet, List<LigneSaisie> lignes,
                                String utilisateur, boolean publier) {
        if (evenementCode == null || !evenements.existsById(evenementCode)) {
            throw new MoteurException("EVENEMENT_INCONNU", "L'événement « " + evenementCode + " » n'existe pas");
        }
        if (lignes == null || lignes.size() < 2) {
            throw new MoteurException("RECETTE_INCOMPLETE", "Une recette a besoin d'au moins deux lignes");
        }
        int version = recettes.findFirstByEvenementCodeOrderByVersionDesc(evenementCode)
                .map(r -> r.getVersion() + 1).orElse(1);
        Recette recette = new Recette(evenementCode, version,
                dateEffet == null ? LocalDate.of(2000, 1, 1) : dateEffet);
        int ordre = 1;
        for (LigneSaisie l : lignes) {
            if (l.rubrique() == null || !rubriques.existsById(l.rubrique())) {
                throw new MoteurException("RUBRIQUE_INCONNUE", "Ligne " + ordre + " : la rubrique « " + l.rubrique() + " » n'existe pas");
            }
            if (l.sens() == null) {
                throw new MoteurException("SENS_REQUIS", "Ligne " + ordre + " : indiquez le sens, D ou C");
            }
            if (l.montantSource() == null || l.montantSource().isBlank()) {
                throw new MoteurException("CHAMP_REQUIS", "Ligne " + ordre + " : indiquez le montant à prendre dans l'événement");
            }
            recette.ajouterLigne(new RecetteLigne(ordre++, l.rubrique(), l.sens(), l.montantSource().trim(), l.facultative()));
        }
        if (publier) {
            recette.publier(utilisateur);
        }
        return recettes.save(recette);
    }

    public Recette publierRecette(Long id, String utilisateur) {
        Recette recette = recettes.findById(id)
                .orElseThrow(() -> new MoteurException("RECETTE_INCONNUE", "Recette " + id + " introuvable"));
        recette.publier(utilisateur);
        return recette;
    }

    // ---------- Suppression douce : refusée tant que l'élément est utilisé ----------

    public void supprimerOperation(String code) {
        Operation o = operations.findById(code).orElseThrow(() -> introuvable("L'opération", code));
        if (evenements.existsByOperationCode(code)) {
            throw new MoteurException("ELEMENT_UTILISE", "L'opération " + code + " est utilisée par un événement");
        }
        o.supprimer();
    }

    public void supprimerJournal(String code) {
        Journal j = journaux.findById(code).orElseThrow(() -> introuvable("Le journal", code));
        if (evenements.existsByJournalCode(code)) {
            throw new MoteurException("ELEMENT_UTILISE", "Le journal " + code + " est utilisé par un événement");
        }
        j.supprimer();
    }

    public void supprimerEvenement(String code) {
        Evenement e = evenements.findById(code).orElseThrow(() -> introuvable("L'événement", code));
        if (recettes.existsByEvenementCodeAndStatut(code, StatutRecette.PUBLIEE)) {
            throw new MoteurException("ELEMENT_UTILISE", "L'événement " + code + " a une recette publiée : retirez-la d'abord");
        }
        e.supprimer();
    }

    public void supprimerRubrique(String code) {
        Rubrique r = rubriques.findById(code).orElseThrow(() -> introuvable("La rubrique", code));
        if (mappings.existsByRubriqueCode(code) || recettes.rubriqueUtiliseeDansUneRecette(code, StatutRecette.RETIREE)) {
            throw new MoteurException("ELEMENT_UTILISE", "La rubrique " + code + " est utilisée par un mapping ou une recette");
        }
        r.supprimer();
    }

    public void supprimerCompte(String code) {
        Compte c = comptes.findById(code).orElseThrow(() -> introuvable("Le compte", code));
        if (mappings.existsByCompteCode(code)) {
            throw new MoteurException("ELEMENT_UTILISE", "Le compte " + code + " est utilisé par le mapping");
        }
        c.supprimer();
    }

    /** Un mapping supprimé ne touche pas les écritures passées : leur compte a été recopié. */
    public void supprimerMapping(Long id) {
        mappings.findById(id).orElseThrow(() -> introuvable("Le mapping", String.valueOf(id))).supprimer();
    }

    public Recette retirerRecette(Long id) {
        Recette r = recettes.findById(id).orElseThrow(() -> introuvable("La recette", String.valueOf(id)));
        r.retirer();
        return r;
    }

    private static MoteurException introuvable(String quoi, String code) {
        return new MoteurException("ELEMENT_INCONNU", quoi + " " + code + " n'existe pas");
    }

    @Transactional(readOnly = true)
    public Vue tout() {
        return new Vue(operations.findAll(), journaux.findAll(), evenements.findAll(), rubriques.findAll(),
                comptes.findAll(), mappings.findAll(), recettes.findAllByOrderByEvenementCodeAscVersionAsc());
    }

    public record LigneSaisie(String rubrique, Sens sens, String montantSource, Boolean ignorerSiZero) {
        public boolean facultative() {
            return Boolean.TRUE.equals(ignorerSiZero);
        }
    }

    public record Vue(List<Operation> operations, List<Journal> journaux, List<Evenement> evenements,
                      List<Rubrique> rubriques, List<Compte> comptes, List<Mapping> mappings,
                      List<Recette> recettes) {
    }

    private static String code(String v) {
        if (v == null || v.isBlank()) {
            throw new MoteurException("CHAMP_REQUIS", "Le code est obligatoire");
        }
        return v.trim().toUpperCase();
    }

    private static String valeurOuTous(String v) {
        return v == null || v.isBlank() ? Mapping.TOUS : v.trim().toUpperCase();
    }

    private static MoteurException dejaExistant(String quoi, String code) {
        return new MoteurException("DEJA_EXISTANT", quoi + " " + code + " existe déjà");
    }
}
