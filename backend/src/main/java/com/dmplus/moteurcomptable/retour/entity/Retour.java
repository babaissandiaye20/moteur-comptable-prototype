package com.dmplus.moteurcomptable.retour.entity;

import com.dmplus.moteurcomptable.shared.entity.EntiteAuditee;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Un point de la grille de retours : ce qu'il faut vérifier, et l'avis du testeur.
 * Remplace le fichier Excel : les retours sont saisis dans l'application et gardés en base.
 */
@Entity
@Table(name = "retour")
public class Retour extends EntiteAuditee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private int ordre;
    @Column(nullable = false)
    private String fonctionnalite;
    @Column(name = "ou_tester")
    private String ouTester;
    @Column(nullable = false, length = 500)
    private String critere;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutRetour statut = StatutRetour.NON_TESTE;
    @Column(length = 2000)
    private String observation;
    @Column(name = "ajoute_par_testeur", nullable = false)
    private boolean ajouteParTesteur;

    protected Retour() {
    }

    public Retour(int ordre, String fonctionnalite, String ouTester, String critere, boolean ajouteParTesteur) {
        this.ordre = ordre;
        this.fonctionnalite = fonctionnalite;
        this.ouTester = ouTester;
        this.critere = critere;
        this.ajouteParTesteur = ajouteParTesteur;
    }

    public void noter(StatutRetour statut, String observation) {
        if (statut != null) {
            this.statut = statut;
        }
        this.observation = observation;
    }

    /** Met à jour le libellé d'un point de départ, sans toucher à l'avis du testeur. */
    public void reformuler(String fonctionnalite, String ouTester, String critere) {
        this.fonctionnalite = fonctionnalite;
        this.ouTester = ouTester;
        this.critere = critere;
    }

    public Long getId() { return id; }
    public int getOrdre() { return ordre; }
    public String getFonctionnalite() { return fonctionnalite; }
    public String getOuTester() { return ouTester; }
    public String getCritere() { return critere; }
    public StatutRetour getStatut() { return statut; }
    public String getObservation() { return observation; }
    public boolean isAjouteParTesteur() { return ajouteParTesteur; }
}
