package com.dmplus.moteurcomptable.ecriture.entity;

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

import java.math.BigDecimal;

/** Une ligne d'écriture. Le compte est RECOPIÉ ici : si le mapping change, la ligne garde son compte. */
@Entity
@Table(name = "ecriture_ligne")
public class EcritureLigne {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "ecriture_id", nullable = false)
    private Ecriture ecriture;
    private int ordre;
    @Column(name = "rubrique_code", nullable = false)
    private String rubriqueCode;
    @Column(name = "compte_code", nullable = false)
    private String compteCode;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 1)
    private Sens sens;
    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal montant;
    @Column(nullable = false, length = 3)
    private String devise;
    @Column(name = "reference_titre")
    private String referenceTitre;

    protected EcritureLigne() {
    }

    public EcritureLigne(int ordre, String rubriqueCode, String compteCode, Sens sens,
                         BigDecimal montant, String devise, String referenceTitre) {
        this.ordre = ordre;
        this.rubriqueCode = rubriqueCode;
        this.compteCode = compteCode;
        this.sens = sens;
        this.montant = montant;
        this.devise = devise;
        this.referenceTitre = referenceTitre;
    }

    void rattacher(Ecriture e) {
        this.ecriture = e;
    }

    public int getOrdre() { return ordre; }
    public String getRubriqueCode() { return rubriqueCode; }
    public String getCompteCode() { return compteCode; }
    public Sens getSens() { return sens; }
    public BigDecimal getMontant() { return montant; }
    public String getDevise() { return devise; }
    public String getReferenceTitre() { return referenceTitre; }
}
