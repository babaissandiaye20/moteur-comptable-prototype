package com.dmplus.moteurcomptable.parametrage.entity;

import com.dmplus.moteurcomptable.shared.entity.Sens;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Une ligne de recette : rubrique + sens + nom du montant à prendre dans l'événement. Pas de compte ici. */
@Entity
@Table(name = "recette_ligne")
public class RecetteLigne {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "recette_id", nullable = false)
    private Recette recette;
    private int ordre;
    @Column(name = "rubrique_code", nullable = false)
    private String rubriqueCode;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 1)
    private Sens sens;
    @Column(name = "montant_source", nullable = false)
    private String montantSource;
    @Column(name = "ignorer_si_zero")
    private boolean ignorerSiZero;

    protected RecetteLigne() {
    }

    public RecetteLigne(int ordre, String rubriqueCode, Sens sens, String montantSource, boolean ignorerSiZero) {
        this.ordre = ordre;
        this.rubriqueCode = rubriqueCode;
        this.sens = sens;
        this.montantSource = montantSource;
        this.ignorerSiZero = ignorerSiZero;
    }

    void rattacher(Recette r) {
        this.recette = r;
    }

    public int getOrdre() { return ordre; }
    public String getRubriqueCode() { return rubriqueCode; }
    public Sens getSens() { return sens; }
    public String getMontantSource() { return montantSource; }
    public boolean isIgnorerSiZero() { return ignorerSiZero; }
}
