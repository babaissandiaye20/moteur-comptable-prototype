package com.dmplus.moteurcomptable.parametrage.entity;

import com.dmplus.moteurcomptable.shared.entity.EntiteSupprimable;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Ce qui se passe précisément (ACQ_OBLIGATION…). Appartient à UNE seule opération. */
@Entity
@Table(name = "evenement")
@SQLRestriction("is_deleted = false")
public class Evenement extends EntiteSupprimable {

    @Id
    private String code;
    private String libelle;
    @Column(name = "operation_code", nullable = false)
    private String operationCode;
    @Column(name = "journal_code", nullable = false)
    private String journalCode;

    protected Evenement() {
    }

    public Evenement(String code, String libelle, String operationCode, String journalCode) {
        this.code = code;
        this.libelle = libelle;
        this.operationCode = operationCode;
        this.journalCode = journalCode;
    }

    public String getCode() { return code; }
    public String getLibelle() { return libelle; }
    public String getOperationCode() { return operationCode; }
    public String getJournalCode() { return journalCode; }
}
