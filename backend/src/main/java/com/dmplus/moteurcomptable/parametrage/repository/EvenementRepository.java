package com.dmplus.moteurcomptable.parametrage.repository;

import com.dmplus.moteurcomptable.parametrage.entity.Evenement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;

public interface EvenementRepository extends JpaRepository<Evenement, String> {

    /** Ignore la suppression douce : vrai si le code existe, même supprimé. */
    @Query(value = "select count(*) > 0 from evenement where code = :code and is_deleted = true", nativeQuery = true)
    boolean existeSupprime(@Param("code") String code);

    /** Supprimer puis recréer le même code : on restaure la ligne au lieu d'en créer une seconde. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "update evenement set libelle = :libelle, operation_code = :operationCode, journal_code = :journalCode, is_deleted = false where code = :code", nativeQuery = true)
    int restaurer(@Param("code") String code, @Param("libelle") String libelle, @Param("operationCode") String operationCode, @Param("journalCode") String journalCode);
    boolean existsByOperationCode(String operationCode);
    boolean existsByJournalCode(String journalCode);
    java.util.List<Evenement> findByOperationCode(String operationCode);
    java.util.List<Evenement> findByJournalCode(String journalCode);
}
