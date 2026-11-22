package com.dmplus.moteurcomptable.parametrage.entity;

import com.dmplus.moteurcomptable.shared.entity.EntiteSupprimable;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Le nom d'une ligne d'écriture (« Valeur de l'obligation », « Frais »…), avec sa nature. */
@Entity
@Table(name = "rubrique")
@SQLRestriction("is_deleted = false")
public class Rubrique extends EntiteSupprimable {

    @Id
    private String code;
    private String libelle;
    @Enumerated(EnumType.STRING)
    private Nature nature;

    protected Rubrique() {
    }

    public Rubrique(String code, String libelle, Nature nature) {
        this.code = code;
        this.libelle = libelle;
        this.nature = nature;
    }

    public String getCode() { return code; }
    public String getLibelle() { return libelle; }
    public Nature getNature() { return nature; }
}
