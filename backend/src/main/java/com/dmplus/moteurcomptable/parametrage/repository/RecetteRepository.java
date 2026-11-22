package com.dmplus.moteurcomptable.parametrage.repository;

import com.dmplus.moteurcomptable.parametrage.entity.Recette;

import com.dmplus.moteurcomptable.parametrage.entity.StatutRecette;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RecetteRepository extends JpaRepository<Recette, Long> {

    /** La version publiée la plus récente, déjà en vigueur à la date de l'événement. */
    Optional<Recette> findFirstByEvenementCodeAndStatutAndDateEffetLessThanEqualOrderByVersionDesc(
            String evenementCode, StatutRecette statut, LocalDate date);

    Optional<Recette> findFirstByEvenementCodeOrderByVersionDesc(String evenementCode);

    List<Recette> findAllByOrderByEvenementCodeAscVersionAsc();

    boolean existsByEvenementCodeAndStatut(String evenementCode, StatutRecette statut);

    @Query("select count(l) > 0 from RecetteLigne l where l.rubriqueCode = :rubrique and l.recette.statut <> :retiree")
    boolean rubriqueUtiliseeDansUneRecette(String rubrique, StatutRecette retiree);
}
