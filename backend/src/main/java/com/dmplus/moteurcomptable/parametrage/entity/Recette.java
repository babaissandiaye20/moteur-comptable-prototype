package com.dmplus.moteurcomptable.parametrage.entity;

import com.dmplus.moteurcomptable.shared.entity.EntiteAuditee;
import com.dmplus.moteurcomptable.shared.entity.Sens;

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
import jakarta.persistence.UniqueConstraint;

import com.dmplus.moteurcomptable.shared.exception.MoteurException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * La « définition » d'un événement, versionnée : la liste des lignes à écrire.
 * Une version publiée ne se modifie plus ; chaque écriture garde la version qu'elle a utilisée.
 */
@Entity
@Table(name = "recette", uniqueConstraints = @UniqueConstraint(columnNames = {"evenement_code", "version"}))
public class Recette extends EntiteAuditee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "evenement_code", nullable = false)
    private String evenementCode;
    @Column(nullable = false)
    private int version;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutRecette statut;
    @Column(name = "date_effet", nullable = false)
    private LocalDate dateEffet;
    /** Qui a publié la version (règle métier), en plus de la traçabilité created_by / updated_by. */
    @Column(name = "publie_par")
    private String publiePar;
    @Column(name = "publie_le")
    private LocalDateTime publieLe;

    @OneToMany(mappedBy = "recette", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("ordre")
    private List<RecetteLigne> lignes = new ArrayList<>();

    protected Recette() {
    }

    public Recette(String evenementCode, int version, LocalDate dateEffet) {
        this.evenementCode = evenementCode;
        this.version = version;
        this.dateEffet = dateEffet;
        this.statut = StatutRecette.BROUILLON;
    }

    public void ajouterLigne(RecetteLigne ligne) {
        exigerBrouillon();
        ligne.rattacher(this);
        lignes.add(ligne);
    }

    /** Publier fige la recette : elle ne pourra plus être modifiée, seulement remplacée par une nouvelle version. */
    public void publier(String utilisateur) {
        exigerBrouillon();
        boolean debit = lignes.stream().anyMatch(l -> l.getSens() == Sens.D);
        boolean credit = lignes.stream().anyMatch(l -> l.getSens() == Sens.C);
        if (!debit || !credit) {
            throw new MoteurException("RECETTE_INCOMPLETE",
                    "Une règle d'événement doit avoir au moins une ligne au débit et une au crédit");
        }
        this.statut = StatutRecette.PUBLIEE;
        this.publiePar = utilisateur;
        this.publieLe = LocalDateTime.now();
    }

    /** Retirer une recette : elle ne sert plus pour les nouveaux événements, mais reste liée aux écritures passées. */
    public void retirer() {
        this.statut = StatutRecette.RETIREE;
    }

    private void exigerBrouillon() {
        if (statut != StatutRecette.BROUILLON) {
            throw new MoteurException("RECETTE_FIGEE",
                    "La règle " + evenementCode + " v" + version + " est " + statut + " : créez une nouvelle version");
        }
    }

    public Long getId() { return id; }
    public String getEvenementCode() { return evenementCode; }
    public int getVersion() { return version; }
    public StatutRecette getStatut() { return statut; }
    public LocalDate getDateEffet() { return dateEffet; }
    public List<RecetteLigne> getLignes() { return lignes; }
    public String getPubliePar() { return publiePar; }
    public LocalDateTime getPublieLe() { return publieLe; }
}
