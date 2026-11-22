package com.dmplus.moteurcomptable.parametrage.repository;

import com.dmplus.moteurcomptable.parametrage.entity.Evenement;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EvenementRepository extends JpaRepository<Evenement, String> {
    boolean existsByOperationCode(String operationCode);
    boolean existsByJournalCode(String journalCode);
}
