package com.dmplus.moteurcomptable.ecriture.repository;

import com.dmplus.moteurcomptable.ecriture.entity.EvenementRecu;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EvenementRecuRepository extends JpaRepository<EvenementRecu, Long> {
    Optional<EvenementRecu> findBySourceAndEventId(String source, String eventId);
}
