package com.dmplus.moteurcomptable.ecriture.service;

import com.dmplus.moteurcomptable.ecriture.dto.EcritureDto;
import com.dmplus.moteurcomptable.ecriture.dto.EvenementEntrantDto;
import com.dmplus.moteurcomptable.ecriture.dto.ResultatTraitementDto;
import com.dmplus.moteurcomptable.parametrage.entity.Compte;
import com.dmplus.moteurcomptable.ecriture.entity.Ecriture;
import com.dmplus.moteurcomptable.ecriture.entity.EcritureLigne;
import com.dmplus.moteurcomptable.parametrage.entity.Evenement;
import com.dmplus.moteurcomptable.ecriture.entity.EvenementRecu;
import com.dmplus.moteurcomptable.ecriture.entity.JournalCompteur;
import com.dmplus.moteurcomptable.parametrage.entity.Mapping;
import com.dmplus.moteurcomptable.parametrage.entity.Recette;
import com.dmplus.moteurcomptable.parametrage.entity.StatutRecette;
import com.dmplus.moteurcomptable.shared.exception.MoteurException;
import com.dmplus.moteurcomptable.parametrage.repository.CompteRepository;
import com.dmplus.moteurcomptable.ecriture.repository.EcritureRepository;
import com.dmplus.moteurcomptable.ecriture.repository.EvenementRecuRepository;
import com.dmplus.moteurcomptable.parametrage.repository.EvenementRepository;
import com.dmplus.moteurcomptable.ecriture.repository.JournalCompteurRepository;
import com.dmplus.moteurcomptable.parametrage.repository.MappingRepository;
import com.dmplus.moteurcomptable.parametrage.repository.RecetteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * La génération des écritures : un événement reçu + la recette publiée + le mapping = une écriture équilibrée.
 * Tout se passe dans une transaction : l'écriture est enregistrée entière, ou pas du tout.
 */
@Service
@Transactional
public class EcritureService {

    private final EvenementRecuRepository recus;
    private final EvenementRepository evenements;
    private final RecetteRepository recettes;
    private final MappingRepository mappings;
    private final CompteRepository comptes;
    private final EcritureRepository ecritures;
    private final JournalCompteurRepository compteurs;

    public EcritureService(EvenementRecuRepository recus, EvenementRepository evenements,
                           RecetteRepository recettes, MappingRepository mappings, CompteRepository comptes,
                           EcritureRepository ecritures, JournalCompteurRepository compteurs) {
        this.recus = recus;
        this.evenements = evenements;
        this.recettes = recettes;
        this.mappings = mappings;
        this.comptes = comptes;
        this.ecritures = ecritures;
        this.compteurs = compteurs;
    }

    public ResultatTraitementDto traiter(EvenementEntrantDto e) {
        verifierChamps(e);
        String classe = normaliser(e.classeActif());
        String categorie = normaliser(e.categorie());
        String contenu = contenuCanonique(e, classe, categorie);
        String hash = sha256(contenu);

        // 1. Idempotence : le même événement reçu deux fois ne crée qu'une écriture.
        var dejaRecu = recus.findBySourceAndEventId(e.source(), e.eventId());
        if (dejaRecu.isPresent()) {
            if (!dejaRecu.get().getHash().equals(hash)) {
                throw new MoteurException("COLLISION_IDENTIFIANT",
                        "L'événement " + e.eventId() + " a déjà été reçu avec un contenu différent : rien n'est écrasé");
            }
            Ecriture existante = ecritures.findByEvenementRecuId(dejaRecu.get().getId()).orElseThrow();
            return new ResultatTraitementDto("DOUBLON",
                    "Événement déjà reçu : aucune nouvelle écriture", EcritureDto.de(existante));
        }

        // 2. L'événement doit être paramétré, avec une recette publiée en vigueur à sa date.
        Evenement evenement = evenements.findById(e.evenement())
                .orElseThrow(() -> new MoteurException("EVENEMENT_INCONNU",
                        "L'événement « " + e.evenement() + " » n'est pas paramétré"));
        Recette recette = recettes
                .findFirstByEvenementCodeAndStatutAndDateEffetLessThanEqualOrderByVersionDesc(
                        evenement.getCode(), StatutRecette.PUBLIEE, e.dateValeur())
                .orElseThrow(() -> new MoteurException("RECETTE_INTROUVABLE",
                        "Aucune recette publiée pour " + evenement.getCode() + " au " + e.dateValeur()));

        // 3. Le moteur pur : recette + montants + mapping → lignes équilibrées.
        List<LigneRecette> lignesRecette = recette.getLignes().stream()
                .map(l -> new LigneRecette(l.getOrdre(), l.getRubriqueCode(), l.getSens(),
                        l.getMontantSource(), l.isIgnorerSiZero()))
                .toList();
        List<LigneProposee> lignes = GenerateurEcriture.generer(lignesRecette, e.montants(),
                rubrique -> resoudreCompte(rubrique, classe, categorie));

        // 4. On enregistre l'événement reçu, puis l'écriture avec ses lignes (compte recopié).
        EvenementRecu recu = recus.save(new EvenementRecu(e.source(), e.eventId(), evenement.getCode(), contenu, hash));
        Ecriture ecriture = new Ecriture(recu.getId(), evenement.getCode(), recette.getId(), recette.getVersion(),
                evenement.getJournalCode(), e.dateValeur(), e.dateReglement(),
                e.libelle() == null || e.libelle().isBlank() ? evenement.getLibelle() : e.libelle(),
                e.utilisateur() == null || e.utilisateur().isBlank() ? e.source() : e.utilisateur());
        String devise = e.devise() == null || e.devise().isBlank() ? "XOF" : e.devise();
        lignes.forEach(l -> ecriture.ajouterLigne(new EcritureLigne(l.ordre(), l.rubrique(), l.compte(),
                l.sens(), l.montant(), devise, e.referenceTitre())));
        return new ResultatTraitementDto("TRAITE", "Écriture générée", EcritureDto.de(ecritures.save(ecriture)));
    }

    /** Validation par un deuxième utilisateur. Le numéro est attribué ici, sans trou, par journal et exercice. */
    public EcritureDto valider(Long id, String utilisateur) {
        Ecriture ecriture = trouver(id);
        ecriture.verifierValidable(utilisateur);
        var cle = new JournalCompteur.Cle(ecriture.getJournalCode(), ecriture.getExercice());
        JournalCompteur compteur = compteurs.verrouiller(cle)
                .orElseGet(() -> compteurs.save(new JournalCompteur(cle.journalCode(), cle.exercice())));
        String numero = "%s-%d-%04d".formatted(cle.journalCode(), cle.exercice(), compteur.suivant());
        ecriture.valider(utilisateur, numero);
        return EcritureDto.de(ecriture);
    }

    @Transactional(readOnly = true)
    public List<EcritureDto> lister() {
        return ecritures.findAllByOrderByIdDesc().stream().map(EcritureDto::de).toList();
    }

    @Transactional(readOnly = true)
    public EcritureDto detail(Long id) {
        return EcritureDto.de(trouver(id));
    }

    /** Le « répertoire » : la ligne de mapping la plus précise pour rubrique + classe + catégorie. */
    String resoudreCompte(String rubrique, String classe, String categorie) {
        Mapping meilleur = mappings.findByRubriqueCode(rubrique).stream()
                .filter(m -> m.score(classe, categorie) >= 0)
                .max(Comparator.comparingInt(m -> m.score(classe, categorie)))
                .orElseThrow(() -> new MoteurException("MAPPING_MANQUANT",
                        "Aucun compte pour la rubrique " + rubrique + " / " + classe + " / " + categorie
                                + " : ajoutez cette ligne au mapping"));
        Compte compte = comptes.findById(meilleur.getCompteCode())
                .orElseThrow(() -> new MoteurException("COMPTE_INCONNU", "Le compte " + meilleur.getCompteCode() + " n'existe pas"));
        if (!compte.isActif()) {
            throw new MoteurException("COMPTE_INACTIF", "Le compte " + compte.getCode() + " est fermé");
        }
        return compte.getCode();
    }

    private Ecriture trouver(Long id) {
        return ecritures.findById(id)
                .orElseThrow(() -> new MoteurException("ECRITURE_INCONNUE", "Écriture " + id + " introuvable"));
    }

    private static void verifierChamps(EvenementEntrantDto e) {
        if (e == null || blanc(e.source()) || blanc(e.eventId()) || blanc(e.evenement())) {
            throw new MoteurException("CHAMP_REQUIS", "source, eventId et evenement sont obligatoires");
        }
        if (e.dateValeur() == null) {
            throw new MoteurException("CHAMP_REQUIS", "La date de valeur est obligatoire");
        }
        if (e.montants() == null || e.montants().isEmpty()) {
            throw new MoteurException("CHAMP_REQUIS", "L'événement ne contient aucun montant");
        }
    }

    /** Texte stable de l'événement, pour comparer deux réceptions (même contenu = même empreinte). */
    private static String contenuCanonique(EvenementEntrantDto e, String classe, String categorie) {
        Map<String, String> m = new TreeMap<>();
        e.montants().forEach((k, v) -> m.put(k, v == null ? "null" : v.stripTrailingZeros().toPlainString()));
        return "source=" + e.source() + "|eventId=" + e.eventId() + "|evenement=" + e.evenement()
                + "|dateValeur=" + e.dateValeur() + "|dateReglement=" + e.dateReglement()
                + "|classe=" + classe + "|categorie=" + categorie + "|titre=" + e.referenceTitre()
                + "|devise=" + e.devise() + "|montants=" + m;
    }

    private static String sha256(String texte) {
        try {
            byte[] h = MessageDigest.getInstance("SHA-256").digest(texte.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(h);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String normaliser(String v) {
        return blanc(v) ? Mapping.TOUS : v.trim().toUpperCase();
    }

    private static boolean blanc(String v) {
        return v == null || v.isBlank();
    }

}
