package com.dmplus.moteurcomptable.parametrage.repository;

import com.dmplus.moteurcomptable.parametrage.entity.Compte;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;

public interface CompteRepository extends JpaRepository<Compte, String> {

    /** Ignore la suppression douce : vrai si le code existe, même supprimé. */
    @Query(value = "select count(*) > 0 from compte where code = :code and is_deleted = true", nativeQuery = true)
    boolean existeSupprime(@Param("code") String code);

    /** Supprimer puis recréer le même code : on restaure la ligne au lieu d'en créer une seconde. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "update compte set libelle = :libelle, actif = true, is_deleted = false where code = :code", nativeQuery = true)
    int restaurer(@Param("code") String code, @Param("libelle") String libelle);
}
