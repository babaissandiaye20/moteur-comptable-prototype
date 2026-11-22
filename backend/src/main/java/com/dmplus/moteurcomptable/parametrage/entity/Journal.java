package com.dmplus.moteurcomptable.parametrage.entity;

import com.dmplus.moteurcomptable.shared.entity.EntiteSupprimable;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Un classeur d'écritures de même nature (PLA, REV, OD). Paramétré, puis choisi dans une liste. */
@Entity
@Table(name = "journal")
@SQLRestriction("is_deleted = false")
public class Journal extends EntiteSupprimable {

    @Id
    private String code;
    private String libelle;
    @Column(name = "code_externe")
    private String codeExterne;
    private boolean actif;

    protected Journal() {
    }

    public Journal(String code, String libelle, String codeExterne) {
        this.code = code;
        this.libelle = libelle;
        this.codeExterne = codeExterne;
        this.actif = true;
    }

    public String getCode() { return code; }
    public String getLibelle() { return libelle; }
    public String getCodeExterne() { return codeExterne; }
    public boolean isActif() { return actif; }
}
