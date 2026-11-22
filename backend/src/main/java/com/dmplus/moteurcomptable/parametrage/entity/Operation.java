package com.dmplus.moteurcomptable.parametrage.entity;

import com.dmplus.moteurcomptable.shared.entity.EntiteSupprimable;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** La famille d'action : ACHAT, ENCAISSEMENT… Une opération regroupe plusieurs événements. */
@Entity
@Table(name = "operation")
@SQLRestriction("is_deleted = false")
public class Operation extends EntiteSupprimable {

    @Id
    private String code;
    private String libelle;

    protected Operation() {
    }

    public Operation(String code, String libelle) {
        this.code = code;
        this.libelle = libelle;
    }

    public String getCode() { return code; }
    public String getLibelle() { return libelle; }
}
