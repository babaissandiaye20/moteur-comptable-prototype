package com.dmplus.moteurcomptable.ecriture.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.io.Serializable;

/** Le compteur de numéros d'un journal pour un exercice. Verrouillé pendant la validation : aucun trou. */
@Entity
@Table(name = "journal_compteur")
public class JournalCompteur {

    @EmbeddedId
    private Cle cle;
    @Column(name = "dernier_numero", nullable = false)
    private long dernierNumero;

    protected JournalCompteur() {
    }

    public JournalCompteur(String journalCode, int exercice) {
        this.cle = new Cle(journalCode, exercice);
        this.dernierNumero = 0;
    }

    public long suivant() {
        return ++dernierNumero;
    }

    @Embeddable
    public record Cle(@Column(name = "journal_code") String journalCode,
                      @Column(name = "exercice") int exercice) implements Serializable {
    }
}
