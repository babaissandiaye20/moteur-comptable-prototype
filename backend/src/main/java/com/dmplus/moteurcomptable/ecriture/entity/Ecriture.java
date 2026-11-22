package com.dmplus.moteurcomptable.ecriture.entity;

import com.dmplus.moteurcomptable.shared.exception.MoteurException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** L'en-tête d'une écriture. Il garde la recette et la version utilisées : un paramètre changé plus tard ne la touche pas. */
@Entity
@Table(name = "ecriture")
public class Ecriture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "evenement_recu_id", nullable = false)
    private Long evenementRecuId;
    @Column(name = "evenement_code", nullable = false)
    private String evenementCode;
    @Column(name = "recette_id", nullable = false)
    private Long recetteId;
    @Column(name = "recette_version", nullable = false)
    private int recetteVersion;
    @Column(name = "journal_code", nullable = false)
    private String journalCode;
    @Column(nullable = false)
    private int exercice;
    /** Attribué à la validation : PLA-2026-0001. Vide tant que l'écriture est proposée. */
    private String numero;
    @Column(name = "date_valeur", nullable = false)
    private LocalDate dateValeur;
    @Column(name = "date_reglement")
    private LocalDate dateReglement;
    private String libelle;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutEcriture statut;
    @Column(name = "created_by", nullable = false)
    private String createdBy;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "validated_by")
    private String validatedBy;
    @Column(name = "validated_at")
    private LocalDateTime validatedAt;
    @Column(name = "annulee_par")
    private String annuleePar;
    @Column(name = "annulee_le")
    private LocalDateTime annuleeLe;

    @OneToMany(mappedBy = "ecriture", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("ordre")
    private List<EcritureLigne> lignes = new ArrayList<>();

    protected Ecriture() {
    }

    public Ecriture(Long evenementRecuId, String evenementCode, Long recetteId, int recetteVersion,
                    String journalCode, LocalDate dateValeur, LocalDate dateReglement,
                    String libelle, String createdBy) {
        this.evenementRecuId = evenementRecuId;
        this.evenementCode = evenementCode;
        this.recetteId = recetteId;
        this.recetteVersion = recetteVersion;
        this.journalCode = journalCode;
        this.exercice = dateValeur.getYear();
        this.dateValeur = dateValeur;
        this.dateReglement = dateReglement;
        this.libelle = libelle;
        this.statut = StatutEcriture.PROPOSEE;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }

    public void ajouterLigne(EcritureLigne ligne) {
        ligne.rattacher(this);
        lignes.add(ligne);
    }

    /**
     * Annuler une écriture encore à valider : elle reste en base (traçabilité) avec le statut ANNULEE.
     * Une écriture validée ne s'annule pas : elle se corrige par une écriture inverse.
     */
    public void annuler(String utilisateur) {
        if (!statut.peutPasserA(StatutEcriture.ANNULEE)) {
            throw new MoteurException("STATUT_INVALIDE",
                    "Une écriture " + statut + " ne peut pas être annulée : une écriture validée se corrige par une écriture inverse");
        }
        this.statut = StatutEcriture.ANNULEE;
        this.annuleePar = utilisateur;
        this.annuleeLe = LocalDateTime.now();
    }

    /** Vérifie qu'on peut valider, AVANT de consommer un numéro. */
    public void verifierValidable(String utilisateur) {
        if (!statut.peutPasserA(StatutEcriture.VALIDEE)) {
            throw new MoteurException("STATUT_INVALIDE", "Une écriture " + statut + " ne peut pas être validée");
        }
        if (utilisateur == null || utilisateur.isBlank()) {
            throw new MoteurException("UTILISATEUR_REQUIS", "Indiquez qui valide");
        }
        if (utilisateur.equals(createdBy)) {
            throw new MoteurException("QUATRE_YEUX", "Celui qui a créé l'écriture ne peut pas la valider");
        }
    }

    /** Séparation des tâches : celui qui a créé l'écriture ne peut pas la valider. */
    public void valider(String utilisateur, String numeroAttribue) {
        if (!statut.peutPasserA(StatutEcriture.VALIDEE)) {
            throw new MoteurException("STATUT_INVALIDE", "Une écriture " + statut + " ne peut pas être validée");
        }
        if (utilisateur == null || utilisateur.isBlank()) {
            throw new MoteurException("UTILISATEUR_REQUIS", "Indiquez qui valide");
        }
        if (utilisateur.equals(createdBy)) {
            throw new MoteurException("QUATRE_YEUX", "Celui qui a créé l'écriture ne peut pas la valider");
        }
        this.statut = StatutEcriture.VALIDEE;
        this.numero = numeroAttribue;
        this.validatedBy = utilisateur;
        this.validatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getEvenementRecuId() { return evenementRecuId; }
    public String getEvenementCode() { return evenementCode; }
    public Long getRecetteId() { return recetteId; }
    public int getRecetteVersion() { return recetteVersion; }
    public String getJournalCode() { return journalCode; }
    public int getExercice() { return exercice; }
    public String getNumero() { return numero; }
    public LocalDate getDateValeur() { return dateValeur; }
    public LocalDate getDateReglement() { return dateReglement; }
    public String getLibelle() { return libelle; }
    public StatutEcriture getStatut() { return statut; }
    public String getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getValidatedBy() { return validatedBy; }
    public String getAnnuleePar() { return annuleePar; }
    public List<EcritureLigne> getLignes() { return lignes; }
}
