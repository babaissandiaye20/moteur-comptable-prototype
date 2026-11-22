package com.dmplus.moteurcomptable.parametrage.entity;

import com.dmplus.moteurcomptable.shared.entity.EntiteSupprimable;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Un numéro du plan de comptes de la compagnie : une « page » du grand livre. */
@Entity
@Table(name = "compte")
@SQLRestriction("is_deleted = false")
public class Compte extends EntiteSupprimable {

    @Id
    private String code;
    private String libelle;
    private boolean actif;

    protected Compte() {
    }

    public Compte(String code, String libelle, boolean actif) {
        this.code = code;
        this.libelle = libelle;
        this.actif = actif;
    }

    public String getCode() { return code; }
    public String getLibelle() { return libelle; }
    public boolean isActif() { return actif; }
}
