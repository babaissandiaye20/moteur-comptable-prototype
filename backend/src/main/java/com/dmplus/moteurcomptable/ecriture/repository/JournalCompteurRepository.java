package com.dmplus.moteurcomptable.ecriture.repository;

import com.dmplus.moteurcomptable.ecriture.entity.JournalCompteur;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface JournalCompteurRepository extends JpaRepository<JournalCompteur, JournalCompteur.Cle> {

    /** SELECT … FOR UPDATE : la ligne reste verrouillée jusqu'à la fin de la transaction. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from JournalCompteur c where c.cle = :cle")
    Optional<JournalCompteur> verrouiller(JournalCompteur.Cle cle);
}
