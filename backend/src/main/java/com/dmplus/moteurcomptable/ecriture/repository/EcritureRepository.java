package com.dmplus.moteurcomptable.ecriture.repository;

import com.dmplus.moteurcomptable.ecriture.entity.Ecriture;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EcritureRepository extends JpaRepository<Ecriture, Long> {
    Optional<Ecriture> findByEvenementRecuId(Long evenementRecuId);
    List<Ecriture> findAllByOrderByIdDesc();
}
