package com.dmplus.moteurcomptable.shared.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

/**
 * Suppression douce : la ligne reste en base (l'historique et les écritures passées la référencent),
 * mais elle est marquée is_deleted et disparaît des recherches.
 * Réservé au PARAMÉTRAGE. Une écriture comptable ne se supprime jamais : on la contre-passe.
 */
@MappedSuperclass
public abstract class EntiteSupprimable extends EntiteAuditee {

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    public void supprimer() {
        this.deleted = true;
    }

    public boolean isDeleted() { return deleted; }
}
