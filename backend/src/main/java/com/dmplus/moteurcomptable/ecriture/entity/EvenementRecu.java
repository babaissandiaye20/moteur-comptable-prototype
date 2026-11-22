package com.dmplus.moteurcomptable.ecriture.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

/**
 * Ce que l'application source a envoyé, gardé tel quel.
 * La contrainte (source, event_id) unique empêche de traiter deux fois le même événement.
 */
@Entity
@Table(name = "evenement_recu", uniqueConstraints = @UniqueConstraint(columnNames = {"source", "event_id"}))
public class EvenementRecu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String source;
    @Column(name = "event_id", nullable = false)
    private String eventId;
    @Column(name = "evenement_code", nullable = false)
    private String evenementCode;
    @Lob
    @Column(nullable = false)
    private String payload;
    @Column(nullable = false, length = 64)
    private String hash;
    @Column(name = "recu_le", nullable = false)
    private LocalDateTime recuLe;

    protected EvenementRecu() {
    }

    public EvenementRecu(String source, String eventId, String evenementCode, String payload, String hash) {
        this.source = source;
        this.eventId = eventId;
        this.evenementCode = evenementCode;
        this.payload = payload;
        this.hash = hash;
        this.recuLe = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getSource() { return source; }
    public String getEventId() { return eventId; }
    public String getHash() { return hash; }
}
