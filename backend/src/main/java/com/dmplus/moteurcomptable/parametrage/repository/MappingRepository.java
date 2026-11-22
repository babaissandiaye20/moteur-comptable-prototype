package com.dmplus.moteurcomptable.parametrage.repository;

import com.dmplus.moteurcomptable.parametrage.entity.Mapping;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MappingRepository extends JpaRepository<Mapping, Long> {
    List<Mapping> findByRubriqueCode(String rubriqueCode);
    boolean existsByRubriqueCode(String rubriqueCode);
    boolean existsByCompteCode(String compteCode);
    List<Mapping> findByCompteCode(String compteCode);
    boolean existsByRubriqueCodeAndClasseActifAndCategorie(String rubriqueCode, String classeActif, String categorie);
}
